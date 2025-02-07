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
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.meldcx.appscheduler.data.models.App
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.data.repositories.DataRepository
import com.meldcx.appscheduler.utils.Constants
import com.meldcx.appscheduler.utils.Utility
import com.meldcx.appscheduler.workers.AppLaunchWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
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
                val id = scheduleAppLaunch(app.packageName, time - System.currentTimeMillis())
                dataRepository.insertScheule(Schedule(time, app.packageName, id))
            }
        }
    }

    private fun scheduleAppLaunch(packageName: String, delayInMillis: Long): UUID {
        val workData = workDataOf(Constants.WORM_PACKAGE_NAME_KEY to packageName)

        val workRequest = OneTimeWorkRequestBuilder<AppLaunchWorker>()
            .setInitialDelay(delayInMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(application.applicationContext).enqueue(workRequest)
        return workRequest.id
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