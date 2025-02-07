package com.meldcx.appscheduler.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meldcx.appscheduler.utils.Constants
import java.util.UUID

@Entity(tableName = Constants.SCHEDULE_TABLE_NAME)
data class Schedule(
    @PrimaryKey
    val timeInMilli: Long,
    val packageName: String,
    val workId: UUID
)
