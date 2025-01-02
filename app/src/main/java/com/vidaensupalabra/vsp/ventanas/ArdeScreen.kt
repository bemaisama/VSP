// ArdeScreen.kt

package com.vidaensupalabra.vsp.ventanas

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries,UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vidaensupalabra.vsp.ArdeViewModel
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.White
import java.util.Calendar

@Composable
fun ARDEScreen(
    viewModel: ArdeViewModel = viewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = VspBase
        ) {
            Column(modifier = Modifier.fillMaxSize()
            ) {

                // Columna principal para el contenido que incluye desplazamiento
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {

                    MethodArdeDescriptionCard()
                }

                 Text(
                     modifier = Modifier .padding(6.dp),
                    text="Seleccione una fecha para ver el ARDE.",
                    style = MaterialTheme.typography.titleLarge,
                    color =  White,
                    textAlign = TextAlign.Center // Asegura que el texto esté centrado dentro de su contenedor

                 )
                 YearSelectionButtons(context, viewModel,navController)
            }
        }
    }
}
@Composable
fun MethodArdeDescriptionCard() {
    var textState by rememberSaveable { mutableStateOf("El método ARDE es una herramienta teológica centrada en Cristo diseñada para el estudio y la meditación de la Biblia, estructurada en cuatro componentes esenciales:\n\n" +
            "Atributos de Dios (A): Se enfoca en explorar y reflexionar sobre las características y cualidades de Dios reveladas en la Escritura, como su santidad, amor, justicia, omnipresencia, entre otras, con el fin de profundizar en el conocimiento de quién es Dios.\n\n" +
            "Relación con Cristo (R): Examina cómo el texto en estudio se relaciona o conecta con Jesucristo, su vida, ministerio, enseñanzas, muerte y resurrección. Este aspecto busca identificar la presencia y cumplimiento de Cristo en toda la Escritura, resaltando su centralidad en el plan de salvación.\n\n" +
            "Deberes (D): Se refiere a las enseñanzas prácticas y los mandamientos para la vida del creyente extraídos del texto, abordando cómo vivir de acuerdo a la voluntad de Dios y en obediencia a sus mandatos, siguiendo el ejemplo de Jesús.\n\n" +
            "Esperanza (E): Se centra en la esperanza cristiana manifestada en las promesas de Dios, incluyendo la salvación, la vida eterna, y la restauración final de todas las cosas a través de Cristo. Este componente alienta a los creyentes a vivir con una perspectiva esperanzadora basada en las garantías divinas.\n\n" +
            "El método ARDE ofrece así un enfoque integral para el estudio bíblico, invitando a los creyentes a una comprensión más profunda y aplicada de su fe, siempre con Jesucristo en el centro de la reflexión y la práctica.") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp,
        backgroundColor = VspBase // Asegúrate de que VspBase es el color deseado para el fondo del Card
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Información sobre A.R.D.E",
                style = MaterialTheme.typography.headlineMedium,
                color = White,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center // Asegura que el texto esté centrado dentro de su contenedor

            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material.TextField(
                readOnly = true,
                value = textState,
                onValueChange = { textState = it },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent, // Fondo transparente para el TextField
                    cursorColor = White,
                    disabledTextColor = White,
                    textColor = White // Asegúrate de ajustar el color del texto según necesites
                )
            )
        }
    }
}

@Composable
fun YearSelectionButtons(context: Context, viewModel: ArdeViewModel, navController: NavHostController) {
    // Observar eventos de navegación
    LocalLifecycleOwner.current
    LaunchedEffect(key1 = viewModel) {
        viewModel.navigationEvent.collect { route ->
            navController.navigate(route)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(1, 2, 3).forEach { planYear ->
            Button(onClick = {
                showDatePickerDialogForArde(context, planYear) { year, month, day ->
                    viewModel.loadArdeDataForSelectedDate(year, month, day)
                }
            }) {
                Text("Año $planYear")
            }
        }
    }
}

fun showDatePickerDialogForArde(context: Context, planYear: Int, onDateSelected: (Int, Int, Int) -> Unit) {
    // El DatePickerDialog aquí es solo para seleccionar mes y día. El año es fijado por el botón de "Año" presionado
    DatePickerDialog(context, { _, _, month, dayOfMonth ->
        onDateSelected(planYear, month + 1, dayOfMonth)
    }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show()
}



