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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeVideoView(videoId: String, isPlaying: MutableMap<String, Boolean>, currentTime: MutableMap<String, Float>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var videoPlayerReady by remember { mutableStateOf(false) }
    var videoInteractionEnabled by remember { mutableStateOf(false) }
    var youTubePlayerInstance: YouTubePlayer? by remember { mutableStateOf(null) }
    var youTubePlayerViewInstance: YouTubePlayerView? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    youTubePlayerViewInstance = this
                    lifecycleOwner.lifecycle.addObserver(this)
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayerInstance = youTubePlayer
                            videoPlayerReady = true
                            videoInteractionEnabled = true
                            if (isPlaying[videoId] == true) {
                                youTubePlayer.loadVideo(videoId, currentTime[videoId] ?: 0f)
                            } else {
                                youTubePlayer.cueVideo(videoId, currentTime[videoId] ?: 0f)
                            }
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            val isCurrentlyPlaying = state == PlayerConstants.PlayerState.PLAYING
                            isPlaying[videoId] = isCurrentlyPlaying
                            if (isCurrentlyPlaying) {
                                videoInteractionEnabled = true
                            }
                            Log.d("YouTubeVideoView", "Video is playing: $isCurrentlyPlaying")
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            currentTime[videoId] = second
                        }
                    })
                }
            },
            modifier = Modifier
                .align(Alignment.Center)
                .then(if (!videoInteractionEnabled) Modifier.clickable { } else Modifier)
        )

        if (!videoInteractionEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable { }
            )
        }

        if (videoPlayerReady && !(isPlaying[videoId] ?: false)) {
            videoInteractionEnabled = false

            Button(
                onClick = {
                    youTubePlayerInstance?.let {
                        videoInteractionEnabled = true
                        it.play()
                        isPlaying[videoId] = true
                        videoInteractionEnabled = true
                    }
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Reproducir")
            }
        }
    }

    DisposableEffect(context) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // No hacemos nada aquí para evitar detener el video en scroll
                }
                Lifecycle.Event.ON_DESTROY -> {
                    youTubePlayerViewInstance?.release() // Libera los recursos del YouTubePlayerView cuando se destruye la actividad/fragmento
                    youTubePlayerViewInstance = null // Limpia la instancia del reproductor
                    Log.d("YouTubeVideoView", "Video player view disposed")
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            youTubePlayerViewInstance?.release() // Asegúrate de limpiar los recursos
            youTubePlayerViewInstance = null // Limpia la instancia del reproductor
            Log.d("YouTubeVideoView", "Video player view disposed in onDispose")
        }
    }
}
