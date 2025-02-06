package com.meldcx.appscheduler.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meldcx.appscheduler.data.models.App
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.data.repositories.DataRepository
import com.meldcx.appscheduler.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val dataRepository: DataRepository) : ViewModel() {

    private var _appList = MutableLiveData<List<App>>()
    val appList: LiveData<List<App>> get() = _appList

    private var _toastObserver = MutableLiveData<Boolean>()
    val toastObserver: LiveData<Boolean> get() = _toastObserver

    init {
        fetchApps()
    }

    fun verifySchedule(time: Long, app: App) {
        viewModelScope.launch {
            if (dataRepository.checkIfScheduleExists(time)) {
                _toastObserver.value = true
            } else {
                dataRepository.insertScheule(Schedule(time, app.packageName))
                Utility.scheduleAppLaunch(application.applicationContext, app.packageName, time)
            }
        }
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