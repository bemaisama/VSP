package com.vidaensupalabra.vsp.notificaciones

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.vidaensupalabra.vsp.MainActivity
import com.vidaensupalabra.vsp.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val ardeReference = inputData.getString("ardeReference") ?: return Result.failure()
        val message = "Este es el ARDE del día: $ardeReference"

        LocalNotificationService(applicationContext).showNotification(title, message)

        // Reprogramar la siguiente ejecución
        scheduleDailyNotifications(applicationContext, ardeReference)

        return Result.success()
    }
}

class LocalNotificationService(private val context: Context) {

    fun showNotification(title: String, message: String) {
        val notificationId = 101

        // Verificar permisos en Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Si el permiso no está concedido, se debería solicitar al usuario. Aquí solo retornamos.
                return
            }
        }

        val builder = NotificationCompat.Builder(context, "mi_canal_id")
            .setSmallIcon(R.drawable.ic_stat_vsp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}

fun scheduleWeeklyNotification(context: Context) {
    val workManager = WorkManager.getInstance(context)

    val now = Calendar.getInstance()
    val firstTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            add(Calendar.DAY_OF_YEAR, 1)
        }

        if (before(now)) {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
    }

    val initialDelay = firstTime.timeInMillis - now.timeInMillis

    val weeklyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(7, TimeUnit.DAYS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("title" to "Recordatorio", "message" to "Recuerda Alistarte \npara el día del Señor!"))
        .build()

    workManager.enqueueUniquePeriodicWork(
        "weeklyNotification",
        ExistingPeriodicWorkPolicy.KEEP,
        weeklyWorkRequest
    )
}


fun scheduleDailyNotifications(context: Context, ardeReference: String) {
    val workManager = WorkManager.getInstance(context)

    val now = Calendar.getInstance()

    // Configurar la primera notificación a las 6 am
    val firstNotificationTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(now)) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    val firstInitialDelay = firstNotificationTime.timeInMillis - now.timeInMillis

    val firstNotificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(firstInitialDelay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("title" to "A.R.D.E", "ardeReference" to ardeReference))
        .build()

    // Configurar la segunda notificación a las 7 pm
    val secondNotificationTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 19)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(now)) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    val secondInitialDelay = secondNotificationTime.timeInMillis - now.timeInMillis

    val secondNotificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(secondInitialDelay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("title" to "Recordatorio de la Tarde", "ardeReference" to ardeReference))
        .build()

    // Programar los WorkRequest
    workManager.enqueueUniqueWork(
        "dailyNotification6am",
        ExistingWorkPolicy.REPLACE,
        firstNotificationWorkRequest
    )

    workManager.enqueueUniqueWork(
        "dailyNotification7pm",
        ExistingWorkPolicy.REPLACE,
        secondNotificationWorkRequest
    )
}
