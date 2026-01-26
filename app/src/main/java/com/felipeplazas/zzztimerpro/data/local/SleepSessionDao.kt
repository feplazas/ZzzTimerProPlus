package com.felipeplazas.zzztimerpro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Query("SELECT * FROM sleep_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SleepSession?

    @Query("SELECT * FROM sleep_sessions WHERE completed = 1 ORDER BY start_time DESC LIMIT :limit")
    fun getCompletedSessions(limit: Int = 30): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE start_time >= :startDate AND start_time <= :endDate ORDER BY start_time DESC")
    suspend fun getSessionsByDateRange(startDate: Long, endDate: Long): List<SleepSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSession): Long

    @Update
    suspend fun updateSession(session: SleepSession)

    @Delete
    suspend fun deleteSession(session: SleepSession)

    @Query("DELETE FROM sleep_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT AVG(sleep_score) FROM sleep_sessions WHERE completed = 1 AND sleep_score IS NOT NULL")
    suspend fun getAverageSleepScore(): Float?

    @Query("SELECT AVG(total_minutes) FROM sleep_sessions WHERE completed = 1 AND total_minutes IS NOT NULL")
    suspend fun getAverageSleepDuration(): Float?

    @Query("SELECT SUM(total_minutes) FROM sleep_sessions WHERE completed = 1")
    suspend fun getTotalMinutesUsed(): Int?

    @Query("SELECT COUNT(*) FROM sleep_sessions WHERE completed = 1")
    suspend fun getCompletedSessionCount(): Int

    @Transaction
    @Query("SELECT * FROM sleep_sessions WHERE id = :sessionId")
    suspend fun getSessionWithCycles(sessionId: Long): SessionWithCycles?
}

data class SessionWithCycles(
    @Embedded val session: SleepSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    val cycles: List<SleepCycle>
)

