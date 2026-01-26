package com.felipeplazas.zzztimerpro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSoundDao {

    @Query("SELECT * FROM custom_sounds ORDER BY added_at DESC")
    fun getAllCustomSounds(): Flow<List<CustomSound>>

    @Query("SELECT * FROM custom_sounds WHERE category = :category ORDER BY added_at DESC")
    fun getSoundsByCategory(category: SoundCategory): Flow<List<CustomSound>>

    @Query("SELECT * FROM custom_sounds WHERE id = :id")
    suspend fun getSoundById(id: Long): CustomSound?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: CustomSound): Long

    @Update
    suspend fun updateSound(sound: CustomSound)

    @Delete
    suspend fun deleteSound(sound: CustomSound)

    @Query("DELETE FROM custom_sounds WHERE id = :id")
    suspend fun deleteSoundById(id: Long)
}

