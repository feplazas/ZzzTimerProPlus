package com.felipeplazas.zzztimerpro.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromSleepPhase(value: SleepPhase): String {
        return value.name
    }

    @TypeConverter
    fun toSleepPhase(value: String): SleepPhase {
        return SleepPhase.valueOf(value)
    }

    @TypeConverter
    fun fromSoundCategory(value: SoundCategory): String {
        return value.name
    }

    @TypeConverter
    fun toSoundCategory(value: String): SoundCategory {
        return SoundCategory.valueOf(value)
    }
}

