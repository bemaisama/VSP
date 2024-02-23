package com.vidaensupalabra.vsp.otros

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeVideoView(videoId: String, isPlaying: MutableState<Boolean>) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    AndroidView(
        factory = { ctx ->
            YouTubePlayerView(ctx).also { view ->
                lifecycle.addObserver(view)
                view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // Prepara el video para la reproducción sin empezar automáticamente
                        youTubePlayer.cueVideo(videoId, 0f)
                    }

                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                        val isCurrentlyPlaying = state == PlayerConstants.PlayerState.PLAYING
                        isPlaying.value = isCurrentlyPlaying
                        Log.d("YouTubeVideoView", "Video is playing: $isCurrentlyPlaying")
                    }
                })
            }
        },
        modifier = Modifier // Utiliza Modifier si es necesario para tu layout
    )

    // Gestión del ciclo de vida para asegurarse de que el YouTubePlayerView se libera
    DisposableEffect(context) {
        onDispose {
            // Si necesitas realizar alguna limpieza, aquí sería el lugar
        }
    }
}
