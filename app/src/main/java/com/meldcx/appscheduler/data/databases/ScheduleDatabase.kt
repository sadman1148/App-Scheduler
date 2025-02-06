package com.meldcx.appscheduler.data.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meldcx.appscheduler.data.daos.ScheduleDao
import com.meldcx.appscheduler.data.models.Schedule

@Database(
    entities = [Schedule::class],
    version = 1
)
abstract class ScheduleDatabase: RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
}