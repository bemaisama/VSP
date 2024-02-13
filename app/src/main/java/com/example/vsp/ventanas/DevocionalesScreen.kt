package com.example.vsp.ventanas

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ContentAlpha
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vsp.ArdeEntity
import com.example.vsp.ui.theme.VspMarcoTransparente50
import com.example.vsp.ui.theme.White

@Composable
fun DevocionalScreen(arde: ArdeEntity?, onSave: (ArdeEntity) -> Unit, onClose: () -> Unit) {
    // Obtiene el contexto actual dentro del composable.
    val context = LocalContext.current
    // Este composable maneja el botón de retroceso en este punto del árbol de composición
    BackHandler {
        onClose()
    }

    if (arde == null) {
        androidx.compose.material.Text("No ARDE data available. Please select a date.")
        return
    }

    // Ahora usamos `devocional` en lugar de `reference`
    var devocional by rememberSaveable { mutableStateOf(arde.devocional) }

    Column(modifier =
    Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        // Mostramos la referencia como un texto estático, ya que no necesitamos editarla aquí
        androidx.compose.material.Text(
            "A.R.D.E.: ${arde.reference}",
            style = MaterialTheme.typography.headlineMedium,
            color = White,

        )

        Spacer(modifier = Modifier.height(8.dp))

        // El TextField ahora está vinculado a `devocional`
        androidx.compose.material.TextField(
            textStyle = MaterialTheme.typography.bodyMedium,
            value = devocional,
            onValueChange = { newValue -> devocional = newValue },
            label = { androidx.compose.material.Text("Escribe tu devocional aquí", color = White) },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = VspMarcoTransparente50, // Fondo transparente para el TextField
                cursorColor = White,
                disabledTextColor = White,
                textColor = White, // Color del texto
                focusedLabelColor = Color.Transparent, // Color del label cuando el TextField está enfocado
                unfocusedLabelColor = White.copy(alpha = ContentAlpha.medium), // Color del label cuando el TextField no está enfocado
                focusedIndicatorColor = White, // Color del marco cuando el TextField está enfocado
                unfocusedIndicatorColor = White.copy(alpha = ContentAlpha.disabled) // Color del marco cuando el TextField no está enfocado
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                onSave(arde.copy(devocional = devocional))
                Toast.makeText(context, "Guardando ARDE...", Toast.LENGTH_SHORT).show()
            }) {
                androidx.compose.material.Text("Guardar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onClose) {
                androidx.compose.material.Text("Cerrar")
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                // Lógica para compartir el devocional
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "A.R.D.E.: ${arde.reference}\n\n $devocional")
                    type = "text/plain"
                }
                val shareIntentChooser = Intent.createChooser(shareIntent, null)
                context.startActivity(shareIntentChooser)
            }) {
                androidx.compose.material.Text("Compartir")
            }

        }
    }
}
