package com.felipeplazas.zzztimerpro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_cycles",
    foreignKeys = [
        ForeignKey(
            entity = SleepSession::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("session_id")]
)
data class SleepCycle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    val phase: SleepPhase, // AWAKE, LIGHT, DEEP, REM
    @ColumnInfo(name = "movement_score")
    val movementScore: Float, // 0.0 - 1.0
    @ColumnInfo(name = "sound_level")
    val soundLevel: Float // 0.0 - 1.0
)

enum class SleepPhase {
    AWAKE,
    LIGHT,
    DEEP,
    REM
}

