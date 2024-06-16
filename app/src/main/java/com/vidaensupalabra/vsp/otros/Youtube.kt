// Youtube.kt

package com.vidaensupalabra.vsp.otros

import android.util.Log
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
    var youTubePlayerInstance by remember { mutableStateOf<YouTubePlayer?>(null) }
    var youTubePlayerViewInstance by remember { mutableStateOf<YouTubePlayerView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                Log.d("YouTubeVideoView", "Creating YouTubePlayerView for videoId: $videoId")
                YouTubePlayerView(ctx).apply {
                    youTubePlayerViewInstance = this
                    lifecycleOwner.lifecycle.addObserver(this)
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            Log.d("YouTubeVideoView", "YouTubePlayer is ready for videoId: $videoId")
                            youTubePlayerInstance = youTubePlayer
                            if (isPlaying[videoId] == true) {
                                Log.d("YouTubeVideoView", "Loading video: $videoId at time: ${currentTime[videoId]}")
                                youTubePlayer.loadVideo(videoId, currentTime[videoId] ?: 0f)
                            } else {
                                Log.d("YouTubeVideoView", "Cueing video: $videoId at time: ${currentTime[videoId]}")
                                youTubePlayer.cueVideo(videoId, currentTime[videoId] ?: 0f)
                            }
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            val isCurrentlyPlaying = state == PlayerConstants.PlayerState.PLAYING
                            isPlaying[videoId] = isCurrentlyPlaying
                            Log.d("YouTubeVideoView", "State changed for videoId: $videoId. Is playing: $isCurrentlyPlaying")
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            currentTime[videoId] = second
                            Log.d("YouTubeVideoView", "Current second for videoId: $videoId is $second")
                        }
                    })
                }
            },
            update = { view ->
                youTubePlayerViewInstance = view
            },
            modifier = Modifier
                .align(Alignment.Center)
                .then(Modifier.clickable { })
        )

        if (youTubePlayerInstance == null || isPlaying[videoId] == false) {
            Log.d("YouTubeVideoView", "Displaying play button for videoId: $videoId")
            Button(
                onClick = {
                    youTubePlayerInstance?.let {
                        Log.d("YouTubeVideoView", "Play button clicked for videoId: $videoId")
                        it.play()
                        isPlaying[videoId] = true
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
                Lifecycle.Event.ON_DESTROY -> {
                    youTubePlayerViewInstance?.release()
                    youTubePlayerViewInstance = null
                    Log.d("YouTubeVideoView", "Video player view disposed for videoId: $videoId")
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            youTubePlayerViewInstance?.release()
            youTubePlayerViewInstance = null
            Log.d("YouTubeVideoView", "Video player view disposed in onDispose for videoId: $videoId")
        }
    }
}