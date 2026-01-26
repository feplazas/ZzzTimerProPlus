package com.felipeplazas.zzztimerpro.ui.alarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felipeplazas.zzztimerpro.databinding.ItemAlarmBinding
import com.felipeplazas.zzztimerpro.data.local.ScheduledAlarm
import com.felipeplazas.zzztimerpro.R
import java.util.Locale

class AlarmAdapter(
    private val onToggle: (ScheduledAlarm, Boolean) -> Unit,
    private val onEdit: (ScheduledAlarm) -> Unit,
    private val onDelete: (ScheduledAlarm) -> Unit
) : ListAdapter<ScheduledAlarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(
        private val binding: ItemAlarmBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: ScheduledAlarm) {
            binding.tvAlarmTime.text = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)
            binding.tvAlarmName.text = alarm.name
            binding.tvRepeatDays.text = formatRepeatDays(alarm.repeatDays)
            binding.switchEnabled.isChecked = alarm.enabled

            binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onToggle(alarm, isChecked)
            }

            binding.root.setOnClickListener { onEdit(alarm) }
            binding.btnDelete.setOnClickListener { onDelete(alarm) }
        }

        private fun formatRepeatDays(repeatDays: String): String {
            val dayAbbr = listOf(
                itemView.context.getString(R.string.monday).take(3),
                itemView.context.getString(R.string.tuesday).take(3),
                itemView.context.getString(R.string.wednesday).take(3),
                itemView.context.getString(R.string.thursday).take(3),
                itemView.context.getString(R.string.friday).take(3),
                itemView.context.getString(R.string.saturday).take(3),
                itemView.context.getString(R.string.sunday).take(3)
            )
            if (repeatDays.isEmpty()) return itemView.context.getString(R.string.repeat)

            val days = repeatDays.split(",").mapNotNull { it.toIntOrNull() }
            if (days.size == 7) return dayAbbr.joinToString(", ")
            if (days.size == 5 && days.containsAll(listOf(0, 1, 2, 3, 4))) return dayAbbr.subList(0, 5).joinToString(", ")
            if (days.size == 2 && days.containsAll(listOf(5, 6))) return listOf(dayAbbr[5], dayAbbr[6]).joinToString(", ")

            return days.joinToString(", ") { dayAbbr.getOrElse(it) { "" } }
        }
    }

    private class AlarmDiffCallback : DiffUtil.ItemCallback<ScheduledAlarm>() {
        override fun areItemsTheSame(oldItem: ScheduledAlarm, newItem: ScheduledAlarm): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScheduledAlarm, newItem: ScheduledAlarm): Boolean {
            return oldItem == newItem
        }
    }
}
