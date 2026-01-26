package com.felipeplazas.zzztimerpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TimerSession::class,
        SavedTimer::class,
        SleepSession::class,
        SleepCycle::class,
        ScheduledAlarm::class,
        CustomSound::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun savedTimerDao(): SavedTimerDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun sleepCycleDao(): SleepCycleDao
    abstract fun scheduledAlarmDao(): ScheduledAlarmDao
    abstract fun customSoundDao(): CustomSoundDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tablas asegurando nombres snake_case alineados con @ColumnInfo de las entidades.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_timers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        duration_minutes INTEGER NOT NULL,
                        sound_res_id INTEGER,
                        sound_name TEXT,
                        custom_sound_uri TEXT,
                        vibration_enabled INTEGER NOT NULL DEFAULT 1,
                        fade_duration_minutes INTEGER NOT NULL DEFAULT 5,
                        created_at INTEGER NOT NULL,
                        last_used_at INTEGER,
                        used_count INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sleep_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER,
                        targetWakeTime INTEGER,
                        actualWakeTime INTEGER,
                        sleepScore INTEGER,
                        totalMinutes INTEGER,
                        deepSleepMinutes INTEGER,
                        lightSleepMinutes INTEGER,
                        remSleepMinutes INTEGER,
                        awakeMinutes INTEGER,
                        quality TEXT,
                        notes TEXT,
                        completed INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sleep_cycles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId INTEGER NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        phase TEXT NOT NULL,
                        movementScore REAL NOT NULL,
                        soundLevel REAL NOT NULL,
                        FOREIGN KEY(sessionId) REFERENCES sleep_sessions(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sleep_cycles_sessionId ON sleep_cycles(sessionId)")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scheduled_alarms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        hour INTEGER NOT NULL,
                        minute INTEGER NOT NULL,
                        enabled INTEGER NOT NULL DEFAULT 1,
                        repeat_days TEXT NOT NULL,
                        sound_res_id INTEGER,
                        custom_sound_uri TEXT,
                        vibration_enabled INTEGER NOT NULL DEFAULT 1,
                        smart_wake_enabled INTEGER NOT NULL DEFAULT 0,
                        wake_window_minutes INTEGER NOT NULL DEFAULT 30,
                        math_challenge_enabled INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_sounds (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        uri TEXT NOT NULL,
                        category TEXT NOT NULL,
                        duration INTEGER,
                        addedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear índices para mejorar consultas frecuentes
                database.execSQL("CREATE INDEX IF NOT EXISTS index_saved_timers_last_used_at ON saved_timers(last_used_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_saved_timers_used_count ON saved_timers(used_count)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_alarms_enabled ON scheduled_alarms(enabled)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_alarms_hour_minute ON scheduled_alarms(hour, minute)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zzz_timer_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
