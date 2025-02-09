package com.meldcx.appscheduler.di

import com.meldcx.appscheduler.data.daos.ScheduleDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootReceiverEntryPoint {
    fun scheduleDao(): ScheduleDao
}