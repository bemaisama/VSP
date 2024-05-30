package com.vidaensupalabra.vsp.notificaciones

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
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
        val title = inputData.getString("title") ?: "Notificación"
        val message = inputData.getString("message") ?: "Mensaje de notificación"
        LocalNotificationService(applicationContext).showNotification(title, message)
        return Result.success()
    }
}

class LocalNotificationService(private val context: Context) {

    fun showNotification(title: String, message: String) {
        val notificationId = 101

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
        .setInputData(workDataOf("title" to "Recordatorio Semanal", "message" to "¡Es domingo a las 7 AM!"))
        .build()

    workManager.enqueueUniquePeriodicWork(
        "weeklyNotification",
        ExistingPeriodicWorkPolicy.REPLACE,
        weeklyWorkRequest
    )
}

fun scheduleTestNotification(context: Context, delayInSeconds: Long) {
    val workManager = WorkManager.getInstance(context)

    val testWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
        .setInputData(workDataOf("title" to "Prueba de Notificación", "message" to "Esta es una notificación de prueba."))
        .build()

    workManager.enqueue(testWorkRequest)
}
