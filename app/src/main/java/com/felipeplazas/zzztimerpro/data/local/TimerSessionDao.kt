package com.felipeplazas.zzztimerpro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {
    
    @Insert
    suspend fun insertSession(session: TimerSession): Long
    
    @Query("SELECT * FROM timer_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<TimerSession>>

    @Query("SELECT * FROM timer_sessions ORDER BY start_time DESC")
    suspend fun getAllSessionsList(): List<TimerSession>
    
    @Query("SELECT * FROM timer_sessions WHERE completed = 1 ORDER BY start_time DESC")
    fun getCompletedSessions(): Flow<List<TimerSession>>
    
    @Query("SELECT * FROM timer_sessions WHERE start_time >= :startTime AND start_time <= :endTime ORDER BY start_time DESC")
    fun getSessionsInRangeFlow(startTime: Long, endTime: Long): Flow<List<TimerSession>>

    @Query("SELECT * FROM timer_sessions WHERE start_time >= :startTime AND start_time <= :endTime ORDER BY start_time DESC")
    suspend fun getSessionsInRange(startTime: Long, endTime: Long): List<TimerSession>
    
    @Query("SELECT COUNT(*) FROM timer_sessions WHERE completed = 1")
    suspend fun getCompletedSessionCount(): Int
    
    @Query("SELECT SUM(duration_minutes) FROM timer_sessions WHERE completed = 1")
    suspend fun getTotalMinutesUsed(): Int?
    
    @Query("SELECT AVG(duration_minutes) FROM timer_sessions WHERE completed = 1")
    suspend fun getAverageDuration(): Float?
    
    @Query("SELECT sound_used FROM timer_sessions WHERE sound_used IS NOT NULL GROUP BY sound_used ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostUsedSound(): String?
    
    @Query("DELETE FROM timer_sessions")
    suspend fun deleteAllSessions()
    
    @Delete
    suspend fun deleteSession(session: TimerSession)
}
