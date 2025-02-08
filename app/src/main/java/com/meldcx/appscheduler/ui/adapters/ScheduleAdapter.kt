package com.meldcx.appscheduler.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meldcx.appscheduler.R
import com.meldcx.appscheduler.data.models.Schedule
import com.meldcx.appscheduler.databinding.ScheduleRecyclerItemBinding
import com.meldcx.appscheduler.ui.listeners.ScheduleClickListener
import com.meldcx.appscheduler.utils.Utility

class ScheduleAdapter(
    private val context: Context,
    private val clickListener: ScheduleClickListener) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private val schedules = mutableListOf<Schedule>()

    @SuppressLint("NotifyDataSetChanged")
    fun addSchedules(scheduleList: List<Schedule>) {
        schedules.addAll(scheduleList)
        notifyDataSetChanged()
    }

    fun updateSchedules(pos: Int) {
        schedules.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun checkForTimeUpdates() {
        notifyItemRangeChanged(0, itemCount)
    }

    inner class ViewHolder(private val binding: ScheduleRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule: Schedule) {
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
            val timeParts = Utility.parseTime(schedule.timeInMilli).split("_")
            with(binding) {
                tvAppName.text = appName
                tvTime.text = timeParts[0]
                tvDate.text = timeParts[1]
                ivAppIcon.setImageDrawable(appIcon)
                if (schedule.timeInMilli < System.currentTimeMillis()) {
                    clScheduleInfoHolder.alpha = 0.3F
                }
                cvScheduleInfoHolder.setOnClickListener {
                    if (ivDelete.visibility == View.VISIBLE) {
                        ivDelete.visibility = View.GONE
                        cvParent.setCardBackgroundColor(null)
                    } else {
                        ivDelete.visibility = View.VISIBLE
                        cvParent.setCardBackgroundColor(AppCompatResources.getColorStateList(context, R.color.bright_red))
                    }
                }
                ivDelete.setOnClickListener {
                    clickListener.onScheduleClick(schedule, position)
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
        holder.bind(schedules[position])
    }

    override fun getItemCount() = schedules.size
}