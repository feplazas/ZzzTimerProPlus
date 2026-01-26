package com.felipeplazas.zzztimerpro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledAlarmDao {

    @Query("SELECT * FROM scheduled_alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<ScheduledAlarm>>

    @Query("SELECT * FROM scheduled_alarms WHERE enabled = 1 ORDER BY hour, minute")
    fun getEnabledAlarms(): Flow<List<ScheduledAlarm>>

    @Query("SELECT * FROM scheduled_alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): ScheduledAlarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: ScheduledAlarm): Long

    @Update
    suspend fun updateAlarm(alarm: ScheduledAlarm)

    @Delete
    suspend fun deleteAlarm(alarm: ScheduledAlarm)

    @Query("DELETE FROM scheduled_alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("UPDATE scheduled_alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)
}

