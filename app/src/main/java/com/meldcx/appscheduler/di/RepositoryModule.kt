package com.meldcx.appscheduler.di

import com.meldcx.appscheduler.data.daos.ScheduleDao
import com.meldcx.appscheduler.data.repositories.DataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideDataRepository(scheduleDao: ScheduleDao): DataRepository {
        return DataRepository(scheduleDao)
    }
}