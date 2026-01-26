package com.felipeplazas.zzztimerpro

import com.felipeplazas.zzztimerpro.data.local.SleepSession
import com.felipeplazas.zzztimerpro.utils.SleepScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepScoreCalculatorTest {

    @Test
    fun score_is_100_for_optimal_distribution() {
        val session = SleepSession(
            id = 1,
            startTime = 0L,
            endTime = 0L,
            targetWakeTime = null,
            actualWakeTime = null,
            sleepScore = null,
            totalMinutes = 480, // 8h
            deepSleepMinutes = 96, // 20%
            lightSleepMinutes = 288,
            remSleepMinutes = 96, // 20%
            awakeMinutes = 0,
            quality = null,
            notes = null,
            completed = true
        )
        val score = SleepScoreCalculator.calculateSleepScore(session)
        // Máximo 100; según la función, 30 + 30 + 20 + 20 = 100
        assertEquals(100, score)
    }

    @Test
    fun score_quality_labels() {
        assertEquals("Excellent", SleepScoreCalculator.getSleepQuality(90))
        assertEquals("Good", SleepScoreCalculator.getSleepQuality(75))
        assertEquals("Fair", SleepScoreCalculator.getSleepQuality(60))
        assertEquals("Poor", SleepScoreCalculator.getSleepQuality(40))
    }
}
