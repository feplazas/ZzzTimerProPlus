package com.felipeplazas.zzztimerpro.utils

import com.felipeplazas.zzztimerpro.data.local.SleepSession

/**
 * Calculadora de puntuación de sueño basada en métricas de calidad.
 * Evalúa duración total, sueño profundo, REM y consistencia.
 */
object SleepScoreCalculator {

    /**
     * Calcula la puntuación de sueño de 0 a 100.
     * @param session Sesión de sueño con métricas registradas
     * @return Puntuación entre 0-100
     */
    fun calculateSleepScore(session: SleepSession): Int {
        var score = 0

        // Duration score (max 30 points)
        val durationScore = calculateDurationScore(session.totalMinutes ?: 0)
        score += durationScore

        // Deep sleep score (max 30 points)
        val deepSleepScore = calculateDeepSleepScore(
            session.deepSleepMinutes ?: 0,
            session.totalMinutes ?: 1
        )
        score += deepSleepScore

        // REM sleep score (max 20 points)
        val remSleepScore = calculateREMScore(
            session.remSleepMinutes ?: 0,
            session.totalMinutes ?: 1
        )
        score += remSleepScore

        // Consistency score (max 20 points)
        val consistencyScore = 20 // Would be calculated based on historical data
        score += consistencyScore

        return score.coerceIn(0, 100)
    }

    private fun calculateDurationScore(totalMinutes: Int): Int {
        // Optimal sleep: 7-9 hours (420-540 minutes)
        return when {
            totalMinutes in 420..540 -> 30 // Perfect
            totalMinutes in 360..420 || totalMinutes in 540..600 -> 25 // Good
            totalMinutes in 300..360 || totalMinutes in 600..660 -> 20 // Fair
            totalMinutes < 300 -> (totalMinutes / 10).coerceIn(0, 15) // Poor
            else -> 15 // Too much sleep
        }
    }

    private fun calculateDeepSleepScore(deepMinutes: Int, totalMinutes: Int): Int {
        // Deep sleep should be 15-25% of total sleep
        val percentage = (deepMinutes.toFloat() / totalMinutes.toFloat()) * 100
        return when {
            percentage in 15f..25f -> 30
            percentage in 10f..15f || percentage in 25f..30f -> 25
            percentage in 5f..10f || percentage in 30f..35f -> 20
            else -> 10
        }
    }

    private fun calculateREMScore(remMinutes: Int, totalMinutes: Int): Int {
        // REM sleep should be 20-25% of total sleep
        val percentage = (remMinutes.toFloat() / totalMinutes.toFloat()) * 100
        return when {
            percentage in 20f..25f -> 20
            percentage in 15f..20f || percentage in 25f..30f -> 15
            percentage in 10f..15f || percentage in 30f..35f -> 10
            else -> 5
        }
    }

    /**
     * Obtiene la etiqueta de calidad basada en la puntuación.
     * Debe usarse con Context.getString() para i18n.
     * @param score Puntuación de sueño (0-100)
     * @return Clave de string resource (quality_excellent, quality_good, etc.)
     */
    fun getSleepQualityKey(score: Int): String {
        return when {
            score >= 85 -> "quality_excellent"
            score >= 70 -> "quality_good"
            score >= 50 -> "quality_fair"
            else -> "quality_poor"
        }
    }

    /**
     * Método de compatibilidad para tests JVM que no tienen acceso a resources.
     * Devuelve etiquetas en inglés según el score.
     */
    fun getSleepQuality(score: Int): String {
        return when {
            score >= 85 -> "Excellent"
            score >= 70 -> "Good"
            score >= 50 -> "Fair"
            else -> "Poor"
        }
    }

    /**
     * Genera insight personalizado basado en la sesión y promedio.
     * @param session Sesión de sueño actual
     * @param averageScore Puntuación promedio del usuario
     * @return Clave de string resource para el insight
     */
    fun generateInsightKey(session: SleepSession, averageScore: Float): String {
        val score = session.sleepScore ?: 0

        return when {
            score > averageScore + 10 -> "insight_great_night"
            score < averageScore - 10 -> "insight_below_average"
            (session.deepSleepMinutes ?: 0) < ((session.totalMinutes ?: 0) * 0.15) -> "insight_low_deep_sleep"
            (session.awakeMinutes ?: 0) > 30 -> "insight_awake_periods"
            else -> "insight_consistent"
        }
    }
}
