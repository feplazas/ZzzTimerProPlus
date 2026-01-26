package com.felipeplazas.zzztimerpro.services

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistencia ligera del temporizador para restaurar tras kill / swipe en recientes.
 * Guardamos timestamp de finalización y duración total. Calculamos restante dinámicamente.
 */
object TimerPersistence {
    private const val PREF_NAME = "timer_prefs"
    private const val KEY_END_TIMESTAMP = "end_timestamp_ms"
    private const val KEY_TOTAL_DURATION = "total_duration_ms"
    private const val KEY_IS_RUNNING = "is_running"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveStart(ctx: Context, endTimestamp: Long, totalDuration: Long) {
        prefs(ctx).edit()
            .putLong(KEY_END_TIMESTAMP, endTimestamp)
            .putLong(KEY_TOTAL_DURATION, totalDuration)
            .putBoolean(KEY_IS_RUNNING, true)
            .apply()
    }

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }

    fun isRunning(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_IS_RUNNING, false)
    fun getEndTimestamp(ctx: Context): Long = prefs(ctx).getLong(KEY_END_TIMESTAMP, 0L)
    fun getTotalDuration(ctx: Context): Long = prefs(ctx).getLong(KEY_TOTAL_DURATION, 0L)

    /**
     * Calcula milisegundos restantes basados en tiempo real. Si ya pasó, retorna 0.
     */
    fun computeRemaining(ctx: Context): Long {
        val end = getEndTimestamp(ctx)
        if (end <= 0L) return 0L
        val remaining = end - System.currentTimeMillis()
        return if (remaining < 0L) 0L else remaining
    }
}

