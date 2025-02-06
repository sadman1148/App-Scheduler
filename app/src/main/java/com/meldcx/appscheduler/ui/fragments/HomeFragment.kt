package com.meldcx.appscheduler.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.meldcx.appscheduler.R
import com.meldcx.appscheduler.data.models.App
import com.meldcx.appscheduler.databinding.FragmentHomeBinding
import com.meldcx.appscheduler.ui.listeners.AppClickListener
import com.meldcx.appscheduler.ui.adapters.AppAdapter
import com.meldcx.appscheduler.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class HomeFragment : Fragment(), AppClickListener {

    private lateinit var binding: FragmentHomeBinding
    private val appAdapter by lazy {
        AppAdapter(this)
    }
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            with(rvApps) {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = appAdapter
            }
            btnRetry.setOnClickListener {
                tvProgress.visibility = View.VISIBLE
                progress.visibility = View.VISIBLE
                vm.fetchApps()
            }
            vm.appList.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    tvProgress.visibility = View.GONE
                    progress.visibility = View.GONE
                    tvPlaceHolder.visibility = View.GONE
                    btnRetry.visibility = View.GONE
                    appAdapter.addApps(it)
                } else {
                    tvPlaceHolder.visibility = View.VISIBLE
                    btnRetry.visibility = View.VISIBLE
                }
            }
            vm.toastObserver.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    "An app is already scheduled at that time",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showDatePicker(app: App) {
        val calendar: Calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val tempCal = calendar.clone() as Calendar
                tempCal.set(selectedYear, selectedMonth, selectedDay)
                if (tempCal.timeInMillis < calendar.timeInMillis) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_choose_a_valid_date),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showTimePicker(tempCal, app)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar, app: App) {
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
            if (tempCal.timeInMillis < System.currentTimeMillis()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_choose_a_valid_time),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                vm.verifySchedule(tempCal.timeInMillis, app)
            }
        }
        picker.show(parentFragmentManager, "TimePicker")
    }

    override fun onAppClick(app: App) {
        showDatePicker(app)
    }
}