// MainViewModel.kt

package com.vidaensupalabra.vsp.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.vidaensupalabra.vsp.Cancion
import com.vidaensupalabra.vsp.ImportantAnnouncement

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val db = FirebaseFirestore.getInstance()
    var anuncios = mutableStateListOf<ImportantAnnouncement>()

    init {
        leerAnuncios()
    }

    private fun leerAnuncios() {
        val db = FirebaseFirestore.getInstance()
        // Esta consulta recupera todos los anuncios, independientemente de su lastUpdated
        db.collection("anuncios").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            Log.d(TAG, "Fetched ${snapshots?.size()} announcements")

            if (snapshots != null && !snapshots.isEmpty) {
                // Limpia la lista actual para evitar duplicados
                anuncios.clear()

                // Itera sobre los documentos y los añade a la lista
                for (document in snapshots.documents) {
                    val anuncio = document.toObject(ImportantAnnouncement::class.java)
                    if (anuncio != null) {
                        anuncios.add(anuncio)
                        Log.d(TAG, "Announcement added: ${anuncio.youtubevideo}")
                    }
                }
            }
        }
    }

    private val _canciones = MutableLiveData<List<Cancion>>()
    val canciones: LiveData<List<Cancion>> = _canciones

    init {
        cargarCanciones()
    }

    private fun cargarCanciones() {
        db.collection("canciones").addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(TAG, "Error al escuchar cambios en Firestore", error)
                return@addSnapshotListener
            }

            val cancionesList = ArrayList<Cancion>()
            for (doc in value!!) {
                val cancion = doc.toObject(Cancion::class.java)
                cancionesList.add(cancion)
            }
            _canciones.value = cancionesList
        }
    }
}

