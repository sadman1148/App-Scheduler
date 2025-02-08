package com.meldcx.appscheduler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meldcx.appscheduler.services.AlarmService
import com.meldcx.appscheduler.utils.Constants
import com.meldcx.appscheduler.utils.TimeUtil
import timber.log.Timber

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            it.getStringExtra(Constants.PACKAGE_NAME_KEY)?.let{ packageName ->
                Timber.d("onReceive() > alarm for $packageName received at: ${TimeUtil.parseTime(System.currentTimeMillis())}")
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra(Constants.PACKAGE_NAME_KEY, packageName)
                }
                context.startService(serviceIntent)
            }
        }
    }
}