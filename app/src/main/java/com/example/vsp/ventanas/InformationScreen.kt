package com.example.vsp.ventanas

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.vsp.R
import com.example.vsp.ui.theme.VspBase
import com.example.vsp.ui.theme.VspMarco
import com.example.vsp.ui.theme.VspMarcoTransparente
import com.example.vsp.ui.theme.VspMarcoTransparente50
import com.example.vsp.ui.theme.White


@Composable
fun InformationScreen() {
    val scrollState = rememberScrollState()
    // Variables para controlar la expansión de "Nuestra Misión" y "Nuestra Visión"
    var expandedMission by remember { mutableStateOf(false) }
    var expandedVision by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = VspBase) {
        Column(

            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState),
        ) {
            TextoNuestraIglesia()
            Spacer(modifier = Modifier.height(16.dp))
            // Usamos un Row para colocar "Nuestra Misión" y "Nuestra Visión" uno al lado del otro
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                ExpandibleContenido(
                    iconResourceId = R.drawable.mision_vsp,
                    title = "Nuestra Misión",
                    isExpanded = expandedMission
                ) { expandedMission = !expandedMission }

                ExpandibleContenido(
                    iconResourceId = R.drawable.flag_vsp,
                    title = "Nuestra Visión",
                    isExpanded = expandedVision
                ) { expandedVision = !expandedVision }
            }
            // Nueva sección de creencias
            // Tarjetas de detalles expandidas debajo de "Nuestra Misión" y "Nuestra Visión"
            if (expandedMission) {
                ExpandedContent(
                    title = "Nuestra Misión",
                    content = "Hacer crecer la iglesia de Cristo en nuestra ciudad, proclamando y enseñando Su palabra, para traer esperanza y transformación de vida a nuestras familias."
                )
            }

            if (expandedVision) {
                ExpandedContent(
                    title = "Nuestra Visión",
                    content = "Ser conocidos en nuestra ciudad como una iglesia que sirve como Jesús lo hace, fundamentados en Sus palabras, interesados en las personas y sus necesidades, y con un compromiso total por la gloria de Dios y Su reino."
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Hitosvsp()
            Spacer(modifier = Modifier.height(16.dp))
            CreenciasSection()
            Spacer(modifier = Modifier.height(16.dp))
            BotonConfecionDeFe()
        }
    }
}

@Composable
fun Hitosvsp() {
    Spacer(modifier = Modifier.height(24.dp))
    BasicText(text = "HITOS", style = MaterialTheme.typography.headlineSmall.copy(color = White, fontWeight = FontWeight.Bold))
    Spacer(modifier = Modifier.height(8.dp))

    // Los hitos son mejor representados en una lista, aquí se simula una
    val hitos = listOf(
        "En 2013, inicia nuestra comunidad en un estudio bíblico en casa.",
        "En 2014, escogimos el nombre de nuestra comunidad y establecimos nuestra membresía.",
        "En 2015, se ordenaron nuestros pastores de manera oficial por la membresía.",
        "En 2017, recibimos nuestra personería jurídica como iglesia cristiana ante el Estado.",
        "En 2018, nos vinculamos a ACTS 29 para ser parte de esta red de plantación de Iglesias.",
        "Actualmente, nuestro deseo es seguir trabajando para la gloria de Dios."
    )

    // Iteramos sobre la lista de hitos para mostrar cada uno con su icono
    hitos.forEach { hito ->
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tiempo),
                contentDescription = null, // Descripción opcional para accesibilidad
                modifier = Modifier.size(24.dp),
                tint = VspMarco
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = hito,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CreenciasSection() {
    Spacer(modifier = Modifier.height(24.dp))
    BasicText(
        text = "LO QUE CREEMOS",
        style = MaterialTheme.typography.headlineSmall.copy(
            color = White,
            fontWeight = FontWeight.Bold
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    // Aquí puedes detallar las creencias
    BasicText(
        text = "Somos una iglesia cristiana de confesión Bautista Reformada, por lo que reconocemos en el documento de la Confesión de Londres de 1689, un trabajo que resume nuestros fundamentos doctrinales de fe y práctica para todo lo somos y lo que hacemos como iglesia.\n" +
                "\n" +
                "Dentro de los principales distintivos doctrinales que nos identifican, podemos resumirlos de la siguiente manera:\n" +
                "\n",
        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
    )
    // Agrega más BasicText para cada creencia o considera un enfoque diferente si son muchas
    ExpandibleCreencia(
        title = "DE LAS ESCRITURAS",
        description = "Creemos que la Biblia es la Palabra de Dios, compuesta por 39 libros en el Antiguo Testamento y 27 en el Nuevo Testamento, que fue escrita por hombres divinamente inspirados y que es un tesoro perfecto de instrucción celestial. Creemos que la Biblia tiene a Dios mismo por autor, por objeto la salvación y por contenido la verdad sin mezcla alguna de error. Creemos que en la Biblia se revela la voluntad divina y los principios según los cuales Dios nos juzgará; siendo por lo mismo, y habiendo de serlo hasta el final de los tiempos, centro verdadero de la unión cristiana y norma suprema a la cual debe sujetarse todo juicio que se forme de la conducta, las creencias y las opiniones humanas."
    )
    ExpandibleCreencia(
        title = "DE DIOS",
        description = "Creemos que hay un solo Dios viviente y verdadero, infinito, Espíritu inteligente, Creador y Gobernador supremo del cielo y de la tierra, indeciblemente glorioso en santidad; merecedor de toda la honra, confianza y amor. Creemos que en la unidad de la divinidad existen tres personas, el Padre, el Hijo, y el Espíritu Santo, iguales estos en toda perfección divina y que desempeñan oficios distintos que armonizan en la gran obra de la redención, revelada en las Sagradas Escrituras."
    )
    ExpandibleCreencia(
        title = "DEL SER HUMANO",
        description = "Creemos que los seres humanos fueron creados, tanto el hombre como la mujer, a la imagen de Dios, igualmente dignos, en santidad y sujeción a la ley de su Creador. Creemos que por la desobediencia voluntaria al mandato de Dios, cayeron de aquel estado santo y feliz, por lo que ahora toda la humanidad es pecadora, no por fuerza, sino por su voluntad. Es por eso que, hallándose por naturaleza enteramente desprovista de la santidad que requiere la ley de Dios, está totalmente inclinada a lo malo, y por lo mismo bajo justa condenación a ruina eterna, sin defensa ni excusa; estando completamente necesitado del rescate que es por medio de la fe en Jesucristo."
    )
    ExpandibleCreencia(
        title = "DE LA SALVACIÓN",
        description = "Creemos que la salvación del ser humano es completamente por gracia, por fe en la obra redentora de Jesucristo, el Hijo de Dios, quien, por la designación del Padre, libremente tomó sobre sí nuestra naturaleza, pero sin pecado; honró la ley divina con su obediencia personal y con su muerte hizo plena expiación por nuestros pecados, resucitando después de entre los muertos y desde entonces está sentado en su trono en los cielos. Creemos que al creer en la obra de Jesús en la cruz, el pecador arrepentido recibe el perdón y la libertad del poder del pecado, en la condición de una nueva creación, vestida de la justicia de Cristo y con la esperanza eterna de una vida reconciliada y con Dios."
    )
    ExpandibleCreencia(
        title = "DE LA IGLESIA",
        description = "Creemos que la Iglesia es la comunidad de todos los creyentes en el evangelio de Jesucristo de todas las épocas, en todo el mundo, visiblemente establecida en iglesias locales, en las cuales sus miembros son añadidos por medio de una confesión sincera de fe y compromiso con la comunión del evangelio. Creemos que la iglesia es el principal instrumento de Dios a través del cual cumple sus propósitos redentores en la tierra, cuando esta ejerce la comisión que le fue dada por Su Redentor, de predicar el evangelio a todas las naciones."
    )
    ExpandibleCreencia(
        title = "DE LOS ÚLTIMOS TIEMPOS",
        description = "Creemos que Cristo vuelve a juzgar a los vivos y a los muertos. Hasta su regreso, los creyentes deben vivir sus vidas de tal manera que den gloria a Dios a través de Jesucristo. La iglesia debe estar ocupada haciendo el trabajo de la predicación del evangelio (en palabras y en hechos) y del discipulado, proclamando el Evangelio puro de Cristo enseñando la palabra de Dios."
    )

    ExpandibleCreencia(
        title = "DE LA FAMILIA",
        description = "Creemos que Dios creó la familia para ser la primera y fundamental institución humana, en donde a partir de las relaciones personales entre sus miembros, se crea el ambiente ideal para el desarrollo del amor mutuo, el compañerismo, la práctica de principios y valores. Creemos que dicha institución fue diseñada por Dios para estar constituida por la unión matrimonial de un hombre con una mujer, quienes serían responsables del cuidado del hogar y la crianza de los hijos, conforme a las instrucciones y del diseño biológico del Creador, dando a cada miembro de la familia un rol y responsabilidades particulares."
    )

}

@Composable
fun ExpandibleCreencia(title: String, description: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VspMarcoTransparente)
    ) {
        Column(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.Add,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = VspMarco
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(color = White),fontWeight = FontWeight.Bold
                )

            }
            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TextoNuestraIglesia() {
    val text = buildAnnotatedString {
    val showDialog = remember { mutableStateOf(false) }

        BasicText(
            text = "NUESTRA IGLESIA",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )
        painterResource(id = R.drawable.vspsomos)
        Image(
            painter = painterResource(id = R.drawable.vspsomos),
            contentDescription = "Foto de la iglesia",
            modifier = Modifier
                .size(300.dp)
                .padding(vertical = 16.dp)
                .clickable { showDialog.value = true }
        )

        if (showDialog.value) {
            Dialog(onDismissRequest = { showDialog.value = false }) {
                // Box que detecta clics para cerrar el diálogo, sin interceptar los clics en la imagen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            onClick = { showDialog.value = false },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                ) {
                    // Centra la imagen en la pantalla
                    Box(contentAlignment = Alignment.Center) {
                        ImagenFullScreen(painter = painterResource(id = R.drawable.vspsomos))
                    }
                }
            }
        }

        append("Somos una comunidad de discípulos de Cristo que buscan glorificar a Dios, ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
            append("viviendo")
        }
        append(" centrados en el evangelio, ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
            append("proclamando")
        }
        append(" fieles Su palabra y ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
            append("dependiendo")
        }
        append(" en todo del poder de Su Espíritu.")
    }

    BasicText(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, textAlign = TextAlign.Center)
    )
}


@Composable
fun ExpandibleContenido(
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
        Icon(
            painter = painterResource(id = iconResourceId),
            contentDescription = title,
            modifier = Modifier.size(48.dp),
            tint = VspMarco
        )

        // Icono de expansión y título en la misma Row para que estén uno al lado del otro
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.Add,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = VspMarco
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(color = VspMarco),
            )
        }
    }
}

@Composable
fun ExpandedContent(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VspMarcoTransparente)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(color = VspMarco),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun BotonConfecionDeFe (){
    val context = LocalContext.current // Obtiene el contexto actual
    Button(
        onClick = {
            // Acción para abrir el enlace en el navegador
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://vidaensupalabra.com/wp-content/uploads/2023/08/Confesion-de-Fe-Bautista-de-Londres-de-1689-Guia-de-estudio.pdf")
            }
            context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = VspMarcoTransparente50, // Usa tu color personalizado aquí
        )
    ) {
        Text(text = "Descarga la CFBL 1689",  style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),fontWeight = FontWeight.Bold )
    }
}

@Composable
fun ImagenFullScreen(painter: Painter) {
    // Estado para manejar la escala de la imagen
    var scale by remember { mutableFloatStateOf(1f) }
    // Estado para manejar la posición de la imagen
    var offset by remember { mutableStateOf(Offset.Zero) }

    Image(
        painter = painter,
        contentDescription = "Imagen ampliada",
        modifier = Modifier
            .padding(10.dp) // Agrega padding alrededor de la imagen en el diálogo
            .fillMaxSize() // Hace que la imagen llene el espacio disponible
            .graphicsLayer(
                // Aplica la escala y la traslación a la imagen
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
