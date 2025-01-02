// MainActivity.kt
package com.vidaensupalabra.vsp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vidaensupalabra.vsp.notificaciones.scheduleNotifications
import com.vidaensupalabra.vsp.notificaciones.scheduleWeeklyNotification
import com.vidaensupalabra.vsp.otros.checkForUpdate
import com.vidaensupalabra.vsp.otros.getCurrentArdeReference
import com.vidaensupalabra.vsp.room.AppDatabase
import com.vidaensupalabra.vsp.room.ArdeEntity
import com.vidaensupalabra.vsp.ui.theme.VSPTheme
import com.vidaensupalabra.vsp.ui.theme.VspBase
import com.vidaensupalabra.vsp.ui.theme.VspMarco
import com.vidaensupalabra.vsp.ui.theme.White
import com.vidaensupalabra.vsp.ventanas.ARDEScreen
import com.vidaensupalabra.vsp.ventanas.DevocionalScreen
import com.vidaensupalabra.vsp.ventanas.DonacionScreen
import com.vidaensupalabra.vsp.ventanas.HomeScreen
import com.vidaensupalabra.vsp.ventanas.InformationScreen
import com.vidaensupalabra.vsp.ventanas.Mas
import com.vidaensupalabra.vsp.ventanas.MultimediaScreen
import com.vidaensupalabra.vsp.ventanas.MusicaScreen
import com.vidaensupalabra.vsp.viewmodels.ArdeViewModel
import com.vidaensupalabra.vsp.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SecureWebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                // Manejar el error SSL según tu criterio
                handler?.proceed()
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://www.example.com")
    }
}

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val builder = NotificationCompat.Builder(this, "mi_canal_id")
            .setSmallIcon(R.drawable.ic_stat_vsp)
            .setContentTitle(remoteMessage.notification?.title ?: "Anuncio nuevo")
            .setContentText(remoteMessage.notification?.body ?: "Hay un Cambio Nuevo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)
        with(NotificationManagerCompat.from(this)) {
            notify(101, builder.build())
        }
    }
}


// ------------------ MainActivity -------------------- //

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    private lateinit var developerOptionsButton: Button
    private var handler = Handler(Looper.getMainLooper())
    private var isHeldDown = false
    private lateinit var ardeViewModel: ArdeViewModel

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos
        requestStoragePermissions()
        initializeDatabase()

        // Crear canal de notificación
        createNotificationChannel()

        setContent {
            VSPTheme {
                MainScreen(mainActivity = this)
            }
        }
        Log.d("MainActivity", "onCreate called")

        ardeViewModel = ViewModelProvider(this).get(ArdeViewModel::class.java)
        ardeViewModel.dataLoaded.observe(this, Observer { dataLoaded ->
            if (dataLoaded) {
                lifecycleScope.launch {
                    val ardeReference = getCurrentArdeReference(this@MainActivity)
                    Log.d("MainActivity", "Scheduling notifications for ARDE_REFERENCE: $ardeReference")
                    scheduleNotifications(this@MainActivity)
                    scheduleWeeklyNotification(this@MainActivity)
                }
            }
        })

        // Verificar actualizaciones
        lifecycleScope.launch {
            val currentVersion = BuildConfig.VERSION_NAME
            val downloadPath = "${filesDir}/update.apk"
            val updateUrl = checkForUpdate(currentVersion)
            Log.d("MainActivity", "Update URL: $updateUrl")
            if (updateUrl != null) {
                showUpdateDialog(updateUrl, downloadPath)
            } else {
                Log.i("MainActivity", "No updates available.")
            }
        }

        // Botón para abrir diálogo de desarrollador
        developerOptionsButton = Button(this).apply {
            text = "Opciones de Desarrollador"
            isEnabled = false
            visibility = View.GONE
            setOnClickListener {
                startActivity(Intent(this@MainActivity, DeveloperOptionsActivity::class.java))
            }
        }
        addContentView(
            developerOptionsButton,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
    }

    private fun initializeDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            Log.d("MainActivity", "Database initialized: $db")
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.REQUEST_INSTALL_PACKAGES
                ), 1
            )
            Log.d("MainActivity", "Storage permissions requested")
        }
    }

    private fun showUpdateDialog(updateUrl: String, downloadPath: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nueva versión disponible")
        builder.setMessage("Hay una nueva versión disponible. ¿Deseas actualizar ahora?")
        builder.setPositiveButton("Actualizar") { _, _ ->
            val intent = Intent(this, DownloadActivity::class.java).apply {
                putExtra("downloadUrl", updateUrl)
                putExtra("outputPath", downloadPath)
            }
            startActivityForResult(intent, 1235)
        }
        builder.setNegativeButton("Más tarde", null)
        builder.show()
        Log.d("MainActivity", "Update dialog shown")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mi Canal de Notificación"
            val descriptionText = "Descripción de Mi Canal de Notificación"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("mi_canal_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("MainActivity", "Notification channel created")
        }
    }

    private fun installApk(apkPath: String) {
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            Log.e("MainActivity", "APK file does not exist: $apkPath")
            return
        }
        val apkUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            apkFile
        )
        Log.d("MainActivity", "APK URI: $apkUri")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: SecurityException) {
            Log.e("MainActivity", "SecurityException during installation: ${e.message}")
            e.printStackTrace()
            handleInstallationError("SecurityException: ${e.message}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting install activity: ${e.message}")
            e.printStackTrace()
            handleInstallationError("Error: ${e.message}")
        }
    }

    private fun handleInstallationError(errorMessage: String) {
        Log.e("MainActivity", "Installation failed: $errorMessage")
        AlertDialog.Builder(this)
            .setTitle("Error de instalación")
            .setMessage("La instalación falló: $errorMessage")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun startInstallIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting install intent: ${e.message}")
            e.printStackTrace()
        }
    }

    @Deprecated("Use registerForActivityResult(...) instead.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            if (resultCode == RESULT_OK) {
                val downloadPath = "${filesDir}/update.apk"
                val apkUri = FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    File(downloadPath)
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    startInstallIntent(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error after permission granted: ${e.message}")
                    handleInstallationError("Error: ${e.message}")
                }
            } else {
                Log.e("MainActivity", "Permission for installing unknown apps not granted")
                handleInstallationError("Permission not granted for unknown apps")
            }
        } else if (requestCode == 1235 && resultCode == RESULT_OK) {
            val outputPath = data?.getStringExtra("outputPath")
            if (outputPath != null) {
                try {
                    installApk(outputPath)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error installing APK: ${e.message}")
                    handleInstallationError("Error: ${e.message}")
                }
            }
        }
    }

    // Manejo de long press
    fun handleLongPressStart() {
        isHeldDown = true
        handler.postDelayed({
            if (isHeldDown) {
                developerOptionsButton.isEnabled = true
                developerOptionsButton.visibility = View.VISIBLE
                Toast.makeText(this, "Opciones de Desarrollador activadas", Toast.LENGTH_SHORT).show()
            }
        }, 10_000)
    }

    fun handleLongPressEnd() {
        isHeldDown = false
        handler.removeCallbacksAndMessages(null)
    }
}

// ------------------ Composables M3 -------------------- //

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainActivity: MainActivity) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = VspBase
        ) {
            NavigationGraph(navController = navController, mainActivity = mainActivity)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("Inicio", painterResource(id = R.drawable.church), Screens.Home.route),
        NavigationItem("A.R.D.E", painterResource(id = R.drawable.arde), Screens.ARDE.route),
        NavigationItem("Canciones", painterResource(id = R.drawable.musica), Screens.Musica.route),
        NavigationItem("Información", painterResource(id = R.drawable.informacion), Screens.Information.route),
        NavigationItem("Mas", painterResource(id = R.drawable.menu), Screens.Mas.route)
    )

    // NavigationBar es el equivalente M3 a BottomNavigation
    NavigationBar(
        containerColor = VspMarco,
        contentColor = White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        maxLines = 1
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VspBase,
                    unselectedIconColor = White,
                    selectedTextColor = VspBase,
                    unselectedTextColor = White
                )
            )
        }
    }
}

// Rutas enum
enum class Screens(val route: String) {
    Home("home"),
    ARDE("arde"),
    Musica("musica"),
    Information("information"),
    Mas("mas"),
    Donacion("donacion"),
    Multimedia("multimedia"),
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(navController: NavHostController, mainActivity: MainActivity) {
    val mainViewModel: MainViewModel = viewModel()
    NavHost(navController = navController, startDestination = Screens.Home.route) {
        composable(Screens.Home.route) { HomeScreen() }
        composable(Screens.ARDE.route) { ARDEScreen(navController = navController) }
        composable(Screens.Musica.route) { MusicaScreen(viewModel = mainViewModel) }
        composable(Screens.Information.route) { InformationScreen() }
        composable(Screens.Mas.route) { Mas(navController, mainActivity) }
        composable("arde_detail/{year}/{month}/{day}") { backStackEntry ->
            val year = backStackEntry.arguments?.getString("year")?.toInt() ?: 0
            val month = backStackEntry.arguments?.getString("month")?.toInt() ?: 0
            val day = backStackEntry.arguments?.getString("day")?.toInt() ?: 0
            val viewModel: ArdeViewModel = viewModel()
            LaunchedEffect(Unit) {
                viewModel.loadArdeDataForSelectedDate(year, month, day)
            }
            val ardeEntity = viewModel.currentArde.observeAsState().value
            DevocionalScreen(
                arde = ardeEntity,
                onSave = { updatedArde: ArdeEntity ->
                    viewModel.updateArde(updatedArde)
                },
                onClose = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screens.Donacion.route) { DonacionScreen() }
        composable(Screens.Multimedia.route) { MultimediaScreen() }
    }
}

// Modelos data
data class NavigationItem(val title: String, val icon: Painter, val route: String)
data class ImportantAnnouncement(
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val titulo2: String = "",
    val descripcion2: String = "",
    val fecha2: String = "",
    val youtubevideo: String = "",
)
data class Cancion(
    val letra1: String = "",
    val letra2: String = "",
    val letra3: String = "",
    val youtubevideo1: String = "",
    val youtubevideo2: String = "",
    val youtubevideo3: String = "",
)
