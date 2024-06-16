// MultimediaScreen.kt

// Importaciones necesarias
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.White

// Ajusta esta lista con las URLs directas de tus imágenes
val imageUrls = listOf(
    "",
    "",
    "",
    "",

    )

@Composable
fun MultimediaScreen() {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GalleryScreen(imageUrls)
        }
    }
}


@Composable
fun GalleryScreen(imageUrls: List<String>) {
    var showDialog by remember { mutableStateOf(false) }
    var currentImageUrl by remember { mutableStateOf("") }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        items(imageUrls) { imageUrl ->
            Image(
                painter = rememberImagePainter(imageUrl),
                contentDescription = "Imagen",
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        currentImageUrl = imageUrl
                        showDialog = true
                    }
                    .padding(4.dp)
            )
        }
    }

    if (showDialog) {
        ImagenFullScreenMultimedia(rememberImagePainter(currentImageUrl)) {
            showDialog = false
        }
    }
}

@Composable
fun ImagenFullScreenMultimedia(painter: Painter, onClose: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.Black)
        .clickable { onClose() }, contentAlignment = Alignment.Center) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Image(
            painter = painter,
            contentDescription = "Imagen ampliada",
            modifier = Modifier
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

