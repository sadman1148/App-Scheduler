package com.meldcx.appscheduler.ui.listeners

import com.meldcx.appscheduler.data.models.App

interface AppClickListener {
    fun onAppClick(app: App)
}