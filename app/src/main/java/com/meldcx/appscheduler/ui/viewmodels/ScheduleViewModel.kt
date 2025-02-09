package com.meldcx.appscheduler.ui.viewmodels

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.data.repositories.DataRepository
import com.meldcx.appscheduler.utils.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val application: Application,
    private val dataRepository: DataRepository
) : ViewModel() {

    private var _schedules = MutableLiveData<List<Schedule>>()
    val schedules: LiveData<List<Schedule>> get() = _schedules

    private var _deleteStatus = MutableLiveData<Int>()
    val deleteStatus: LiveData<Int> get() = _deleteStatus

    private var _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> get() = _updateStatus

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _schedules.value = dataRepository.fetchSchedules()
        }
    }

    fun handleScheduleDeletion(schedule: Schedule, pos: Int) {
        viewModelScope.launch {
            deleteAndCancelAlarm(schedule)
            _deleteStatus.value = pos
        }
    }

    fun handleScheduleUpdate(time: Long, schedule: Schedule) {
        viewModelScope.launch {
            deleteAndCancelAlarm(schedule)
            insertSchedule(Schedule(time, schedule.packageName))
        }
    }

    private suspend fun deleteAndCancelAlarm(schedule: Schedule) {
        dataRepository.deleteSchedule(schedule.timeInMilli)
        cancelAlarm(schedule)
    }

    private fun insertSchedule(schedule: Schedule) {
        viewModelScope.launch {
            TimeUtil.scheduleAppLaunch(application.applicationContext, schedule.packageName, schedule.timeInMilli)
            dataRepository.insertScheule(schedule)
            fetchData()
        }
    }

    private fun cancelAlarm(schedule: Schedule) {
        (application.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(
            PendingIntent.getBroadcast(
                application.applicationContext,
                schedule.timeInMilli.toInt(),
                Intent(application.applicationContext.packageManager.getLaunchIntentForPackage(schedule.packageName)),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}