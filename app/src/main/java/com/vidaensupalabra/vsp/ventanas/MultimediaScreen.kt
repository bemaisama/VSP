// MultimediaScreen.kt

import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vidaensupalabra.vsp.R
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class MultimediaItem(
    val name: String,
    val url: String,
    val mimeType: String
)

@Composable
fun MultimediaScreen() {
    val context = LocalContext.current
    var multimediaItems by remember { mutableStateOf<List<MultimediaItem>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar datos asincrónicamente
    LaunchedEffect(Unit) {
        try {
            multimediaItems = loadMultimediaDataFromUrl("https://raw.githubusercontent.com/bemaisama/VSP/master/app/src/main/assets/multimedia.json")
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    if (errorMessage != null) {
        // Mostrar un mensaje de error en caso de fallo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: $errorMessage",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        // Separar los elementos por tipo
        val imageItems = multimediaItems.filter { it.mimeType.startsWith("image/") }
        val videoItems = multimediaItems.filter { it.mimeType.startsWith("video/") }

        // Control de pestañas
        var selectedTabIndex by remember { mutableStateOf(0) }

        Scaffold(
            backgroundColor = VspBase,
            topBar = {
                TopAppBar(
                    modifier = Modifier.height(90.dp),
                    backgroundColor = Color.Transparent,
                    contentColor = Color.White,
                    elevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Multimedia",
                            color = White,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Pestañas para cambiar entre imágenes y videos
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = VspBase,
                    contentColor = White
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    ) {
                        Text(
                            text = "Imágenes",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    ) {
                        Text(
                            text = "Videos",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Contenido de la pestaña seleccionada
                when (selectedTabIndex) {
                    0 -> GalleryScreen(imageItems) // Mostrar imágenes
                    1 -> GalleryScreen(videoItems) // Mostrar videos
                }
            }
        }
    }
}

suspend fun loadMultimediaDataFromUrl(url: String): List<MultimediaItem> {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000 // Tiempo de espera para conectar (15 segundos)
            connection.readTimeout = 15000    // Tiempo de espera para leer datos (15 segundos)
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                Json.decodeFromString(json)
            } else {
                throw Exception("Error al cargar datos: Código de respuesta ${connection.responseCode}")
            }
        } catch (e: Exception) {
            throw Exception("Error al cargar datos desde el URL", e)
        }
    }
}

@Composable
fun GalleryScreen(multimediaItems: List<MultimediaItem>) {
    var showDialog by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<MultimediaItem?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        items(multimediaItems) { item ->
            when {
                item.mimeType.startsWith("image/") -> {
                    Image(
                        painter = rememberImagePainter(
                            data = item.url,
                            builder = {
                                placeholder(R.drawable.placeholder)
                                error(R.drawable.error_placeholder)
                            }
                        ),
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                currentItem = item
                                showDialog = true
                            }
                            .padding(4.dp)
                    )
                }
                item.mimeType.startsWith("video/") -> {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray)
                            .clickable {
                                currentItem = item
                                showDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    Glide.with(context)
                                        .asBitmap()
                                        .load(item.url)
                                        .apply(RequestOptions().frame(1000000)) // Captura el cuadro en el tiempo 1s
                                        .into(this)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        currentItem?.let {
            Dialog(
                onDismissRequest = { showDialog = false },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            it.mimeType.startsWith("image/") -> {
                                ImagenFullScreenMultimedia(it.url) {
                                    showDialog = false
                                }
                            }
                            it.mimeType.startsWith("video/") -> {
                                VideoFullScreen(it.url) {
                                    showDialog = false
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ImagenFullScreenMultimedia(imageUrl: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val painter = rememberImagePainter(
            data = imageUrl,
            builder = {
                placeholder(R.drawable.placeholder)
                error(R.drawable.error_placeholder)
            }
        )

        Image(
            painter = painter,
            contentDescription = "Imagen ampliada",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(1f, scale),
                    scaleY = maxOf(1f, scale),
                    translationX = offset.x,
                    translationY = offset.y
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offset += pan
                    }
                }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoFullScreen(videoUrl: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }},
            modifier = Modifier.fillMaxSize()
        )
    }
}
