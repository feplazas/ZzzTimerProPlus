package com.felipeplazas.zzztimerpro.ui.sounds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.AmbientSound
import com.felipeplazas.zzztimerpro.databinding.ItemAmbientSoundBinding

class SoundAdapter(
    private val sounds: List<AmbientSound>,
    private val onSoundClick: (AmbientSound) -> Unit
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {
    
    private var currentPlayingId: Int? = null
    
    inner class SoundViewHolder(private val binding: ItemAmbientSoundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(sound: AmbientSound) {
            binding.apply {
                tvSoundName.text = root.context.getString(sound.nameResId)
                tvSoundDescription.text = root.context.getString(sound.descriptionResId)
                ivSoundIcon.setImageResource(sound.iconResId)
                
                // Update play button icon based on playing state
                if (currentPlayingId == sound.id) {
                    btnPlaySound.setIconResource(R.drawable.ic_pause)
                } else {
                    btnPlaySound.setIconResource(R.drawable.ic_play)
                }
                
                // Click listeners
                cardSound.setOnClickListener {
                    onSoundClick(sound)
                }
                
                btnPlaySound.setOnClickListener {
                    onSoundClick(sound)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val binding = ItemAmbientSoundBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SoundViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        holder.bind(sounds[position])
    }
    
    override fun getItemCount(): Int = sounds.size
    
    fun setCurrentPlaying(soundId: Int?) {
        val previousId = currentPlayingId
        currentPlayingId = soundId
        
        // Notify changes for previous and current playing items
        sounds.indexOfFirst { it.id == previousId }.takeIf { it != -1 }?.let {
            notifyItemChanged(it)
        }
        sounds.indexOfFirst { it.id == soundId }.takeIf { it != -1 }?.let {
            notifyItemChanged(it)
        }
    }
}
