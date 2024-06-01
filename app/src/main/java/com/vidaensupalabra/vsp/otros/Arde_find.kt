package com.vidaensupalabra.vsp.otros

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.vidaensupalabra.vsp.AppDatabase
import com.vidaensupalabra.vsp.ArdeEntity
import com.vidaensupalabra.vsp.notificaciones.scheduleDailyNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

suspend fun getCurrentArdeReference(context: Context): String {
    return withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Los meses en Calendar empiezan en 0
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val ardeEntity = getArdeEntityForDate(context, year, month, day)
        ardeEntity?.reference ?: "Referencia no encontrada"
    }
}

suspend fun getArdeEntityForDate(context: Context, year: Int, month: Int, day: Int): ArdeEntity? {
    val db = AppDatabase.getInstance(context)
    return db.ardeDao().findByDate(year, month, day).firstOrNull()
}


class NotificationScheduler(private val context: Context, private val lifecycle: Lifecycle) :
    LifecycleObserver {
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun scheduleNotifications() {
        scope.launch {
            val ardeReference = getCurrentArdeReference(context)
            scheduleDailyNotifications(context, ardeReference)
        }
    }
}
