package com.meldcx.appscheduler.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.meldcx.appscheduler.data.models.App
import com.meldcx.appscheduler.databinding.FragmentHomeBinding
import com.meldcx.appscheduler.ui.listeners.AppClickListener
import com.meldcx.appscheduler.ui.adapters.AppAdapter
import com.meldcx.appscheduler.ui.viewmodels.HomeViewModel
import com.meldcx.appscheduler.utils.TimeUtil
import dagger.hilt.android.AndroidEntryPoint

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
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onAppClick(app: App) {
        TimeUtil(this) { time ->
            vm.verifySchedule(time, app)
        }.showDatePicker()
    }
}