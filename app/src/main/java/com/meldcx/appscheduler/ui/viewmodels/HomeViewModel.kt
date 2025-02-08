package com.meldcx.appscheduler.ui.viewmodels

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meldcx.appscheduler.R
import com.meldcx.appscheduler.data.models.App
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.data.repositories.DataRepository
import com.meldcx.appscheduler.receivers.AlarmReceiver
import com.meldcx.appscheduler.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val dataRepository: DataRepository) : ViewModel() {

    private var _appList = MutableLiveData<List<App>>()
    val appList: LiveData<List<App>> get() = _appList

    private var _toastObserver = MutableLiveData<String>()
    val toastObserver: LiveData<String> get() = _toastObserver

    init {
        fetchApps()
    }

    fun verifySchedule(time: Long, app: App) {
        viewModelScope.launch {
            if (dataRepository.checkIfScheduleExists(time)) {
                _toastObserver.value = application.getString(R.string.app_scheduled_warning)
            } else {
                scheduleAppLaunch(app.packageName, time)
                dataRepository.insertScheule(Schedule(time, app.packageName))
                _toastObserver.value = "${app.name} scheduled for launch"
            }
        }
    }

    @SuppressLint("MissingPermission") // alternative permission added
    private fun scheduleAppLaunch(packageName: String, time: Long) {
        val context = application.applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(Constants.PACKAGE_NAME_KEY, packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            time.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }

    fun fetchApps() {
        application.applicationContext.let { context ->
            _appList.value = extractAppData(getInstalledApps(context), context)
        }
    }

    private fun getInstalledApps(context: Context): List<ResolveInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            pm.queryIntentActivities(mainIntent, 0)
        }
    }

    private fun extractAppData(appResolveInfoList: List<ResolveInfo>, context: Context): List<App> {
        val appList = mutableListOf<App>()
        for (appResolveInfo in appResolveInfoList) {
            val pm = context.packageManager
            with(appResolveInfo) {
                val resources = pm.getResourcesForApplication(activityInfo.applicationInfo)
                appList.add(
                    App(
                        if (activityInfo.labelRes != 0) {
                            resources.getString(activityInfo.labelRes) // getting label from resources
                        } else {
                            activityInfo.applicationInfo.loadLabel(pm).toString() // getting it out of app info
                        },
                        activityInfo.packageName,
                        activityInfo.loadIcon(pm)
                    )
                )
            }
        }
        return appList
    }
}