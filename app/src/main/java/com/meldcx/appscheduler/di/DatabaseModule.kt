package com.meldcx.appscheduler.di

import android.content.Context
import androidx.room.Room
import com.meldcx.appscheduler.data.daos.ScheduleDao
import com.meldcx.appscheduler.data.databases.ScheduleDatabase
import com.meldcx.appscheduler.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context): ScheduleDatabase {
        return Room.databaseBuilder(context, ScheduleDatabase::class.java, Constants.APP_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideScheduleDao(db: ScheduleDatabase): ScheduleDao {
        return db.scheduleDao()
    }
}