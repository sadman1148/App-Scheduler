package com.meldcx.appscheduler.ui.listeners

import com.meldcx.appscheduler.data.models.Schedule

interface ScheduleClickListener {
    fun onScheduleDeleteClick(schedule: Schedule, pos: Int)
    fun onScheduleEditClick(schedule: Schedule)
}