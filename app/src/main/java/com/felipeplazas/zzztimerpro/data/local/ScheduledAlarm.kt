package com.felipeplazas.zzztimerpro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_alarms")
data class ScheduledAlarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hour: Int, // 0-23
    val minute: Int, // 0-59
    val enabled: Boolean = true,
    @ColumnInfo(name = "repeat_days")
    val repeatDays: String = "", // Comma-separated: "0,1,2,3,4" for Mon-Fri
    @ColumnInfo(name = "sound_res_id")
    val soundResId: Int? = null,
    @ColumnInfo(name = "custom_sound_uri")
    val customSoundUri: String? = null,
    @ColumnInfo(name = "vibration_enabled")
    val vibrationEnabled: Boolean = true,
    @ColumnInfo(name = "smart_wake_enabled")
    val smartWakeEnabled: Boolean = false, // Smart wake-up feature
    @ColumnInfo(name = "wake_window_minutes")
    val wakeWindowMinutes: Int = 30, // Window for smart wake
    @ColumnInfo(name = "math_challenge_enabled")
    val mathChallengeEnabled: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

