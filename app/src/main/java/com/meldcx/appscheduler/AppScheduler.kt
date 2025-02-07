package com.meldcx.appscheduler

import android.app.Application
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AppScheduler : Application() {

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}