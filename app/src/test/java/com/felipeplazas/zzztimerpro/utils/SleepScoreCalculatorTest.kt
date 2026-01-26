package com.felipeplazas.zzztimerpro.utils

import com.felipeplazas.zzztimerpro.data.local.SleepSession
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepScoreCalculatorTest {

    @Test
    fun `score optimal duration gives high score`() {
        val session = SleepSession(
            startTime = 0L,
            endTime = 0L,
            totalMinutes = 480, // 8h óptimas
            deepSleepMinutes = 90, // ~18.75%
            remSleepMinutes = 105, // ~21.8%
            lightSleepMinutes = 240,
            awakeMinutes = 15
        )
        val score = SleepScoreCalculator.calculateSleepScore(session)
        // Esperamos score alto >= 85
        assertEquals(true, score >= 85)
        assertEquals("Excellent", SleepScoreCalculator.getSleepQuality(score))
    }

    @Test
    fun `score low deep sleep penalizes`() {
        val session = SleepSession(
            startTime = 0L,
            endTime = 0L,
            totalMinutes = 420,
            deepSleepMinutes = 20, // ~4.7% bajo
            remSleepMinutes = 80,
            lightSleepMinutes = 300,
            awakeMinutes = 20
        )
        val score = SleepScoreCalculator.calculateSleepScore(session)
        assertEquals(true, score < 70)
    }
}

