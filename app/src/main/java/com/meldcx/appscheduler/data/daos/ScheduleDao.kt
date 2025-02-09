package com.meldcx.appscheduler.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meldcx.appscheduler.data.models.Schedule

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insertSchedule(schedule: Schedule)

    @Query("SELECT EXISTS (SELECT 1 FROM schedules WHERE timeInMilli = :time)")
    suspend fun checkIfScheduleExists(time: Long): Boolean

    @Query("SELECT * FROM schedules")
    suspend fun fetchSchedules(): List<Schedule>

    @Query("DELETE FROM schedules WHERE timeInMilli = :time")
    suspend fun deleteSchedule(time: Long)

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedules(): List<Schedule>
}