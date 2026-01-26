package com.felipeplazas.zzztimerpro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "saved_timers")
data class SavedTimer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    @ColumnInfo(name = "sound_res_id")
    val soundResId: Int? = null,
    @ColumnInfo(name = "sound_name")
    val soundName: String? = null,
    @ColumnInfo(name = "custom_sound_uri")
    val customSoundUri: String? = null,
    @ColumnInfo(name = "vibration_enabled")
    val vibrationEnabled: Boolean = true,
    @ColumnInfo(name = "fade_duration_minutes")
    val fadeDurationMinutes: Int = 5,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long? = null,
    @ColumnInfo(name = "used_count")
    val usedCount: Int = 0
) : Serializable


