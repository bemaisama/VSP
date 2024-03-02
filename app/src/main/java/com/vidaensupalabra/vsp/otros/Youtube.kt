package com.vidaensupalabra.vsp.otros

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeVideoView(videoId: String, isPlaying: MutableMap<String, Boolean>) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var videoPlayerReady by remember { mutableStateOf(false) }
    var videoInteractionEnabled by remember { mutableStateOf(false) } // Estado para controlar la interacción con el video
    var youTubePlayerInstance: YouTubePlayer? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    lifecycle.addObserver(this)
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            // Almacena la instancia del reproductor para su uso posterior
                            youTubePlayerInstance = youTubePlayer
                            // Prepara el video para la reproducción sin empezar automáticamente
                            youTubePlayer.cueVideo(videoId, 0f)
                            videoPlayerReady = true
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            val isCurrentlyPlaying = state == PlayerConstants.PlayerState.PLAYING
                            isPlaying[videoId] = isCurrentlyPlaying // Corrección aquí
                            Log.d("YouTubeVideoView", "Video is playing: $isCurrentlyPlaying")
                        }
                    })
                }
            },
            modifier = Modifier
                .align(Alignment.Center)
                .then(if (!videoInteractionEnabled) Modifier.clickable { /* Bloquea los clics */ } else Modifier)
        )

        // Capa transparente que intercepta los clics cuando la interacción con el video está deshabilitada
        if (!videoInteractionEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable { /* Intercepta los clics */ }
            )
        }

        // Ajuste en la lógica de reproducción
        if (videoPlayerReady && !isPlaying[videoId]!!) {
            Button(
                onClick = {
                    youTubePlayerInstance?.let {
                        if (!videoInteractionEnabled) {
                            // Habilita la interacción con el video y reproduce
                            videoInteractionEnabled = true
                            it.play()
                            isPlaying[videoId] = true // Actualiza el estado de reproducción para este videoId
                        }
                    }
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Reproducir")
            }
        }
    }

    // Gestión del ciclo de vida para asegurarse de que el YouTubePlayerView se libera
    DisposableEffect(context) {
        onDispose {
            // Realiza la limpieza necesaria aquí
        }
    }
}
