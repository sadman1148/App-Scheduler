package com.meldcx.appscheduler.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.databinding.FragmentScheduleBinding
import com.meldcx.appscheduler.ui.adapters.ScheduleAdapter
import com.meldcx.appscheduler.ui.listeners.ScheduleClickListener
import com.meldcx.appscheduler.ui.viewmodels.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleFragment : Fragment(), ScheduleClickListener {

    private lateinit var binding: FragmentScheduleBinding
    private val vm: ScheduleViewModel by viewModels()
    private val scheduleAdapter by lazy {
        ScheduleAdapter(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            with(rvSchedules) {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = scheduleAdapter
            }
            vm.schedules.observe(viewLifecycleOwner) {
                if (it.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    ivEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                    ivEmpty.visibility = View.GONE
                    scheduleAdapter.addSchedules(it)
                }
            }
            vm.status.observe(viewLifecycleOwner) {
                scheduleAdapter.updateSchedules(it)
                if (scheduleAdapter.itemCount == 0) {
                    tvEmpty.visibility = View.VISIBLE
                    ivEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        scheduleAdapter.checkForTimeUpdates()
    }

    override fun onScheduleClick(schedule: Schedule, pos: Int) {
        vm.handleScheduleDeletion(schedule, pos)
    }
}