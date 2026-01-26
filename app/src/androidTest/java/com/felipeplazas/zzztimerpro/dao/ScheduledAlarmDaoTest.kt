package com.felipeplazas.zzztimerpro.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.ScheduledAlarm
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduledAlarmDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insert_and_query() = runBlocking {
        val id = db.scheduledAlarmDao().insertAlarm(
            ScheduledAlarm(
                name = "Test",
                hour = 7,
                minute = 30
            )
        )
        assert(id > 0)
        val all = db.scheduledAlarmDao().getAllAlarms().first()
        assertEquals(1, all.size)
        assertEquals("Test", all[0].name)
    }
}

