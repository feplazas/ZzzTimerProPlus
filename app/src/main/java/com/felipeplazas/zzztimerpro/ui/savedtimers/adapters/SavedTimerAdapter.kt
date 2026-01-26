package com.felipeplazas.zzztimerpro.ui.savedtimers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import com.felipeplazas.zzztimerpro.databinding.ItemSavedTimerBinding

class SavedTimerAdapter(
    private val onTimerClick: (SavedTimer) -> Unit,
    private val onTimerEdit: (SavedTimer) -> Unit,
    private val onTimerDelete: (SavedTimer) -> Unit
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
            binding.timerDuration.text = itemView.context.getString(
                R.string.minutes_format,
                timer.durationMinutes
            )

            val soundText = timer.soundName ?: itemView.context.getString(R.string.no_sound)
            binding.timerSound.text = itemView.context.getString(R.string.sound_format, soundText)

            if (timer.usedCount > 0) {
                binding.usedCount.text = itemView.context.getString(
                    R.string.used_count_format,
                    timer.usedCount
                )
                binding.usedCount.visibility = android.view.View.VISIBLE
            } else {
                binding.usedCount.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener {
                onTimerClick(timer)
            }

            // For edit and delete, we can use long press and show a menu
            binding.root.setOnLongClickListener {
                onTimerEdit(timer)
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

