package com.example.vsp.ventanas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vsp.MainViewModel
import com.example.vsp.TextWithStyleFromFirestore
import com.example.vsp.ui.theme.VspBase
import com.example.vsp.ui.theme.VspMarco
import com.example.vsp.ui.theme.White

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
            item { CancionCard(titulo = cancion.titulo1, artista = cancion.artista1, letra = cancion.letra1) }
            item { CancionCard(titulo = cancion.titulo2, artista = cancion.artista2, letra = cancion.letra2) }
            item { CancionCard(titulo = cancion.titulo3, artista = cancion.artista3, letra = cancion.letra3) }
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
                // Usamos TextWithStyleFromFirestore para mostrar la letra con estilo
                TextWithStyleFromFirestore(letra)
            }
        }
    }
}

