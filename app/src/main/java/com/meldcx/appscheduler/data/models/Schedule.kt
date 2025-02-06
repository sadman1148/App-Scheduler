package com.meldcx.appscheduler.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meldcx.appscheduler.utils.Constants

@Entity(tableName = Constants.SCHEDULE_TABLE_NAME)
data class Schedule(
    @PrimaryKey
    val timeInMilli: Long,
    val packageName: String
)
