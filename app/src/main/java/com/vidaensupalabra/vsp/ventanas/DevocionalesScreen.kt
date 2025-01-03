// DevocionalesScreen.kt

package com.vidaensupalabra.vsp.ventanas

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.app.AlertDialog
import android.content.Intent
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
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
import androidx.compose.ui.viewinterop.AndroidView
import com.vidaensupalabra.vsp.room.ArdeEntity
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.White

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DevocionalScreen(arde: ArdeEntity?, onSave: (ArdeEntity) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    BackHandler { onClose() }

    if (arde == null) {
        androidx.compose.material.Text("No ARDE data available. Please select a date.")
        return
    }

    var devocional by rememberSaveable { mutableStateOf(arde.devocional) }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        androidx.compose.material.Text(
            "A.R.D.E.: ${arde.reference}",
            style = MaterialTheme.typography.headlineMedium,
            color = White,
        )

        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.material.TextField(
            textStyle = MaterialTheme.typography.bodyMedium,
            value = devocional,
            onValueChange = { newValue -> devocional = newValue },
            label = { androidx.compose.material.Text("Escribe tu devocional aquí", color = White) },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = VspBase,
                cursorColor = White,
                disabledTextColor = White,
                textColor = White,
                focusedLabelColor = Color.Transparent,
                unfocusedLabelColor = White.copy(alpha = ContentAlpha.medium),
                focusedIndicatorColor = White,
                unfocusedIndicatorColor = White.copy(alpha = ContentAlpha.disabled)
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

        Spacer(modifier = Modifier.height(16.dp))
        if (arde.reference != null) {
            BibleWebView(ardeReference = arde.reference)
        }
    }
}

class SafeWebViewClient : WebViewClient() {
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        handler?.cancel() // Cancela la carga de la página cuando hay un error SSL

        // Muestra una alerta al usuario (opcional)
        val context = view?.context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error de seguridad")
        builder.setMessage("Hay un problema con el certificado de seguridad de este sitio. La conexión no es segura.")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request?.url.toString() == "https://finished-loading/") {
            view?.visibility = View.VISIBLE
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val js = """
            setTimeout(function() {
                // Selecciona el contenido deseado
                var desiredContent = document.querySelector('div.ChapterContent_reader__Dt27r');
                
                // Elimina todos los elementos con la clase 'ChapterContent_note'
                var notes = desiredContent.querySelectorAll('.ChapterContent_note__YlDW0');
                notes.forEach(function(note) {
                    note.remove();
                });
                
                // Obtén el contenido HTML del elemento filtrado
                var desiredHtml = desiredContent.outerHTML;
                
                // Selecciona los botones de navegación
                var navigationContainer = document.querySelector('div.w-\\[90vw\\].flex');
                if (navigationContainer) {
                    navigationContainer.style.position = 'relative';
                    navigationContainer.style.bottom = 'auto';
                    var navigationButtons = navigationContainer.outerHTML;
                    
                    // Limpia el body y establece el nuevo contenido
                    document.body.innerHTML =  desiredHtml + navigationButtons;
                    
                    // Añade estilos para fondo transparente y texto blanco
                    document.body.style.backgroundColor = 'transparent';
                    document.body.style.color = 'white';
                    
                    var elements = document.getElementsByTagName('*');
                    for (var i = 0; i < elements.length; i++) {
                        elements[i].style.backgroundColor = 'transparent';
                        elements[i].style.color = 'white';
                    }
                    window.location.href = 'https://finished-loading/';
                }
            }, 1500);
        """
        view?.evaluateJavascript(js, null)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BibleWebView(ardeReference: String) {
    val bookAbbreviation = convertReferenceToAbbreviation(ardeReference)
    if (bookAbbreviation.isEmpty()) {
        return
    }

    val urlBase = "https://www.bible.com/es/bible/103/"
    val completeUrl = "$urlBase$bookAbbreviation.NBLA"

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                visibility = View.INVISIBLE // Hace el WebView inicialmente invisible
                setBackgroundColor(0)  // Hace el fondo transparente

                webViewClient = SafeWebViewClient()

                webChromeClient = WebChromeClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportMultipleWindows(false)
                    javaScriptCanOpenWindowsAutomatically = false
                    safeBrowsingEnabled = true
                }
                loadUrl(completeUrl)
            }
        }
    )
}


// Una función para convertir el nombre del libro y el capítulo a la abreviatura usada en la URL
// Debes expandir esta función para cubrir todos los casos necesarios, este es solo un ejemplo simplificado
fun convertReferenceToAbbreviation(reference: String): String {
    val bookNameToAbbreviationMap = mapOf(
        "Génesis" to "GEN",
        "Éxodo" to "EXO",
        "Levítico" to "LEV",
        "Números" to "NUM",
        "Deuteronomio" to "DEU",
        "Josué" to "JOS",
        "Jueces" to "JDG",
        "Rut" to "RUT",
        "1 Samuel" to "1SA",
        "2 Samuel" to "2SA",
        "1 Reyes" to "1KI",
        "2 Reyes" to "2KI",
        "1 Crónicas" to "1CH",
        "2 Crónicas" to "2CH",
        "Esdras" to "EZR",
        "Nehemías" to "NEH",
        "Ester" to "EST",
        "Job" to "JOB",
        "Salmo" to "PSA",
        "Proverbios" to "PRO",
        "Eclesiastés" to "ECC",
        "Cantares" to "SNG",
        "Isaías" to "ISA",
        "Jeremías" to "JER",
        "Lamentaciones" to "LAM",
        "Ezequiel" to "EZK",
        "Daniel" to "DAN",
        "Oseas" to "HOS",
        "Joel" to "JOL",
        "Amós" to "AMO",
        "Abdías" to "OBA",
        "Jonás" to "JON",
        "Miqueas" to "MIC",
        "Nahúm" to "NAM",
        "Habacuc" to "HAB",
        "Sofonías" to "ZEP",
        "Hageo" to "HAG",
        "Zacarías" to "ZEC",
        "Malaquías" to "MAL",
        "Mateo" to "MAT",
        "Marcos" to "MRK",
        "Lucas" to "LUK",
        "Juan" to "JHN",
        "Hechos" to "ACT",
        "Romanos" to "ROM",
        "1 Corintios" to "1CO",
        "2 Corintios" to "2CO",
        "Gálatas" to "GAL",
        "Efesios" to "EPH",
        "Filipenses" to "PHP",
        "Colosenses" to "COL",
        "1 Tesalonicenses" to "1TH",
        "2 Tesalonicenses" to "2TH",
        "1 Timoteo" to "1TI",
        "2 Timoteo" to "2TI",
        "Tito" to "TIT",
        "Filemón" to "PHM",
        "Hebreos" to "HEB",
        "Santiago" to "JAS",
        "1 Pedro" to "1PE",
        "2 Pedro" to "2PE",
        "1 Juan" to "1JN",
        "2 Juan" to "2JN",
        "3 Juan" to "3JN",
        "Judas" to "JUD",
        "Apocalipsis" to "REV"
    )

    // Extracción del nombre del libro y capítulo desde la referencia
    val regex = Regex("([\\d\\s\\w]+) (\\d+)")
    val matchResult = regex.find(reference)

    // Si no encuentra una coincidencia válida, retorna una cadena vacía
    if (matchResult == null) return ""

    val (bookName, chapter) = matchResult.destructured

    val abbreviation = bookNameToAbbreviationMap[bookName.trim()] ?: return ""
    return "$abbreviation.$chapter"
}

