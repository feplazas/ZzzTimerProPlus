package com.felipeplazas.zzztimerpro.migration

import android.database.Cursor
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration23Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private val dbName = "migration-test"

    @Test
    fun migrate2To3_createsIndices() {
        // Create database at version 2 and close
        helper.createDatabase(dbName, 2).close()
        // Run migration to 3 with declared migrations
        val db: SupportSQLiteDatabase = helper.runMigrationsAndValidate(
            dbName,
            3,
            true,
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3
        )
        // Validate indices existence by querying sqlite_master
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name IN ('index_saved_timers_last_used_at','index_saved_timers_used_count','index_scheduled_alarms_enabled','index_scheduled_alarms_hour_minute')").use { cursor: Cursor ->
            assert(cursor.count >= 4)
        }
    }
}
