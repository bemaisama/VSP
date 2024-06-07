package com.vidaensupalabra.vsp.ventanas

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vidaensupalabra.vsp.MainViewModel
import com.vidaensupalabra.vsp.R
import com.vidaensupalabra.vsp.R.drawable
import com.vidaensupalabra.vsp.otros.YouTubeVideoView
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.VspMarco
import com.vidaensupalabra.vsp.ui.theme.VspMarcoTransparente
import com.vidaensupalabra.vsp.ui.theme.VspMarcoTransparente50
import com.vidaensupalabra.vsp.ui.theme.White

@Composable
fun HomeScreen() {
    val isYouTubeVideoPlaying = remember { mutableStateOf(false) }

    Scaffold(
        backgroundColor = VspBase, // Color de fondo de la pantalla
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                backgroundColor = Color.Transparent, // Hace transparente el fondo del AppBar
                contentColor = Color.White,
                elevation = 0.dp // Elimina la sombra debajo del AppBar
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner),
                    contentDescription = "Banner Iglesia",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight() // Ajusta esta altura para hacer el banner más grande
                        .padding(horizontal = 0.dp, vertical = 8.dp) // Ajusta el padding si es necesario
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(VspBase) // Asegura que el fondo de la columna también tenga el color base
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenedor del video y el mensaje
            Box(
                modifier = Modifier
                    .height(370.dp)
                    .fillMaxWidth()
            ) {
                BackgroundVideo(
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = isYouTubeVideoPlaying
                )
                TextoBienvenida()
            }
            Spacer(modifier = Modifier.height(8.dp))
            Horarios()
            Spacer(modifier = Modifier.height(16.dp))
            VideosYoutubeMasVideos(isPlaying = isYouTubeVideoPlaying)
            Spacer(modifier = Modifier.height(16.dp))
            EquipoPastoral()
            Spacer(modifier = Modifier.height(16.dp))
            Ministerios_asociados(
                "https://story.church/",
                "https://www.acts29.com/network/latin-america/?lang=es",
                "https://thepillarnetwork.com/"
            )
        }
    }
}

@Composable
fun BackgroundVideo(modifier: Modifier = Modifier, isPlaying: MutableState<Boolean>) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var videoView: VideoView? = null

    if (!isPlaying.value) { // Solo muestra el VideoView si el video de YouTube no está en reproducción
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI("android.resource://${ctx.packageName}/${R.raw.videoloop_vsp}".toUri())
                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.isLooping = true
                        start()
                        val scale = 1.5f // Ajusta este valor según la cantidad de zoom que desees
                        scaleX = scale
                        scaleY = scale
                    }
                    videoView = this
                }
            }
        )
    }

    // Utilizamos DisposableEffect para detener la reproducción y liberar el VideoView cuando el composable es removido
    DisposableEffect(isPlaying.value) {
        if (isPlaying.value) {
            videoView?.pause()
        } else {
            videoView?.start()
        }
        onDispose { }
    }

    // Capa semitransparente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VspBase.copy(alpha = 0.7f)) // Ajusta la transparencia aquí
    )
}

@Composable
fun TextoBienvenida() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Para todo en la vida",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "CRISTO ES SUFICIENTE!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFBDBDBD), // Cambia este color si tienes VspMarco definido
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Somos una iglesia cristiana de confesión bautista, ubicada en la ciudad de Riohacha (COL), trabajando para promover la vida que agrada a Dios, que es conforme a Su Palabra y cuyo fundamento es Cristo y Su glorioso evangelio.\n\n¡Estamos felices que te intereses por conocernos!",
            style = MaterialTheme.typography.bodyMedium,
            color = White,
            textAlign = TextAlign.Center
        )
    }
}

fun extractYouTubeVideoId(url: String): String? {
    val pattern = Regex("^(https?://)?(www.youtube.com/watch\\?v=|youtu.be/)([\\w-]+)(.*\$)")
    val matchResult = pattern.find(url)
    return matchResult?.groups?.get(3)?.value
}

@Composable
fun VideosYoutubeMasVideos(viewModel: MainViewModel = viewModel(), isPlaying: MutableState<Boolean>) {
    val context = LocalContext.current
    val announcements = viewModel.anuncios
    val isPlayingState = remember { mutableStateMapOf<String, Boolean>() }
    val currentTime = remember { mutableStateMapOf<String, Float>() }

    if (announcements.isNotEmpty()) {
        val announcement = announcements[0] // Utiliza el primer anuncio de la lista
        // Extrae el ID del video de la URL
        val videoId = extractYouTubeVideoId(announcement.youtubevideo) ?: ""
        Log.d("VideosYoutubeMasVideos", "Loading YouTube video with ID: $videoId")

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ÚLTIMO SERMÓN",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = VspMarco, // Asegúrate de tener definido VspMarco o usa un color existente aquí
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            // Verifica que el ID del video no esté vacío antes de intentar mostrar el video
            if (videoId.isNotEmpty()) {
                YouTubeVideoView(videoId = videoId, isPlaying = isPlayingState, currentTime = currentTime)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón "Ver más sermones"
            Button(
                onClick = {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse("https://www.youtube.com/@vidaensupalabra3623")
                    context.startActivity(openURL)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VspMarcoTransparente50, // Asegúrate de tener definido VspMarcoTransparente50 o usa un color existente aquí
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Ver más sermones",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun ExpandibleContenidopastor(
    iconResourceId: Int,
    title: String,
    isExpanded: Boolean,
    onExpand: () -> Unit
) {
    // Envuelve todo el contenido de Column en un Modifier.clickable
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onExpand) // Hace clicable toda la columna
    ) {
        Image(
            painter = painterResource(id = iconResourceId),
            contentDescription = title,
            modifier = Modifier.size(118.dp),
            //tint = VspMarco
        )

        // Icono de expansión y título en la misma Row para que estén uno al lado del otro
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.Add,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = VspMarco
            )

            Spacer(modifier = Modifier.width(2.dp))

            androidx.compose.material3.Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(color = VspMarco),
            )
        }
    }
}

@Composable
fun ExpandirContent(title: String, content: String) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VspMarcoTransparente)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.material3.Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(color = VspMarco),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            androidx.compose.material3.Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
@Composable
fun IconRow(facebookUrl: String, instagramUrl: String, twitterUrl: String) {
    val context = LocalContext.current // Obtiene el contexto actual

    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        // Icono de Facebook
        IconButton(onClick = { openUrl(context, facebookUrl) }) {
            Icon(painter = painterResource(drawable.icon_facebook), contentDescription = "Facebook", tint = VspMarcoTransparente50)
        }
        // Icono de Instagram
        IconButton(onClick = { openUrl(context, instagramUrl) }) {
            Icon(painter = painterResource(drawable.icon_instagram), contentDescription = "Instagram", tint = VspMarcoTransparente50)
        }
        // Icono de Twitter
        IconButton(onClick = { openUrl(context, twitterUrl) }) {
            Icon(painter = painterResource(drawable.icon_twitter), contentDescription = "Twitter", tint = VspMarcoTransparente50)
        }
    }
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }
    context.startActivity(intent)
}

@Composable
fun Ministerios_asociados(storychurkUrl: String, acts29Url: String, pillarnetworkUrl: String) {
    val context = LocalContext.current // Obtiene el contexto actual

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MINISTERIOS ASOCIADOS",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Storychurk Image Button
        Box(modifier = Modifier
            .clickable { openUrl(context, storychurkUrl) }) {
            Image(
                painter = painterResource(id = drawable.storychurk),
                contentDescription = "Storychurk",
                modifier = Modifier.fillMaxSize()
                    .height(60.dp), // Usa fillMaxSize para llenar el Box
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Acts29 Image Button
        Box(modifier = Modifier
            .clickable { openUrl(context, acts29Url) }) {
            Image(
                painter = painterResource(id = drawable.acts29),
                contentDescription = "Acts29",
                modifier = Modifier.fillMaxSize()
                    .height(60.dp), // Usa fillMaxSize para llenar el Box
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // PillarNetwork Image Button
        Box(modifier = Modifier
            .clickable { openUrl(context, pillarnetworkUrl) }) {
            Image(
                painter = painterResource(id = drawable.pillarnetwork),
                contentDescription = "PillarNetwork",
                modifier = Modifier.fillMaxSize()
                    .height(60.dp), // Usa fillMaxSize para llenar el Box
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}
@Composable
fun EquipoPastoral() {
    var expandedEduardo by remember { mutableStateOf(false) }
    var expandedAndres by remember { mutableStateOf(false) }

    Text(
        text = "EQUIPO PASTORAL",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
        color = VspMarco,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            ExpandibleContenidopastor(
                iconResourceId = drawable.andrescuadroscaled,
                title = "Andrés Aguilar",
                isExpanded = expandedAndres
            ) {
                expandedAndres = !expandedAndres
                if (expandedAndres) { // Si se está expandiendo Andrés, contraer Eduardo.
                    expandedEduardo = false
                }
            }

            ExpandibleContenidopastor(
                iconResourceId = drawable.eduardocuadradoscaled,
                title = "Eduardo Fergusson",
                isExpanded = expandedEduardo
            ) {
                expandedEduardo = !expandedEduardo
                if (expandedEduardo) { // Si se está expandiendo Eduardo, contraer Andrés.
                    expandedAndres = false
                }
            }
        }

        if (expandedAndres) {
            ExpandirContent(
                title = "Andrés Aguilar",
                content = "Licenciado en Estudios Teológicos del Seminario Reformado Latinoamericano. Pastor de ICVSP desde 2013, actualmente a tiempo completo. Casado con Yuli Fragozo y padre de Samuel y Abbie."
            )
            IconRow(
                "https://www.facebook.com/andresaguidav/",
                "https://www.instagram.com/andresaguidav/",
                "https://twitter.com/PrAndresAguilar"
            )
        }

        if (expandedEduardo) {
            ExpandirContent(
                title = "Eduardo Fergusson",
                content = "Egresado del Seminario Reformado Latinoamericano, cursa una maestría en divinidades en el Covenant Baptist Theological Seminary. Pastor de ICVSP desde 2013 y Coordinador de traducciones para Coalición por el Evangelio. Casado con Etna Brito y padre de Emma e Ethan."
            )
            IconRow(
                "https://www.facebook.com/eduarfergusson",
                "https://www.instagram.com/ejfergusson/",
                "https://twitter.com/ejfergusson"
            )
        }
    }
}
@Composable
fun Horarios(viewModel: MainViewModel = viewModel()){
    val announcements = viewModel.anuncios
    if (announcements.isNotEmpty()) {
        announcements.forEach { announcement ->

            Text(
                text = "NUESTROS HORARIOS",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold // Aplica negrita al texto
                ),
                color = White,
                textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
            )

            Card(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 4.dp,
                backgroundColor = VspBase
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente los elementos dentro de esta columna
                ) {
                    Text(
                        text = announcement.titulo,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold // Aplica negrita al texto
                        ),
                        color = White,
                        textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = announcement.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = announcement.fecha,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                    )
                }
            }
            Card(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 4.dp,
                backgroundColor = VspBase
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente los elementos dentro de esta columna
                ) {
                    Text(
                        text = announcement.titulo2,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold // Aplica negrita al texto
                        ),
                        color = White,
                        textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = announcement.descripcion2,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = announcement.fecha2,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                    )

                }
            }
        }
    } else {
        Text(
            text = "NUESTROS HORARIOS",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold // Aplica negrita al texto
            ),
            color = White,
            textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
        )

        Card(

            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = 4.dp,
            backgroundColor = VspBase
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente los elementos dentro de esta columna
            ) {
                Text(
                    text = "DOMINGO",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold // Aplica negrita al texto
                    ),
                    color = White,
                    textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CULTO DE ADORACIÓN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "8:30 AM & 10:30 AM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                )
            }
        }
        Card(

            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = 4.dp,
            backgroundColor = VspBase
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente los elementos dentro de esta columna
            ) {
                Text(
                    text = "MIÉRCOLES",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold // Aplica negrita al texto
                    ),
                    color = White,
                    textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "REUNIÓN DE ORACIÓN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "6:30 PM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                )

            }
        }
    }
}