package com.meldcx.appscheduler.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meldcx.appscheduler.data.models.App
import com.meldcx.appscheduler.databinding.RecyclerItemBinding
import com.meldcx.appscheduler.ui.listeners.AppClickListener


class AppAdapter(private val clickListener: AppClickListener) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private val apps = mutableListOf<App>()

    @SuppressLint("NotifyDataSetChanged")
    fun addApps(appList: List<App>) {
        apps.addAll(appList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: RecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(app: App) {
            with(binding) {
                tvAppName.text = app.name
                ivAppIcon.setImageDrawable(app.icon)
                cvAppInfoHolder.setOnClickListener { clickListener.onAppClick(app) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AppAdapter.ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount() = apps.size
}