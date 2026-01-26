package com.felipeplazas.zzztimerpro.data.repository

import com.felipeplazas.zzztimerpro.data.local.TimerSession
import com.felipeplazas.zzztimerpro.data.local.TimerSessionDao
import kotlinx.coroutines.flow.Flow
import java.util.*

import com.felipeplazas.zzztimerpro.data.local.SleepSessionDao

class StatisticsRepository(
    private val dao: TimerSessionDao,
    private val sleepDao: SleepSessionDao
) {
    
    fun getAllSessions(): Flow<List<TimerSession>> = dao.getAllSessions()
    
    fun getCompletedSessions(): Flow<List<TimerSession>> = dao.getCompletedSessions()
    
    suspend fun insertSession(session: TimerSession): Long {
        return dao.insertSession(session)
    }
    
    suspend fun getCompletedSessionCount(): Int {
        val timerCount = dao.getCompletedSessionCount()
        val sleepCount = sleepDao.getCompletedSessionCount()
        return timerCount + sleepCount
    }
    
    suspend fun getTotalMinutesUsed(): Int {
        val timerMinutes = dao.getTotalMinutesUsed() ?: 0
        val sleepMinutes = sleepDao.getTotalMinutesUsed() ?: 0
        return timerMinutes + sleepMinutes
    }
    
    suspend fun getAverageDuration(): Float {
        return dao.getAverageDuration() ?: 0f
    }
    
    suspend fun getMostUsedSound(): String? {
        return dao.getMostUsedSound()
    }
    
    // Updated to use actual data query from both sources
    suspend fun getWeeklyData(): Map<String, Int> {
        val calendar = Calendar.getInstance()
        // Reset to end of today
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis
        
        // Go back 7 days
        calendar.add(Calendar.DAY_OF_YEAR, -6) // 6 days ago + today = 7 days
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        
        // Fetch sessions in range
        val timerSessions = dao.getSessionsInRange(startTime, endTime)
        val sleepSessions = sleepDao.getSessionsByDateRange(startTime, endTime)
        
        val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        
        // Let's create a map for the last 7 days 
        val resultData = mutableMapOf<String, Int>()
        
        val tempCal = Calendar.getInstance()
        tempCal.timeInMillis = startTime
        
        // Initialize map with 0
        for(i in 0 until 7) {
            val dayName = daysOfWeek[tempCal.get(Calendar.DAY_OF_WEEK) - 1]
            resultData[dayName] = 0
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        // Aggregate minutes from Timer
        timerSessions.forEach { session ->
            val sessionCal = Calendar.getInstance()
            sessionCal.timeInMillis = session.startTime
            val dayName = daysOfWeek[sessionCal.get(Calendar.DAY_OF_WEEK) - 1]
            
            val current = resultData[dayName] ?: 0
            resultData[dayName] = current + session.durationMinutes
        }

        // Aggregate minutes from Sleep
        sleepSessions.forEach { session ->
            val sessionCal = Calendar.getInstance()
            sessionCal.timeInMillis = session.startTime
            val dayName = daysOfWeek[sessionCal.get(Calendar.DAY_OF_WEEK) - 1]
            
            val current = resultData[dayName] ?: 0
            resultData[dayName] = current + (session.totalMinutes ?: 0)
        }
        
        return resultData
    }
    
    suspend fun getMostCommonSchedule(): String {
        val sessions = dao.getAllSessionsList() // Need a direct list method in DAO or collect
        if (sessions.isEmpty()) return "N/A"
        
        // Determine most common hour block (Morning, Afternoon, Evening, Night)
        // Morning: 5-12, Afternoon: 12-17, Evening: 17-21, Night: 21-5
        var morning = 0
        var afternoon = 0
        var evening = 0
        var night = 0
        
        sessions.forEach { 
             val cal = Calendar.getInstance()
             cal.timeInMillis = it.startTime
             val hour = cal.get(Calendar.HOUR_OF_DAY)
             when(hour) {
                 in 5..11 -> morning++
                 in 12..16 -> afternoon++
                 in 17..20 -> evening++
                 else -> night++
             }
        }
        
        val max = maxOf(morning, afternoon, evening, night)
        return when(max) {
            morning -> "Morning (5 AM - 12 PM)"
            afternoon -> "Afternoon (12 PM - 5 PM)"
            evening -> "Evening (5 PM - 9 PM)"
            else -> "Night (9 PM - 5 AM)"
        }
    }
    
    suspend fun clearAllStatistics() {
        dao.deleteAllSessions()
    }
    
    suspend fun exportData(): String {
        val sessions = dao.getAllSessionsList() // Assume this exists or add it
        
        val csv = StringBuilder()
        csv.append("ID,Duration (min),Start Time,End Time,Completed,Sound Used\n")
        
        sessions.forEach { session ->
            csv.append("${session.id},")
            csv.append("${session.durationMinutes},")
            csv.append("${Date(session.startTime)},")
            csv.append("${Date(session.endTime)},")
            csv.append("${session.completed},")
            csv.append("${session.soundUsed ?: "None"}\n")
        }
        
        return csv.toString()
    }
}
