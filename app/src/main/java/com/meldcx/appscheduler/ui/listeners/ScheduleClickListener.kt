package com.meldcx.appscheduler.ui.listeners

import com.meldcx.appscheduler.data.models.Schedule

interface ScheduleClickListener {
    fun onScheduleClick(schedule: Schedule, pos: Int)
}