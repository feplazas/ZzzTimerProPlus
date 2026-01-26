package com.felipeplazas.zzztimerpro.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration12InstrumentedTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private val dbName = "migration-test-1-2"

    @Test
    fun migrate1To2_createsAllTables() {
        helper.createDatabase(dbName, 1).close()
        val db = helper.runMigrationsAndValidate(
            dbName,
            2,
            true,
            AppDatabase.MIGRATION_1_2
        )

        // Verificar que las tablas existen consultando sqlite_master
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name IN ('saved_timers','sleep_sessions','sleep_cycles','scheduled_alarms','custom_sounds')")
        cursor.use {
            assert(it.count >= 5) { "Esperado al menos 5 tablas, encontradas: ${it.count}" }
        }
    }
}


