package com.vidaensupalabra.vsp.notificaciones

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.vidaensupalabra.vsp.MainActivity
import com.vidaensupalabra.vsp.R
import com.vidaensupalabra.vsp.otros.getCurrentArdeReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

const val CHANNEL_ID = "ARDE_CHANNEL_ID"
private const val PERMISSION_REQUEST_CODE = 1001

class NotificationListener : NotificationListenerService() {

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed: ${sbn.notification}")
        cancelNotification(sbn.key)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "Notification posted: ${sbn.notification}")
    }

    companion object {
        private const val TAG = "NotificationListener"
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "onReceive called")
        val ardeReference = intent.getStringExtra("ARDE_REFERENCE") ?: "No ARDE reference found"
        val message = intent.getStringExtra("MESSAGE") ?: ardeReference
        val showBanner = intent.getBooleanExtra("SHOW_BANNER", false)
        showNotification(context, "ARDE", message, showBanner)
    }

    private fun showNotification(context: Context, title: String, message: String, showBanner: Boolean) {
        Log.d("NotificationReceiver", "showNotification called with title: $title, message: $message")
        val channelId = "ARDE_CHANNEL_ID"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "ARDE Notifications", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Channel for ARDE notifications"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationReceiver", "Notification channel created")
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_vsp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (showBanner) {
            val bannerBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.banner)
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bannerBitmap))
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder.setContentIntent(pendingIntent)

        notificationManager.notify(101, notificationBuilder.build())
        Log.d("NotificationReceiver", "Notification shown")
    }
}

class BootReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "onReceive called")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val ardeReference = getCurrentArdeReference(context)
                Log.d("BootReceiver", "ardeReference obtained: $ardeReference")
                withContext(Dispatchers.Main) {
                    scheduleNotifications(context, ardeReference)
                    scheduleWeeklyNotification(context)
                }
            }
        }
    }
}

fun scheduleWeeklyNotification(context: Context) {
    Log.d("AlarmManager", "scheduleWeeklyNotification called")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val pendingIntent = createPendingIntent(context, "Recuerda Alistarte para el día del Señor!", 1, true)

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 7) // Si ya pasó el domingo actual, ajustar para la próxima semana
        }
    }

    Log.d("AlarmManager", "Alarm set for: ${calendar.time}")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (canScheduleExactAlarms(context)) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("AlarmManager", "Exact alarm scheduled")
        } else {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
            Log.d("AlarmManager", "Inexact repeating alarm scheduled")
        }
    } else {
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
        Log.d("AlarmManager", "Inexact repeating alarm scheduled")
    }
}

private fun createPendingIntent(context: Context, message: String, requestCode: Int, showBanner: Boolean = false): PendingIntent {
    val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("MESSAGE", message)
        putExtra("SHOW_BANNER", showBanner)
    }
    return PendingIntent.getBroadcast(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

@RequiresApi(Build.VERSION_CODES.S)
fun scheduleNotifications(context: Context, ardeReference: String) {
    Log.d("AlarmManager", "scheduleNotifications called with ardeReference: $ardeReference")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    scheduleNotification(context, alarmManager, ardeReference, 6, 0, 1)
    scheduleNotification(context, alarmManager, ardeReference, 19, 0, 2)
}

private fun scheduleNotification(context: Context, alarmManager: AlarmManager, ardeReference: String, hour: Int, minute: Int, requestCode: Int) {
    val pendingIntent = createPendingIntent(context, ardeReference, requestCode)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (canScheduleExactAlarms(context)) {
            scheduleAlarmExact(alarmManager, pendingIntent, hour, minute)
        } else {
            scheduleAlarm(alarmManager, pendingIntent, hour, minute)
        }
    } else {
        scheduleAlarm(alarmManager, pendingIntent, hour, minute)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun canScheduleExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val canSchedule = try {
        alarmManager.canScheduleExactAlarms()
    } catch (e: SecurityException) {
        false
    }
    Log.d("AlarmManager", "canScheduleExactAlarms: $canSchedule")
    return canSchedule
}

private fun scheduleAlarm(alarmManager: AlarmManager, pendingIntent: PendingIntent, hour: Int, minute: Int) {
    Log.d("AlarmManager", "scheduleAlarm called for time: $hour:$minute")
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1) // Asegurar que sea para el siguiente día si la hora ya pasó hoy
        }
    }
    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
    Log.d("AlarmManager", "Alarm scheduled for: ${calendar.time}")
}

private fun scheduleAlarmExact(alarmManager: AlarmManager, pendingIntent: PendingIntent, hour: Int, minute: Int) {
    Log.d("AlarmManager", "scheduleAlarmExact called for time: $hour:$minute")
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1) // Asegurar que sea para el siguiente día si la hora ya pasó hoy
        }
    }
    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
    Log.d("AlarmManager", "Exact alarm scheduled for: ${calendar.time}")
}
