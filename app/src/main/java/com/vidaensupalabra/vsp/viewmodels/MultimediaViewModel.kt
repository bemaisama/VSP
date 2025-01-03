// MultimediaViewModel.kt

package com.vidaensupalabra.vsp.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vidaensupalabra.vsp.room.MultimediaDao
import com.vidaensupalabra.vsp.room.MultimediaDatabase
import com.vidaensupalabra.vsp.room.MultimediaEntity
import com.vidaensupalabra.vsp.room.MultimediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val REFRESH_INTERVAL_MS = 12 * 60 * 60 * 1000L // 12 horas
private const val PREFS_NAME = "multimedia_prefs"
private const val PREF_LAST_REFRESH = "last_refresh_time"

/**
 * Este ViewModel:
 *  - Para imágenes: solo si quieres, descargas el archivo completo (JPG/PNG) -> localPath
 *  - Para videos: solo si quieres, descargas la miniatura .jpg -> localPath
 *  - En refreshData() ya NO forzamos la descarga de imagen/miniatura. Se muestra la URL en Compose
 *  - Almacena localPath en DB, para carga offline en Compose (file://...).
 */
class MultimediaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: MultimediaDao
    private val prefs: SharedPreferences
    private val client = OkHttpClient()

    // Exponemos un Flow con la lista en Compose
    val multimediaFlow: Flow<List<MultimediaItem>>

    init {
        val db = MultimediaDatabase.getInstance(application)
        dao = db.multimediaDao()
        prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Convertimos la Entity a MultimediaItem
        multimediaFlow = dao.getAllFlow().map { entities ->
            entities.map { e ->
                MultimediaItem(
                    name = e.name,
                    url = e.url,
                    mimeType = e.mimeType,
                    localPath = e.localPath,
                    date = e.date ?: "" // si está nulo en DB, lo mostramos como cadena vacía o "N/A"
                )
            }
        }

        // Al iniciar, refrescamos si pasaron 12h
        viewModelScope.launch {
            refreshIfNeeded()
        }
    }

    private suspend fun refreshIfNeeded() {
        val lastRefresh = prefs.getLong(PREF_LAST_REFRESH, 0L)
        val now = System.currentTimeMillis()
        if ((now - lastRefresh) > REFRESH_INTERVAL_MS) {
            refreshData()
        }
    }

    /**
     * Descarga sólo el JSON remoto y actualiza DB,
     * pero YA NO descarga imágenes ni fotogramas de video.
     * Así la UI no se congela esperando la descarga.
     */
    suspend fun refreshData() = withContext(Dispatchers.IO) {
        val newItems = loadMultimediaDataFromUrl(
            "https://raw.githubusercontent.com/bemaisama/VSP/master/app/src/main/assets/multimedia.json"
        )
        // Insertamos sin localPath (ni imagen ni miniatura)
        val entities = newItems.map { item ->
            MultimediaEntity(
                name = item.name,
                url = item.url,
                mimeType = item.mimeType,
                localPath = null,  // No descargamos nada en este paso
                date = item.date   // Aqui guardamos la fecha tal cual viene del JSON
            )
        }
        dao.insertAll(entities)
        prefs.edit().putLong(PREF_LAST_REFRESH, System.currentTimeMillis()).apply()
    }

    // ---------- Descarga OFFLINE bajo demanda ----------

    /**
     * Descarga la imagen completa a localPath en 2do plano.
     * Llamas a esta función cuando el usuario abra la imagen,
     * o cuando quieras en segundo plano.
     * Luego, copiamos a un directorio público y notificamos al sistema (si deseas que aparezca en Galería).
     */
    fun downloadImageOffline(remoteUrl: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tempFile = downloadFile(remoteUrl, fileName)
            if (tempFile != null) {
                // Copiamos/movemos a carpeta pública (Pictures) y notificamos
                val publicFile = copyToPublicPictures(tempFile, fileName)
                if (publicFile != null) {
                    // Actualizamos en DB la localPath con la ruta pública
                    dao.insertAll(
                        listOf(
                            MultimediaEntity(
                                name = fileName,
                                url = remoteUrl,
                                mimeType = "image/jpeg", // o la original
                                localPath = publicFile.absolutePath,
                                date = null // OJO: si quieres, rellena con la date original
                            )
                        )
                    )
                    // Notificamos a la galería
                    addImageToGallery(getApplication(), publicFile)
                }
            }
        }
    }

    /**
     * Descarga SÓLO la miniatura (fotograma) de un video y guarda en DB.
     * Llamas a esta función cuando el usuario abra el video, etc.
     * Si deseas que aparezca en la galería de "Videos" también,
     * podrías copiarlo a Movies/ o DCIM/ y notificar similarly.
     */
    fun downloadVideoThumbnailOffline(remoteUrl: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val thumb = downloadVideoThumbnail(remoteUrl, fileName)
            if (thumb != null) {
                // Copiamos a carpeta pública (opcional, si lo deseas en galería)
                val publicFile = copyToPublicPictures(thumb, fileName + "_thumb")
                if (publicFile != null) {
                    dao.insertAll(
                        listOf(
                            MultimediaEntity(
                                name = fileName,
                                url = remoteUrl,
                                mimeType = "video/mp4", // o la real
                                localPath = publicFile.absolutePath,
                                date = null
                            )
                        )
                    )
                    // Notificamos a la galería
                    addImageToGallery(getApplication(), publicFile)
                }
            }
        }
    }

    // ---------- Funciones privadas ----------

    /**
     * Descarga una imagen (JPG/PNG) completa y guarda en filesDir temporalmente.
     * Luego ya la puedes copiar a una carpeta pública.
     */
    private fun downloadFile(remoteUrl: String, fileName: String): File? {
        val context = getApplication<Application>()
        val sanitized = fileName.replace("\\W+".toRegex(), "_") + ".jpg"
        val tempFile = File(context.filesDir, sanitized)

        return try {
            val request = Request.Builder().url(remoteUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                tempFile.outputStream().use { fos ->
                    response.body?.byteStream()?.copyTo(fos)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extrae un fotograma remoto usando MediaMetadataRetriever.
     * Si no se puede (status=0x80000000), retornamos null y mostrarás placeholder.
     * Guardamos en filesDir temporalmente.
     */
    private fun downloadVideoThumbnail(remoteUrl: String, fileName: String): File? {
        val context = getApplication<Application>()
        val sanitized = fileName.replace("\\W+".toRegex(), "_") + "_thumb.jpg"
        val tempFile = File(context.filesDir, sanitized)

        try {
            if (tempFile.exists()) {
                // Si ya existe, no recreamos
                return tempFile
            }
            val retriever = MediaMetadataRetriever()
            // setDataSource con URL remoto a veces falla en algunos dispositivos
            retriever.setDataSource(remoteUrl, HashMap())
            val bmp = retriever.getFrameAtTime(1_000_000) // 1s
            retriever.release()

            if (bmp != null) {
                FileOutputStream(tempFile).use { fos ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                }
                return tempFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Copia un archivo desde el directorio privado de la app a la carpeta pública de "Pictures".
     * Retorna el File en la carpeta pública, o null si falla.
     */
    private fun copyToPublicPictures(original: File, desiredName: String): File? {
        // Directorio público
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }
        val sanitized = desiredName.replace("\\W+".toRegex(), "_") + ".jpg"
        val publicFile = File(picturesDir, sanitized)

        return try {
            original.inputStream().use { input ->
                publicFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            publicFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Envía un broadcast para que el sistema indexe la imagen (o video) y aparezca en la Galería.
     */
    private fun addImageToGallery(context: Context, file: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val fileUri = Uri.fromFile(file)
        mediaScanIntent.data = fileUri
        context.sendBroadcast(mediaScanIntent)
    }

    private suspend fun loadMultimediaDataFromUrl(url: String): List<MultimediaItem> {
        return withContext(Dispatchers.IO) {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.connect()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                Json.decodeFromString(json)
            } else {
                throw Exception("Error al cargar datos: ${conn.responseCode}")
            }
        }
    }

    // ---------- Caché en memoria para no re-extraer en la misma sesión ----------
    private val videoThumbnailCache = mutableMapOf<String, Bitmap?>()

    /**
     * Llamado desde la UI. Si localPath no existe,
     * intenta extraer fotograma remoto. Si falla (error), retorna null => placeholder.
     */
    suspend fun getVideoFrameCached(pathOrUrl: String, frameMicros: Long): Bitmap? {
        // Cache en memoria para no repetir
        videoThumbnailCache[pathOrUrl]?.let { return it }

        val bmp = loadVideoFrame(pathOrUrl, frameMicros)
        videoThumbnailCache[pathOrUrl] = bmp
        return bmp
    }

    private suspend fun loadVideoFrame(
        pathOrUrl: String,
        frameMicros: Long
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                if (pathOrUrl.startsWith("file://")) {
                    retriever.setDataSource(pathOrUrl.removePrefix("file://"))
                } else {
                    // URL remoto
                    retriever.setDataSource(pathOrUrl, HashMap())
                }
                retriever.getFrameAtTime(frameMicros)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                retriever.release()
            }
        }
    }
}

/** Factoría para Compose sin Hilt */
@Suppress("UNCHECKED_CAST")
class MultimediaViewModelFactory(
    private val app: Application
) : ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MultimediaViewModel::class.java)) {
            return MultimediaViewModel(app) as T
        }
        return super.create(modelClass)
    }
}

