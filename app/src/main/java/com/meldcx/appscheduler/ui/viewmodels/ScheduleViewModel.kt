package com.meldcx.appscheduler.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.data.repositories.DataRepository
import com.meldcx.appscheduler.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(private val dataRepository: DataRepository): ViewModel() {

    private var _schedules = MutableLiveData<List<Schedule>>()
    val schedules: LiveData<List<Schedule>> get() = _schedules

    private var _status = MutableLiveData<Int>()
    val status: LiveData<Int> get() = _status

    init {
        viewModelScope.launch {
            _schedules.value = dataRepository.fetchSchedules()
        }
    }

    fun handleScheduleDeletion(context: Context, schedule: Schedule, pos: Int) {
        viewModelScope.launch {
            dataRepository.deleteSchedule(schedule.timeInMilli)
            Utility.cancelScheduledAppLaunch(context, schedule.packageName)
            _status.value = pos
        }
    }
}