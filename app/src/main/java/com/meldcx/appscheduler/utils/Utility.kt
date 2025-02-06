package com.meldcx.appscheduler.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.meldcx.appscheduler.services.AlarmService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utility {

    fun scheduleAppLaunch(context: Context, packageName: String, triggerTimeInMillis: Long) {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Android 12+ (No permission needed)
                val alarmInfo = AlarmManager.AlarmClockInfo(triggerTimeInMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmInfo, pendingIntent)
            }
            else -> {
                // for Android 6+ (API 23+)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeInMillis,
                    pendingIntent
                )
            }
        }

        // Start foreground service
        val serviceIntent = Intent(context, AlarmService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

    }

    fun cancelScheduledAppLaunch(context: Context, packageName: String) {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

    fun parseTime(time: Long): String {
        return SimpleDateFormat("hh:mm a_dd MMM yyyy", Locale.US).format(Date(time))
    }
}