package com.felipeplazas.zzztimerpro.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import com.felipeplazas.zzztimerpro.data.repository.SavedTimersRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SavedTimersRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: SavedTimersRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SavedTimersRepository.createForTest(db.savedTimerDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insert and retrieve timer`() = runBlocking {
        val id = repository.saveTimer(
            SavedTimer(name = "Test", durationMinutes = 15)
        )
        val loaded = repository.getTimerById(id)
        assertNotNull(loaded)
        assertEquals("Test", loaded?.name)
    }

    @Test
    fun `mark timer used updates count and last_used_at`() = runBlocking {
        val id = repository.saveTimer(SavedTimer(name = "UseMe", durationMinutes = 10))
        val before = repository.getTimerById(id)!!
        repository.markTimerUsed(id)
        val after = repository.getTimerById(id)!!
        assertEquals(before.usedCount + 1, after.usedCount)
        assertNotNull(after.lastUsedAt)
    }
}

