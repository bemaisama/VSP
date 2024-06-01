package com.vidaensupalabra.vsp.ventanas

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vidaensupalabra.vsp.MainViewModel
import com.vidaensupalabra.vsp.otros.YouTubeVideoView
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.VspMarco
import com.vidaensupalabra.vsp.ui.theme.White

@Composable
fun MusicaScreen(viewModel: MainViewModel) {
    val canciones = viewModel.canciones.observeAsState(initial = listOf())

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text(
                "Canciones para este Domingo",
                color = White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        canciones.value.forEach { cancion ->
            item {
                CancionCard(
                    titulo = cancion.titulo1,
                    artista = cancion.artista1,
                    letra = cancion.letra1
                )
            }
            item {
                CancionCard(
                    titulo = cancion.titulo2,
                    artista = cancion.artista2,
                    letra = cancion.letra2
                )
            }
            item {
                CancionCard(
                    titulo = cancion.titulo3,
                    artista = cancion.artista3,
                    letra = cancion.letra3
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            VideosDomingo(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CancionCard(titulo: String, artista: String, letra: String) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        backgroundColor = VspBase,
        elevation = 4.dp,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.headlineSmall.copy(color = VspMarco),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.Add,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (isExpanded) {
                Text(
                    "Artista: $artista",
                    style = MaterialTheme.typography.bodyLarge.copy(color = White),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextoConformato(letra)
            }
        }
    }
}

@Composable
fun TextoConformato(text: String) {
    val processedText = text.replace("/n", "\n")

    val styledText = buildAnnotatedString {
        var startIndex: Int
        var endIndex: Int
        var currentIndex = 0

        while (currentIndex < processedText.length) {
            startIndex = processedText.indexOf("*", currentIndex)
            if (startIndex == -1) {
                append(processedText.substring(currentIndex, processedText.length))
                break
            }
            endIndex = processedText.indexOf("*", startIndex + 1)
            if (endIndex == -1) {
                append(processedText.substring(currentIndex, processedText.length))
                break
            }

            if (startIndex > currentIndex) {
                append(processedText.substring(currentIndex, startIndex))
            }

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = White)) {
                append(processedText.substring(startIndex + 1, endIndex))
            }

            currentIndex = endIndex + 1
        }
    }

    Text(text = styledText, color = White, style = MaterialTheme.typography.bodyMedium)
}

fun extractoVideosDomingo(url: String): String? {
    val pattern = Regex("^(https?://)?(www.youtube.com/watch\\?v=|youtu.be/)([\\w-]+)(.*\$)")
    val matchResult = pattern.find(url)
    return matchResult?.groups?.get(3)?.value
}

@Composable
fun VideosDomingo(viewModel: MainViewModel = viewModel()) {
    val canciones = viewModel.canciones.observeAsState(initial = listOf())
    val isPlaying = remember { mutableStateMapOf<String, Boolean>() }
    val currentTime = remember { mutableStateMapOf<String, Float>() }

    if (canciones.value.isNotEmpty()) {
        val cancion = canciones.value[0]
        val videoId1 = extractoVideosDomingo(cancion.youtubevideo1) ?: ""
        val videoId2 = extractoVideosDomingo(cancion.youtubevideo2) ?: ""
        val videoId3 = extractoVideosDomingo(cancion.youtubevideo3) ?: ""

        isPlaying.putIfAbsent(videoId1, false)
        isPlaying.putIfAbsent(videoId2, false)
        isPlaying.putIfAbsent(videoId3, false)
        currentTime.putIfAbsent(videoId1, 0f)
        currentTime.putIfAbsent(videoId2, 0f)
        currentTime.putIfAbsent(videoId3, 0f)

        Log.d("VideosYoutubeMasVideos", "Loading YouTube video with ID: $videoId1")
        Log.d("VideosYoutubeMasVideos", "Loading YouTube video with ID: $videoId2")
        Log.d("VideosYoutubeMasVideos", "Loading YouTube video with ID: $videoId3")

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Videos Domingo",
                style = MaterialTheme.typography.headlineMedium,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            if (videoId1.isNotEmpty()) {
                YouTubeVideoView(videoId = videoId1, isPlaying, currentTime)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (videoId2.isNotEmpty()) {
                YouTubeVideoView(videoId = videoId2, isPlaying, currentTime)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (videoId3.isNotEmpty()) {
                YouTubeVideoView(videoId = videoId3, isPlaying, currentTime)
            }
        }
    }
}
