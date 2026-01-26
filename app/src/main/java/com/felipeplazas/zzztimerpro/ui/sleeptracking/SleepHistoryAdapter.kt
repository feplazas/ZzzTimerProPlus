package com.felipeplazas.zzztimerpro.ui.sleeptracking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felipeplazas.zzztimerpro.data.local.SleepSession
import com.felipeplazas.zzztimerpro.databinding.ItemSleepSessionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepHistoryAdapter : ListAdapter<SleepSession, SleepHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSleepSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemSleepSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun bind(session: SleepSession) {
            val startDate = Date(session.startTime)
            binding.tvDate.text = dateFormat.format(startDate)

            val startTimeStr = timeFormat.format(startDate)
            val endTimeStr = if (session.endTime != null && session.endTime!! > 0) {
                timeFormat.format(Date(session.endTime!!))
            } else {
                "Incomplete"
            }
            binding.tvTimeRange.text = "$startTimeStr - $endTimeStr"

            binding.tvScore.text = "${session.sleepScore ?: 0}"
            
            val totalMin = session.totalMinutes ?: 0
            val hours = totalMin / 60
            val mins = totalMin % 60
            binding.tvDuration.text = "${hours}h ${mins}m"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SleepSession>() {
        override fun areItemsTheSame(oldItem: SleepSession, newItem: SleepSession): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SleepSession, newItem: SleepSession): Boolean {
            return oldItem == newItem
        }
    }
}
