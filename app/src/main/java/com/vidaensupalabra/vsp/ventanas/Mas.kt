package com.vidaensupalabra.vsp.ventanas

//noinspection UsingMaterialAndMaterial3Libraries
import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vidaensupalabra.vsp.BuildConfig
import com.vidaensupalabra.vsp.MainActivity
import com.vidaensupalabra.vsp.Screens
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.VspMarcoTransparente50
import com.vidaensupalabra.vsp.ui.theme.White

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Mas(navController: NavController, mainActivity: MainActivity) {
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
                    modifier = Modifier
                        .fillMaxWidth(), // Asegura que el Box ocupe todo el ancho
                    contentAlignment = Alignment.Center // Centra el contenido del Box
                ) {
                    Text(
                        text = "Vendran Mas Cosas",
                        color = White,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Añade aquí tu TextButton
            TextButton(
                onClick = {
                    // Navega a DonacionScreen al ser presionado
                    navController.navigate(Screens.Donacion.route) // Utiliza la ruta definida en el enum Screens
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = VspMarcoTransparente50), // Usa el color que prefieras para el fondo
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(8.dp) // Ajusta las esquinas redondeadas si es necesario
            ) {
                Text(
                    text = "Donaciones",
                    color = White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }

            TextButton(
                onClick = {
                    // Navega a DonacionScreen al ser presionado
                    navController.navigate(Screens.Multimedia.route) // Utiliza la ruta definida en el enum Screens
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = VspMarcoTransparente50), // Usa el color que prefieras para el fondo
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(8.dp) // Ajusta las esquinas redondeadas si es necesario
            ) {
                Text(
                    text = "Multimedia",
                    color = White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }

            // Añade el nuevo TextButton para "Acerca de esta aplicación"
            TextButton(
                onClick = {
                    // Acción que quieras realizar cuando se presione este botón, si es necesario
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = VspMarcoTransparente50),
                modifier = Modifier
                    .padding(16.dp)
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                mainActivity.handleLongPressStart()
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                mainActivity.handleLongPressEnd()
                                true
                            }
                            else -> false
                        }
                    },
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    Text(
                        text = "Acerca de esta aplicación",
                        color = White,
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Versión: ${BuildConfig.VERSION_NAME}",
                        color = White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
