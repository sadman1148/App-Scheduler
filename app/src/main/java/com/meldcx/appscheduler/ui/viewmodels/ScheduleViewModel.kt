package com.meldcx.appscheduler.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.data.repositories.DataRepository
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

    private var _status = MutableLiveData<Int>()
    val status: LiveData<Int> get() = _status

    init {
        viewModelScope.launch {
            _schedules.value = dataRepository.fetchSchedules()
        }
    }

    fun handleScheduleDeletion(schedule: Schedule, pos: Int) {
        viewModelScope.launch {
            dataRepository.deleteSchedule(schedule.timeInMilli)
            WorkManager.getInstance(application.applicationContext).cancelWorkById(schedule.workId)
            _status.value = pos
        }
    }
}