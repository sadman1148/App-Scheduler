package com.meldcx.appscheduler.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.meldcx.appscheduler.R
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.databinding.ScheduleRecyclerItemBinding
import com.meldcx.appscheduler.ui.listeners.ScheduleClickListener
import com.meldcx.appscheduler.utils.TimeUtil
import timber.log.Timber

class ScheduleAdapter(
    private val context: Context,
    private val clickListener: ScheduleClickListener) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private val schedules = mutableListOf<Schedule>()

    @SuppressLint("NotifyDataSetChanged")
    fun addSchedules(scheduleList: List<Schedule>) {
        schedules.clear()
        schedules.addAll(scheduleList)
        notifyDataSetChanged()
    }

    fun updateSchedules(pos: Int) {
        Timber.d("updateSchedules > removing index $pos")
        schedules.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, itemCount)
        Timber.d("updateSchedules > updated schedules list: $schedules")
    }

    fun checkForTimeUpdates() {
        Timber.d("checkForTimeUpdates > schedules list: $schedules")
        notifyItemRangeChanged(0, itemCount)
    }

    inner class ViewHolder(private val binding: ScheduleRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule: Schedule, position: Int) {
            var appName: String
            var appIcon: Drawable? = AppCompatResources.getDrawable(context, R.drawable.clear)
            context.packageManager.let {
                try {
                    appIcon = it.getApplicationIcon(schedule.packageName)
                } catch (_: Exception) { }

                appName = try {
                    it.getApplicationLabel(it.getApplicationInfo(schedule.packageName, 0)).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    "No App Found"
                }
            }
            val timeParts = TimeUtil.parseTime(schedule.timeInMilli).split("_")
            with(binding) {
                val isExpired = schedule.timeInMilli < System.currentTimeMillis()
                cvDelete.visibility = View.GONE
                cvEdit.visibility = View.GONE
                tvAppName.text = appName
                tvPackageName.text = schedule.packageName
                tvTime.text = timeParts[0]
                tvDate.text = timeParts[1]
                ivAppIcon.setImageDrawable(appIcon)
                if (isExpired) {
                    clScheduleInfoHolder.alpha = 0.3F
                }
                cvParent.setOnClickListener {
                    if (cvDelete.visibility == View.VISIBLE) {
                        cvDelete.visibility = View.GONE
                    } else {
                        cvDelete.visibility = View.VISIBLE
                    }
                    if (!isExpired) {
                        if (cvEdit.visibility == View.VISIBLE) {
                            cvEdit.visibility = View.GONE
                        } else {
                            cvEdit.visibility = View.VISIBLE
                        }
                    }
                }
                cvEdit.setOnClickListener {
                    clickListener.onScheduleEditClick(schedule)
                    cvDelete.visibility = View.GONE
                    cvEdit.visibility = View.GONE
                }
                cvDelete.setOnClickListener {
                    Timber.d("cvDelete onClick > position $position clicked for ${schedule.packageName}")
                    clickListener.onScheduleDeleteClick(schedule, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ScheduleRecyclerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ScheduleAdapter.ViewHolder, position: Int) {
        holder.bind(schedules[position], position)
    }

    override fun getItemCount() = schedules.size
}