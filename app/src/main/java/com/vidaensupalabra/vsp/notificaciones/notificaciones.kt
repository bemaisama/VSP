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

        CoroutineScope(Dispatchers.IO).launch {
            val ardeReference = getCurrentArdeReference(context)
            Log.d("NotificationReceiver", "Retrieved ARDE reference: $ardeReference")

            withContext(Dispatchers.Main) {
                val message = intent.getStringExtra("MESSAGE") ?: ardeReference
                Log.d("NotificationReceiver", "Message to show: $message")
                val showBanner = intent.getBooleanExtra("SHOW_BANNER", false)
                showNotification(context, "ARDE", message, showBanner)
            }
        }
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
                    scheduleNotifications(context)
                    scheduleWeeklyNotification(context)
                }
            }
        }
    }
}

fun scheduleWeeklyNotification(context: Context) {
    Log.d("AlarmManager", "scheduleWeeklyNotification called")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val pendingIntent = createPendingIntent(context, "Recuerda Alistarte para el día del Señor!", 101, true)

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

private fun createPendingIntent(context: Context, ardeReference: String, requestCode: Int, showBanner: Boolean = false): PendingIntent {
    val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("MESSAGE", ardeReference)
        putExtra("SHOW_BANNER", showBanner)
        putExtra("ARDE_REFERENCE", ardeReference)
    }
    return PendingIntent.getBroadcast(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

@RequiresApi(Build.VERSION_CODES.S)
fun scheduleNotifications(context: Context) {
    Log.d("AlarmManager", "scheduleNotifications called")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    CoroutineScope(Dispatchers.IO).launch {
        val ardeReference6AM = getCurrentArdeReference(context)
        Log.d("AlarmManager", "Retrieved ardeReference for 6 AM: $ardeReference6AM")
        withContext(Dispatchers.Main) {
            scheduleNotification(context, alarmManager, 6, 0, 102)
        }

        val ardeReference7PM = getCurrentArdeReference(context)
        Log.d("AlarmManager", "Retrieved ardeReference for 7 PM: $ardeReference7PM")
        withContext(Dispatchers.Main) {
            scheduleNotification(context, alarmManager, 19, 0, 103)
        }
    }
}

private fun scheduleNotification(context: Context, alarmManager: AlarmManager, hour: Int, minute: Int, requestCode: Int) {
    val pendingIntent = PendingIntent.getBroadcast(
        context, requestCode, Intent(context, NotificationReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

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
