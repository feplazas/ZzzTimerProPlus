package com.felipeplazas.zzztimerpro.data.repository

import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.AmbientSound

object SoundRepository {
    
    fun getAllSounds(): List<AmbientSound> {
        return listOf(
            AmbientSound(
                id = 1,
                nameResId = R.string.sound_soft_rain,
                descriptionResId = R.string.desc_soft_rain,
                iconResId = R.drawable.ic_rain,
                audioResId = R.raw.soft_rain
            ),
            AmbientSound(
                id = 3,
                nameResId = R.string.sound_night_forest,
                descriptionResId = R.string.desc_night_forest,
                iconResId = R.drawable.ic_forest,
                audioResId = R.raw.night_forest
            ),
            AmbientSound(
                id = 4,
                nameResId = R.string.sound_gentle_wind,
                descriptionResId = R.string.desc_gentle_wind,
                iconResId = R.drawable.ic_wind,
                audioResId = R.raw.gentle_wind
            ),
            AmbientSound(
                id = 5,
                nameResId = R.string.sound_white_noise,
                descriptionResId = R.string.desc_white_noise,
                iconResId = R.drawable.ic_noise,
                audioResId = R.raw.white_noise
            )
        )
    }
    
    fun getSoundById(id: Int): AmbientSound? {
        return getAllSounds().find { it.id == id }
    }
}
