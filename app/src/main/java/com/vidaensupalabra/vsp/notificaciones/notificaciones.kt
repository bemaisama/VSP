package com.vidaensupalabra.vsp.notificaciones

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vidaensupalabra.vsp.MainActivity
import com.vidaensupalabra.vsp.R
import java.util.Calendar

private const val CHANNEL_ID = "ARDE_CHANNEL_ID"
private const val NOTIFICATION_ID = 101

class LocalNotificationService(private val context: Context) {

    fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permiso no concedido
            return
        }

        createNotificationChannelIfNeeded()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_vsp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "ARDE Notifications", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Channel for ARDE notifications"
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

fun scheduleWeeklyNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("ARDE_REFERENCE", "Recuerda Alistarte para el día del Señor!")
    }
    val pendingIntent = PendingIntent.getBroadcast(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY * 7,
        pendingIntent
    )
}

fun scheduleNotifications(context: Context, ardeReference: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("ARDE_REFERENCE", ardeReference)
    }
    val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    scheduleAlarm(alarmManager, pendingIntent, 6, 0)
    scheduleAlarm(alarmManager, pendingIntent, 19, 0)
}

private fun scheduleAlarm(alarmManager: AlarmManager, pendingIntent: PendingIntent, hour: Int, minute: Int) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }
    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ardeReference = intent.getStringExtra("ARDE_REFERENCE") ?: return
        LocalNotificationService(context).showNotification("A.R.D.E.", ardeReference)
    }
}
