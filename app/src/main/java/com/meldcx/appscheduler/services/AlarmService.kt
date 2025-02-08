package com.meldcx.appscheduler.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.meldcx.appscheduler.utils.Constants

class AlarmService: AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { }

    override fun onInterrupt() { }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra(Constants.PACKAGE_NAME_KEY)
        if (packageName != null) {
            launchApp(packageName)
        }
        return START_STICKY
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}