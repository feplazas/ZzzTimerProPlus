package com.felipeplazas.zzztimerpro.ui.savedtimers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import com.felipeplazas.zzztimerpro.databinding.ItemSavedTimerBinding

class SavedTimersAdapter(
    private val timers: List<SavedTimer>,
    private val onTimerClick: (SavedTimer) -> Unit,
    private val onTimerLongClick: (SavedTimer) -> Unit
) : RecyclerView.Adapter<SavedTimersAdapter.TimerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val binding = ItemSavedTimerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(timers[position])
    }

    override fun getItemCount() = timers.size

    inner class TimerViewHolder(
        private val binding: ItemSavedTimerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timer: SavedTimer) {
            binding.timerName.text = timer.name
            binding.timerDuration.text = itemView.context.getString(
                R.string.minutes_format,
                timer.durationMinutes
            )

            val soundName = if (timer.soundResId != null) {
                getSoundName(timer.soundResId)
            } else {
                itemView.context.getString(R.string.no_sound)
            }
            binding.timerSound.text = itemView.context.getString(R.string.sound_format, soundName)

            binding.usedCount.text = itemView.context.getString(
                R.string.used_count_format,
                timer.usedCount
            )

            binding.root.setOnClickListener {
                onTimerClick(timer)
            }

            binding.root.setOnLongClickListener {
                onTimerLongClick(timer)
                true
            }
        }

        private fun getSoundName(resId: Int): String {
            return when (resId) {
                R.raw.soft_rain -> itemView.context.getString(R.string.sound_soft_rain)
                R.raw.ocean_waves -> itemView.context.getString(R.string.sound_ocean_waves)
                R.raw.night_forest -> itemView.context.getString(R.string.sound_night_forest)
                R.raw.gentle_wind -> itemView.context.getString(R.string.sound_gentle_wind)
                R.raw.white_noise -> itemView.context.getString(R.string.sound_white_noise)
                R.raw.night_birds -> itemView.context.getString(R.string.sound_night_birds)
                else -> itemView.context.getString(R.string.no_sound)
            }
        }
    }
}

