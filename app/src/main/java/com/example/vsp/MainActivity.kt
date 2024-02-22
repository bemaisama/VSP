package com.example.vsp

import DonacionScreen
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.vsp.ui.theme.VSPTheme
import com.example.vsp.ui.theme.VspBase
import com.example.vsp.ui.theme.VspMarco
import com.example.vsp.ui.theme.White
import com.example.vsp.ventanas.ARDEScreen
import com.example.vsp.ventanas.DevocionalScreen
import com.example.vsp.ventanas.HomeScreen
import com.example.vsp.ventanas.InformationScreen
import com.example.vsp.ventanas.Mas
import com.example.vsp.ventanas.MusicaScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Construye la notificación
        val builder = NotificationCompat.Builder(this, "mi_canal_id")
            .setSmallIcon(R.drawable.ic_stat_vsp) // Reemplaza esto con tu drawable de notificación
            .setContentTitle(remoteMessage.notification?.title ?: "Anuncio nuevo")
            .setContentText(remoteMessage.notification?.body ?: "Hay un Cambio Nuevo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Crea un intent que se dispare cuando la notificación es presionada
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        builder.setContentIntent(pendingIntent)

        // Muestra la notificación
        with(NotificationManagerCompat.from(this)) {
            notify(101, builder.build()) // 101 es el ID de la notificación
        }
    }
}

// Entidades y acceso a datos (Room)
@Entity(tableName = "arde_data")
data class ArdeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val month: Int,
    val day: Int,
    val reference: String,
    val devocional: String // Nueva variable añadida

)

// Define el DAO para acceder a tu base de datos.
@Dao
interface ArdeDao {
    @Query("SELECT * FROM arde_data")
    fun getAll(): List<ArdeEntity>

    @Insert
    fun insertAll(vararg ardes: ArdeEntity)

    @Query("SELECT COUNT(*) FROM arde_data")
    fun count(): Int

    @Query("SELECT * FROM arde_data WHERE year = :year AND month = :month AND day = :day")
    suspend fun findByDate(year: Int, month: Int, day: Int): List<ArdeEntity>

    @Update
    suspend fun updateArde(arde: ArdeEntity)

}

// Define la base de datos Room.
@Database(entities = [ArdeEntity::class], version = 2) // Aumenta la versión aquí
abstract class AppDatabase : RoomDatabase() {
    abstract fun ardeDao(): ArdeDao
}

// ViewModels
class ArdeViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = Room.databaseBuilder(application, AppDatabase::class.java, "arde_database")
        .fallbackToDestructiveMigration()
        .build()

    val currentArde: MutableLiveData<ArdeEntity?> = MutableLiveData(null)
    val navigationEvent = MutableSharedFlow<String>()

    init {
        checkAndLoadData()
    }

    private fun checkAndLoadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = db.ardeDao().count() // Esta operación ya está en un contexto seguro gracias a Dispatchers.IO
            if (count == 0) {
                val ardeList = loadArdeDataFromCsv(getApplication<Application>().applicationContext)
                if (ardeList.isNotEmpty()) {
                    db.ardeDao().insertAll(*ardeList.toTypedArray())
                    Log.d("ArdeViewModel", "Data loaded successfully from CSV: ${ardeList.size} records")
                } else {
                    Log.d("ArdeViewModel", "No data found in CSV or error reading CSV")
                }
            } else {
                Log.d("ArdeViewModel", "Database already contains data. No need to load from CSV.")
            }
        }
    }

    // Función para cargar datos de ARDE basados en la fecha seleccionada.
    fun loadArdeDataForSelectedDate(year: Int, month: Int, day: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val ardeData = withContext(Dispatchers.IO) {
                db.ardeDao().findByDate(year, month, day).firstOrNull()
            }
            currentArde.postValue(ardeData)
            if (ardeData != null) {
                navigationEvent.emit("arde_detail/${ardeData.year}/${ardeData.month}/${ardeData.day}")
            }
        }
    }

    fun updateArde(arde: ArdeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.ardeDao().updateArde(arde)
        }
    }

    init {
        checkAndLoadData()
    }

    private suspend fun loadArdeDataFromCsv(context: Context): List<ArdeEntity> = withContext(Dispatchers.IO) {
        val ardeList = mutableListOf<ArdeEntity>()
        context.assets.open("Datos_Arde.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line!!.split(",")
                    if (tokens.size >= 4) { // Asegura que hay al menos 4 tokens por línea
                        val year = tokens[0].toInt()
                        val month = tokens[1].toInt()
                        val day = tokens[2].toInt()
                        val reference = tokens[3]
                        val devocional = "" // Inicializa el campo devocional con una cadena vacía
                        ardeList.add(ArdeEntity(year = year, month = month, day = day, reference = reference, devocional = devocional))
                    }
                }
            }
        }
        return@withContext ardeList
    }
    // Método para limpiar el estado de currentArde

}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MainViewModel"
        private const val PREFS_NAME = "com.example.vsp.prefs"
        private const val LAST_SYNC_TIME = "last_sync_time"
    }
    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var lastSyncTime: Long
        get() = sharedPreferences.getLong(LAST_SYNC_TIME, 0)
        set(value) = sharedPreferences.edit().putLong(LAST_SYNC_TIME, value).apply()
    private val db = FirebaseFirestore.getInstance()
    var anuncios = mutableStateListOf<ImportantAnnouncement>()

    init {
        leerAnuncios()
    }

    private fun leerAnuncios() {
        val db = FirebaseFirestore.getInstance()
        // Esta consulta recupera todos los anuncios, independientemente de su lastUpdated
        db.collection("anuncios").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            Log.d(TAG, "Fetched ${snapshots?.size()} announcements")

            if (snapshots != null && !snapshots.isEmpty) {
                // Limpia la lista actual para evitar duplicados
                anuncios.clear()

                // Itera sobre los documentos y los añade a la lista
                for (document in snapshots.documents) {
                    val anuncio = document.toObject(ImportantAnnouncement::class.java)
                    if (anuncio != null) {
                        anuncios.add(anuncio)
                        Log.d(TAG, "Announcement added: ${anuncio.youtubevideo}")

                    }
                }
            }
        }
    }

    private val _canciones = MutableLiveData<List<Cancion>>()
    val canciones: LiveData<List<Cancion>> = _canciones

    init {
        cargarCanciones()
    }

    private fun cargarCanciones() {
        db.collection("canciones").addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(TAG, "Error al escuchar cambios en Firestore", error)
                return@addSnapshotListener
            }

            val cancionesList = ArrayList<Cancion>()
            for (doc in value!!) {
                val cancion = doc.toObject(Cancion::class.java)
                cancionesList.add(cancion)
            }
            _canciones.value = cancionesList
        }
    }
}


// ComponentActivity y Composables
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mi Canal de Notificación" // Nombre descriptivo del canal
            val descriptionText = "Descripción de Mi Canal de Notificación"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("mi_canal_id", name, importance).apply {
                description = descriptionText
            }
            // Registrar el canal con el sistema
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        setContent {
            VSPTheme {
                // La inicialización del ViewModel se mantiene, pero ya no pasamos el contexto explícitamente.
                MainScreen()
            }

        }
    }
}



@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            // Usamos el valor hexadecimal para el color de fondo
            color = VspBase // Cambiamos aquí el color del fondo
        ) {
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("Inicio", painterResource(id = R.drawable.church), Screens.Home.route),
        NavigationItem("A.R.D.E", painterResource(id = R.drawable.arde), Screens.ARDE.route),
        NavigationItem("Canciones", painterResource(id = R.drawable.musica), Screens.Musica.route),
        NavigationItem("Información", painterResource(id = R.drawable.informacion), Screens.Information.route),
        NavigationItem("Mas", painterResource(id = R.drawable.menu), Screens.Mas.route)
    )

    BottomNavigation(
        backgroundColor = VspMarco,
        contentColor = White // Suponiendo que quieres usar el color blanco para el texto/icons
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp) // Tamaño estándar para los íconos
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        maxLines = 1,
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
                selectedContentColor = VspBase,
                unselectedContentColor = White,
                alwaysShowLabel = true, // Asegura que siempre se muestren las etiquetas
                modifier = Modifier.padding(0.dp) // Remueve cualquier padding adicional
            )
        }
    }
}


// Enum para definir las pantallas, asegúrate de que estas rutas coincidan con las definidas en NavigationItem
enum class Screens(val route: String) {
    Home("home"),
    ARDE("arde"),
    Musica("musica"),
    Information("information"),
    Mas("mas"),
    Donacion("donacion"),

}

// NavigationGraph
@Composable
fun NavigationGraph(navController: NavHostController) {
    val mainViewModel: MainViewModel = viewModel() // Asegúrate de obtener la instancia del ViewModel aquí
    NavHost(navController = navController, startDestination = Screens.Home.route) {
        composable(Screens.Home.route) { HomeScreen() }
        composable(Screens.ARDE.route) { ARDEScreen(navController = navController) }
        composable(Screens.Musica.route) { MusicaScreen(viewModel = mainViewModel) }
        composable(Screens.Information.route) { InformationScreen() }
        composable(Screens.Mas.route) { Mas(navController = navController) }
        composable("arde_detail/{year}/{month}/{day}") { backStackEntry ->
            val year = backStackEntry.arguments?.getString("year")?.toInt() ?: 0
            val month = backStackEntry.arguments?.getString("month")?.toInt() ?: 0
            val day = backStackEntry.arguments?.getString("day")?.toInt() ?: 0

            // Asumiendo que tienes una forma de obtener el ArdeEntity a partir de la fecha.
            val viewModel: ArdeViewModel = viewModel()
            LaunchedEffect(key1 = Unit) {
                viewModel.loadArdeDataForSelectedDate(year, month, day)
            }
            val ardeEntity = viewModel.currentArde.observeAsState().value

            DevocionalScreen(
                arde = ardeEntity,
                onSave = { updatedArde ->
                    viewModel.updateArde(updatedArde)
                },
                onClose = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screens.Donacion.route){ DonacionScreen(navController= navController) }

    }
}


@Composable
fun TextWithStyleFromFirestore(text: String) {
    // Preprocesamos el texto para reemplazar "/n" con el salto de línea "\n"
    val processedText = text.replace("/n", "\n")

    val styledText = buildAnnotatedString {
        var startIndex: Int
        var endIndex: Int
        var currentIndex = 0

        while (currentIndex < processedText.length) {
            startIndex = processedText.indexOf("*", currentIndex)
            if (startIndex == -1) {
                append(processedText.substring(currentIndex, processedText.length))
                break // No more bold text, append the rest and exit
            }
            endIndex = processedText.indexOf("*", startIndex + 1)
            if (endIndex == -1) {
                append(processedText.substring(currentIndex, processedText.length))
                break // No closing tag, append the rest and exit
            }

            // Text before bold
            if (startIndex > currentIndex) {
                append(processedText.substring(currentIndex, startIndex))
            }

            // Bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = White)) {
                append(processedText.substring(startIndex + 1, endIndex))
            }

            currentIndex = endIndex + 1
        }
    }

    Text(text = styledText, color = White, style = MaterialTheme.typography.bodyMedium)
}

// Modelos de datos y Enums

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
    val titulo1: String = "",
    val artista1: String = "",
    val letra1: String = "",
    val titulo2: String = "",
    val artista2: String = "",
    val letra2: String = "",
    val titulo3: String = "",
    val artista3: String = "",
    val letra3: String = ""
)