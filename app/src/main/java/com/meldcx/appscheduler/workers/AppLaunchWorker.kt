package com.meldcx.appscheduler.workers

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.meldcx.appscheduler.utils.Constants

class AppLaunchWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val packageName = inputData.getString(Constants.WORM_PACKAGE_NAME_KEY) ?: return Result.failure()
        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(launchIntent)
        return Result.success()
    }
}