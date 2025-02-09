package com.meldcx.appscheduler.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.meldcx.appscheduler.R
import com.meldcx.appscheduler.receivers.AlarmReceiver
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimeUtil(
    private val fragment: Fragment,
    private val onDateTimeSelected: (selectedTimeMillis: Long) -> Unit
) {
    fun showDatePicker() {
        val calendar: Calendar = Calendar.getInstance()
        val context = fragment.requireContext()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val tempCal = calendar.clone() as Calendar
                tempCal.set(selectedYear, selectedMonth, selectedDay)
                if (tempCal.timeInMillis < calendar.timeInMillis) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_choose_a_valid_date),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showTimePicker(tempCal)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .build()

        picker.addOnPositiveButtonClickListener {
            val tempCal = calendar.clone() as Calendar
            tempCal.set(Calendar.MINUTE, picker.minute)
            tempCal.set(Calendar.HOUR_OF_DAY, picker.hour)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)

            val context = fragment.requireContext()
            if (tempCal.timeInMillis < System.currentTimeMillis()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.please_choose_a_valid_time),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                onDateTimeSelected(tempCal.timeInMillis)
            }
        }
        picker.show(fragment.parentFragmentManager, "TimePicker")
    }

    companion object {
        fun parseTime(time: Long): String {
            return SimpleDateFormat("hh:mm a_dd MMM yyyy", Locale.US).format(Date(time))
        }
        @SuppressLint("MissingPermission") // alternative permission added
        fun scheduleAppLaunch(context: Context, packageName: String, time: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(Constants.PACKAGE_NAME_KEY, packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                time.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            Timber.d("scheduleAppLaunch() > alarm scheduled for $packageName at $time")
        }
    }
}