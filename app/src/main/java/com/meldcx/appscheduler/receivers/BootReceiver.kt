package com.meldcx.appscheduler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meldcx.appscheduler.di.BootReceiverEntryPoint
import com.meldcx.appscheduler.utils.TimeUtil
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            // Accessing the database using EntryPoint
            val appContext = context.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(appContext, BootReceiverEntryPoint::class.java)
            val scheduleDao = entryPoint.scheduleDao()

            // Retrieving stored alarms and rescheduling them
            CoroutineScope(Dispatchers.IO).launch {
                val schedules = scheduleDao.getAllSchedules()
                for (schedule in schedules) {
                    if (schedule.timeInMilli > System.currentTimeMillis()) {
                        Timber.d("onReceive() > rescheduling alarm for ${schedule.packageName} at ${schedule.timeInMilli}")
                        TimeUtil.scheduleAppLaunch(context, schedule.packageName, schedule.timeInMilli)
                    } else {
                        Timber.d("onReceive() > discarding alarm for ${schedule.packageName} at ${schedule.timeInMilli} because time passed")
                    }
                }
            }
        }
    }
}