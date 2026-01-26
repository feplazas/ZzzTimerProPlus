package com.felipeplazas.zzztimerpro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTimerDao {

    @Query("SELECT * FROM saved_timers ORDER BY last_used_at DESC, used_count DESC")
    fun getAllTimers(): Flow<List<SavedTimer>>

    @Query("SELECT * FROM saved_timers WHERE id = :id")
    suspend fun getTimerById(id: Long): SavedTimer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: SavedTimer): Long

    @Update
    suspend fun updateTimer(timer: SavedTimer)

    @Delete
    suspend fun deleteTimer(timer: SavedTimer)

    @Query("DELETE FROM saved_timers WHERE id = :id")
    suspend fun deleteTimerById(id: Long)

    @Query("UPDATE saved_timers SET last_used_at = :timestamp, used_count = used_count + 1 WHERE id = :id")
    suspend fun updateTimerUsage(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM saved_timers ORDER BY used_count DESC LIMIT 5")
    fun getMostUsedTimers(): Flow<List<SavedTimer>>
}

