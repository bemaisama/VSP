//MultimediaScreen.kt
package com.vidaensupalabra.vsp.ventanas

import android.app.Application
import android.graphics.Bitmap
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vidaensupalabra.vsp.R
import com.vidaensupalabra.vsp.room.MultimediaItem
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.White
import com.vidaensupalabra.vsp.viewmodels.MultimediaViewModel
import com.vidaensupalabra.vsp.viewmodels.MultimediaViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultimediaScreen(
    viewModel: MultimediaViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MultimediaViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val multimediaItems by viewModel.multimediaFlow.collectAsState(initial = emptyList())

    // Filtrar por imágenes o videos
    val imageItems = multimediaItems.filter { it.mimeType.startsWith("image/") }
    val videoItems = multimediaItems.filter { it.mimeType.startsWith("video/") }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentList = if (selectedTabIndex == 0) imageItems else videoItems

    // Agrupamos por YYYY-MM
    val groupedByMonth = currentList.groupBy { item ->
        item.date?.take(7) // "2024-12", por ejemplo
    }

    // Nuevo estado: si selectedMonth es null => vista de “carpetas”
    // si no es null => vista de ítems de ese mes
    var selectedMonth by remember { mutableStateOf<String?>(null) }

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = VspBase,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedMonth == null) "Multimedia" else "Mes: $selectedMonth",
                            color = White,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = VspBase,
                contentColor = White
            ) {
                Tab(
                    selected = (selectedTabIndex == 0),
                    onClick = {
                        selectedTabIndex = 0
                        selectedMonth = null // Reinicia al cambiar de pestaña
                    },
                    text = { Text("Imágenes") }
                )
                Tab(
                    selected = (selectedTabIndex == 1),
                    onClick = {
                        selectedTabIndex = 1
                        selectedMonth = null
                    },
                    text = { Text("Videos") }
                )
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = {
                    isRefreshing = true
                    scope.launch {
                        viewModel.refreshData()
                        isRefreshing = false
                        selectedMonth = null
                    }
                }
            ) {
                if (selectedMonth == null) {
                    // Vista de “carpetas” (grupo por mes)
                    MonthFoldersScreen(
                        groupedByMonth = groupedByMonth,
                        onMonthClick = { monthKey ->
                            selectedMonth = monthKey
                        }
                    )
                } else {
                    // Vista de ítems del mes seleccionado
                    val itemsOfMonth = groupedByMonth[selectedMonth].orEmpty()
                    GalleryScreen(
                        multimediaItems = itemsOfMonth,
                        viewModel = viewModel,
                        onBack = {
                            // Botón "Atrás" (opcional)
                            selectedMonth = null
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun MonthFoldersScreen(
    groupedByMonth: Map<String?, List<MultimediaItem>>,
    onMonthClick: (String?) -> Unit
) {
    // Convertimos el map en una lista de pares (mes, listaDeItems)
    val monthList = groupedByMonth.entries.sortedByDescending { it.key } // si quieres orden desc
    // Ejemplo: [("2024-12", items...), ("2024-11", items...), ...]

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(monthList) { (monthKey, itemsInMonth) ->
            // Extraemos un item cualquiera como preview (p.ej. el primero)
            val previewItem = itemsInMonth.firstOrNull()
            // Para mostrar un “folder card”
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
                    .clickable {
                        onMonthClick(monthKey) // Navegamos a la vista de ítems de este mes
                    },
                contentAlignment = Alignment.Center
            ) {
                if (previewItem != null) {
                    // Usa la miniatura (si es imagen) o un approach similar (si es video).
                    FolderPreview(previewItem)
                } else {
                    // No hay items? Placeholder
                    Text("Sin ítems")
                }

                // Nombre del mes (2024-12 => "Diciembre 2024"?)
                // Podrías parsear y formatear con SimpleDateFormat u otra librería
                Text(
                    text = monthKey ?: "Desconocido",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .background(Color(0x88000000))
                        .padding(4.dp)
                )
            }
        }
    }
}
@Composable
fun FolderPreview(item: MultimediaItem) {
    val context = LocalContext.current

    // Si es imagen
    if (item.mimeType.startsWith("image/")) {
        val dataSource = item.localPath?.let { "file://$it" } ?: item.url
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(dataSource)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_placeholder)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
        )
        Image(
            painter = painter,
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Video => podrías mostrar un ícono o un frame en miniatura
        Image(
            painter = rememberAsyncImagePainter(R.drawable.placeholder),
            contentDescription = "Video preview",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun GalleryScreen(
    multimediaItems: List<MultimediaItem>,
    viewModel: MultimediaViewModel,
    onBack: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<MultimediaItem?>(null) }

    // Ejemplo: un botón “Atrás” arriba (opcional)
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "< Atrás",
            modifier = Modifier
                .clickable { onBack() }
                .padding(8.dp),
            color = Color.White
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        items(multimediaItems) { item ->
            when {
                item.mimeType.startsWith("image/") -> {
                    ImageThumbnail(
                        item = item,
                        onClick = {
                            viewModel.downloadImageOffline(item.url, item.name)
                            currentItem = item
                            showDialog = true
                        }
                    )
                }
                item.mimeType.startsWith("video/") -> {
                    VideoThumbnail(
                        item = item,
                        onClick = {
                            viewModel.downloadVideoThumbnailOffline(item.url, item.name)
                            currentItem = item
                            showDialog = true
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Dialog fullscreen
    if (showDialog) {
        currentItem?.let { media ->
            Dialog(onDismissRequest = { showDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    if (media.mimeType.startsWith("image/")) {
                        ImagenFullScreenMultimedia(media.localPath ?: media.url)
                    } else if (media.mimeType.startsWith("video/")) {
                        VideoFullScreen(media.url)
                    }
                }
            }
        }
    }
}

/* ---------- IMÁGENES ---------- */
@Composable
fun ImageThumbnail(
    item: MultimediaItem,
    onClick: () -> Unit
) {
    // Si tenemos localPath, usamos file://..., si no, la URL remota
    val dataSource = item.localPath?.let { "file://$it" } ?: item.url

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(dataSource)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_placeholder)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    )

    val state = painter.state

    Box(
        modifier = Modifier
            .size(100.dp)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize()
        )

        if (state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        }
    }
}

/* ---------- VIDEOS ---------- */
@Composable
fun VideoThumbnail(
    item: MultimediaItem,
    onClick: () -> Unit,
    viewModel: MultimediaViewModel,
    frameMicros: Long = 1_000_000,
    modifier: Modifier = Modifier
) {
    val pathOrUrl = item.localPath?.let { "file://$it" } ?: item.url

    // produceState: extraemos la miniatura en 2do plano
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = pathOrUrl) {
        // Podrías usar el thumbnail offline si lo tienes. Si no, intente remotamente.
        // Si falla, placeholder.
        value = viewModel.getVideoFrameCached(pathOrUrl, frameMicros)
    }

    Box(
        modifier = modifier
            .size(100.dp)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmapState.value
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // placeholder
            Image(
                painter = rememberAsyncImagePainter(R.drawable.placeholder),
                contentDescription = "placeholder video",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/* ---------- Imagen fullscreen con zoom ---------- */
@Composable
fun ImagenFullScreenMultimedia(dataSource: String) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(dataSource)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_placeholder)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = "Imagen ampliada",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = max(scale, 1f),
                    scaleY = max(scale, 1f),
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

/* ---------- Video fullscreen con ExoPlayer ---------- */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoFullScreen(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
