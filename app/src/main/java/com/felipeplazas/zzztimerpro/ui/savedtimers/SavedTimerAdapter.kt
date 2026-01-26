package com.felipeplazas.zzztimerpro.ui.savedtimers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ItemSavedTimerBinding
import com.felipeplazas.zzztimerpro.data.local.SavedTimer

class SavedTimerAdapter(
    private val onStart: (SavedTimer) -> Unit,
    private val onEdit: (SavedTimer) -> Unit,
    private val onDelete: (SavedTimer) -> Unit
) : ListAdapter<SavedTimer, SavedTimerAdapter.TimerViewHolder>(TimerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val binding = ItemSavedTimerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TimerViewHolder(
        private val binding: ItemSavedTimerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timer: SavedTimer) {
            binding.timerName.text = timer.name
            binding.timerDuration.text = itemView.context.getString(R.string.minutes_format, timer.durationMinutes)

            // Set sound info
            val soundText = timer.soundName ?: itemView.context.getString(R.string.no_sound)
            binding.timerSound.text = itemView.context.getString(R.string.sound_format, soundText)

            // Set usage count
            if (timer.usedCount > 0) {
                binding.usedCount.text = itemView.context.getString(R.string.used_count_format, timer.usedCount)
                binding.usedCount.visibility = android.view.View.VISIBLE
            } else {
                binding.usedCount.visibility = android.view.View.GONE
            }

            // Click listeners
            binding.root.setOnClickListener { onStart(timer) }
            binding.root.setOnLongClickListener {
                onEdit(timer)
                true
            }
        }
    }

    private class TimerDiffCallback : DiffUtil.ItemCallback<SavedTimer>() {
        override fun areItemsTheSame(oldItem: SavedTimer, newItem: SavedTimer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedTimer, newItem: SavedTimer): Boolean {
            return oldItem == newItem
        }
    }
}
