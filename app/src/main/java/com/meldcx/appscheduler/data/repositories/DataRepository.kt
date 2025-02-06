package com.meldcx.appscheduler.data.repositories

import com.meldcx.appscheduler.data.daos.ScheduleDao
import com.meldcx.appscheduler.data.models.Schedule
import javax.inject.Inject

class DataRepository @Inject constructor(private val scheduleDao: ScheduleDao) {

    suspend fun insertScheule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule)
    }

    suspend fun checkIfScheduleExists(time: Long): Boolean {
        return scheduleDao.checkIfScheduleExists(time)
    }

    suspend fun fetchSchedules(): List<Schedule> {
        return scheduleDao.fetchSchedules()
    }

    suspend fun deleteSchedule(time: Long) {
        scheduleDao.deleteSchedule(time)
    }
}