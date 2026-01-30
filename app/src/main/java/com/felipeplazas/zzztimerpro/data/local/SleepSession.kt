package com.felipeplazas.zzztimerpro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long?,
    @ColumnInfo(name = "target_wake_time")
    val targetWakeTime: Long? = null,
    @ColumnInfo(name = "actual_wake_time")
    val actualWakeTime: Long? = null,
    @ColumnInfo(name = "sleep_score")
    val sleepScore: Int? = null, // 0-100
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int? = null,
    @ColumnInfo(name = "deep_sleep_minutes")
    val deepSleepMinutes: Int? = null,
    @ColumnInfo(name = "light_sleep_minutes")
    val lightSleepMinutes: Int? = null,
    @ColumnInfo(name = "rem_sleep_minutes")
    val remSleepMinutes: Int? = null,
    @ColumnInfo(name = "awake_minutes")
    val awakeMinutes: Int? = null,
    val quality: String? = null, // "Excellent", "Good", "Fair", "Poor"
    val notes: String? = null,
    val completed: Boolean = false,
    @ColumnInfo(name = "needs_aggregation", defaultValue = "1")
    val needsAggregation: Boolean = true
)

