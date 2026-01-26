package com.felipeplazas.zzztimerpro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepCycleDao {

    @Query("SELECT * FROM sleep_cycles WHERE session_id = :sessionId ORDER BY start_time ASC")
    fun getCyclesBySession(sessionId: Long): Flow<List<SleepCycle>>

    @Query("SELECT * FROM sleep_cycles WHERE session_id = :sessionId ORDER BY start_time ASC")
    suspend fun getCyclesBySessionSync(sessionId: Long): List<SleepCycle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: SleepCycle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycles(cycles: List<SleepCycle>)

    @Update
    suspend fun updateCycle(cycle: SleepCycle)

    @Delete
    suspend fun deleteCycle(cycle: SleepCycle)

    @Query("DELETE FROM sleep_cycles WHERE session_id = :sessionId")
    suspend fun deleteCyclesBySession(sessionId: Long)
}

