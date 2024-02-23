
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vsp.ui.theme.VspBase
import com.example.vsp.ui.theme.VspMarco
import com.example.vsp.ui.theme.VspMarcoTransparente50
import com.example.vsp.ui.theme.White
import com.vidaensupalabra.vsp.R

@Composable
fun DonacionScreen(navController: NavController) {
    val cuentaAhorro = "526-805680-49"
    val nit = "901104161-3"
    val codigoSwift = "COLOCOBM"
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
                        text = "Donación",
                        color = White,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
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
            Text(
                text = "Para realizar una donación de manera virtual, puedes hacerlo desde tu plataforma bancaria a nuestra cuenta de ahorro, usando los siguientes datos para la transacción",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = White
            )
            // Corrección: Pasar las variables como parámetros a la función
            BanColombia(cuentaAhorro = cuentaAhorro, nit = nit, codigoSwift = codigoSwift)
            Textocorreo()
        }
    }
}

// Corrección: Cambio de nombre y adición de parámetro
@Composable
fun BanColombia(cuentaAhorro: String, nit: String, codigoSwift: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically // Asegúrate de que los elementos estén centrados verticalmente
    ) {
        Image(
            painter = painterResource(id = R.drawable.logobancolombia), // Reemplaza con el ID de recurso correcto
            contentDescription = "Logo de Bancolombia",
            modifier = Modifier.height(150.dp)
                .size(120.dp),
            // Ajusta según sea necesario

        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            ReadOnlyTextField(value = cuentaAhorro, label = "Cuenta de Ahorro Bancolombia:")
            ReadOnlyTextField(value = nit, label = "NIT:")
            ReadOnlyTextField(value = codigoSwift, label = "Código Swift:")
        }
    }
}

@Composable
fun ReadOnlyTextField(value: String, label: String) {
    androidx.compose.material.TextField(
        value = value,
        onValueChange = {}, // No se permite modificar el valor
        label = { Text(text = label, style = MaterialTheme.typography.bodyMedium, color = White) },
        readOnly = true, // Campo de solo lectura
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(VspMarcoTransparente50), // Ajusta el color de fondo según necesites
        colors = androidx.compose.material.TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent, // Hace el fondo del TextField transparente
            disabledLabelColor = VspMarco, // Color del label cuando está deshabilitado (solo lectura)
            disabledTextColor = VspMarco, // Color del texto cuando está deshabilitado (solo lectura)
            textColor = VspMarco
        )
    )
}

@Composable
fun Textocorreo() {
    val annotatedText = buildAnnotatedString {
        append("Si deseas reportar tus donaciones para fines tributarios, envía copia de las transacciones que realices al correo: ")
        // Aquí aplicamos un estilo para hacer negrita al correo
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("contabilidad@vidaensupalabra.com")
        }
        append("\n\nPor este mismo correo podrás solicitar tu certificación para fines tributarios.\n\n")
    }

    Text(
        text = annotatedText,
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        color = White
    )
}
