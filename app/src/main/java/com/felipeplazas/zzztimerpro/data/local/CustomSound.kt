package com.felipeplazas.zzztimerpro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_sounds")
data class CustomSound(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val uri: String, // Content URI of the audio file
    val category: SoundCategory = SoundCategory.CUSTOM,
    val duration: Long? = null, // Duration in milliseconds
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)

enum class SoundCategory {
    NATURE,
    WHITE_NOISE,
    MEDITATION,
    BREATHING,
    CUSTOM
}

