package com.felipeplazas.zzztimerpro

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedTimerCrudInstrumentedTest {

    @Test
    fun insertReadUpdateDeleteTimer() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(context)
        val dao = db.savedTimerDao()

        val id = dao.insertTimer(SavedTimer(name = "Instrumented", durationMinutes = 20))
        val read = dao.getTimerById(id)
        assertNotNull(read)
        assertEquals("Instrumented", read?.name)

        val updated = read!!.copy(name = "InstrumentedUpdated")
        dao.updateTimer(updated)
        val readUpdated = dao.getTimerById(id)
        assertEquals("InstrumentedUpdated", readUpdated?.name)

        dao.deleteTimerById(id)
        val deleted = dao.getTimerById(id)
        assertEquals(null, deleted)
    }
}

