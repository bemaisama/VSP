// MusicaScreen.kt

package com.vidaensupalabra.vsp.ventanas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vidaensupalabra.vsp.R
import com.vidaensupalabra.vsp.otros.YouTubeVideoView
import com.vidaensupalabra.vsp.otros.fetchTextFromUrl
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.VspMarco
import com.vidaensupalabra.vsp.ui.theme.White
import com.vidaensupalabra.vsp.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

@Composable
fun MusicaScreen(viewModel: MainViewModel = viewModel()) {
    val canciones by viewModel.canciones.observeAsState(initial = listOf())
    val isPlaying = rememberSaveable(saver = SnapshotStateMapSaver<String, Boolean>()) { mutableStateMapOf<String, Boolean>() }
    val currentTime = rememberSaveable(saver = SnapshotStateMapSaver<String, Float>()) { mutableStateMapOf<String, Float>() }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text(
                "Canciones para este Domingo",
                color = White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        canciones.forEach { cancion ->
            item {
                CancionCard(letraUrl = cancion.letra1, videoUrl = cancion.youtubevideo1, isPlaying = isPlaying, currentTime = currentTime)
            }
            item {
                CancionCard(letraUrl = cancion.letra2, videoUrl = cancion.youtubevideo2, isPlaying = isPlaying, currentTime = currentTime)
            }
            item {
                CancionCard(letraUrl = cancion.letra3, videoUrl = cancion.youtubevideo3, isPlaying = isPlaying, currentTime = currentTime)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CancionCard(letraUrl: String, videoUrl: String, isPlaying: MutableMap<String, Boolean>, currentTime: MutableMap<String, Float>) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var titulo by remember { mutableStateOf<String?>(null) }
    var letra by remember { mutableStateOf<AnnotatedString?>(null) }
    val videoId = extractoVideosDomingo(videoUrl) ?: ""

    // Estado de la Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(letraUrl) {
        if (letraUrl.isNotEmpty() && (letraUrl.startsWith("http://") || letraUrl.startsWith("https://"))) {
            fetchTextFromUrl(context, letraUrl) { fetchedTitulo, fetchedLetra ->
                titulo = fetchedTitulo ?: "Sin título"
                letra = fetchedLetra?.let { htmlText ->
                    convertHtmlToAnnotatedString(htmlText)
                } ?: AnnotatedString("Contenido no disponible")
            }
        } else {
            titulo = "No Disponible"
            letra = AnnotatedString("No Disponible")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                        text = titulo ?: "Cargando...",
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
                    Spacer(modifier = Modifier.height(4.dp))
                    if (letra != null) {
                        SelectionContainer {
                            Text(
                                text = letra!!,
                                color = White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                clipboardManager.setText(letra!!)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Texto copiado al portapapeles")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = VspMarco)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_content_copy_24),
                                contentDescription = "Copiar",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Copiar Todo", color = White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SnackbarHost(hostState = snackbarHostState)
                    } else {
                        CircularProgressIndicator(color = White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (videoId.isNotEmpty()) {
                        YouTubeVideoView(videoId = videoId, isPlaying = isPlaying, currentTime = currentTime)
                    }
                }
            }
        }
    }
}

fun convertHtmlToAnnotatedString(html: String): AnnotatedString {
    val document = Jsoup.parse(html)
    val body = document.body()

    return buildAnnotatedString {
        var isBold = false
        for (element in body.children()) {
            if (element.tagName() == "p") {
                val text = element.text()
                val parts = text.split("*")

                parts.forEachIndexed { index, part ->
                    if (index % 2 == 1) {
                        isBold = !isBold
                    }
                    if (isBold) {
                        append(part)
                    } else {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(part)
                        }
                    }
                }
                append("\n")
            } else {
                appendElement(element, isBold)
            }
        }
    }
}

fun AnnotatedString.Builder.appendElement(element: Element, isBold: Boolean = false) {
    val tagName = element.tagName()

    when (tagName) {
        "h1", "h2", "h3", "h4", "h5", "h6" -> {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = when (tagName) {
                "h1" -> 26.sp
                "h2" -> 22.sp
                "h3" -> 18.sp
                "h4" -> 16.sp
                "h5" -> 14.sp
                "h6" -> 12.sp
                else -> 12.sp
            })) {
                append(element.text())
                append("\n\n")
            }
        }
        "p" -> {
            val text = element.text()
            val parts = text.split("*")

            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(part)
                    }
                } else {
                    append(part)
                }
            }
            append("\n")
        }
        "strong", "b" -> {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(element.text())
            }
        }
        "em", "i" -> {
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                append(element.text())
            }
        }
        "span" -> {
            append(element.text())
            element.children().forEach { child ->
                appendElement(child, isBold)
            }
        }
        else -> {
            append(element.text())
            element.children().forEach { child ->
                appendElement(child, isBold)
            }
        }
    }
}

fun extractoVideosDomingo(url: String): String? {
    val pattern = Regex("^(https?://)?(www.youtube.com/watch\\?v=|youtu.be/)([\\w-]+)(.*\$)")
    val matchResult = pattern.find(url)
    return matchResult?.groups?.get(3)?.value
}

fun <K, V> SnapshotStateMapSaver(): Saver<SnapshotStateMap<K, V>, Any> {
    return listSaver(
        save = { map -> map.toList() },
        restore = { list ->
            SnapshotStateMap<K, V>().apply {
                list.forEach { (k, v) -> this[k] = v }
            }
        }
    )
}

