package com.felipeplazas.zzztimerpro.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.ScheduledAlarm
import com.felipeplazas.zzztimerpro.utils.LogExt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.*

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: ScheduledAlarm) {
        if (!alarm.enabled) {
            cancelAlarm(alarm.id)
            LogExt.logStructured(
                tag = "ALM",
                phase = "ALARM_SCHEDULE",
                event = "skip_disabled",
                metrics = mapOf("alarm_id" to alarm.id)
            )
            return
        }
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.felipeplazas.zzztimerpro.ALARM_TRIGGER"
            putExtra("ALARM_ID", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextAlarmTime = calculateNextAlarmTime(alarm)
        val now = System.currentTimeMillis()
        val drift = nextAlarmTime - now
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime,
                pendingIntent
            )
        }
        LogExt.logStructured(
            tag = "ALM",
            phase = "ALARM_SCHEDULE",
            event = "schedule",
            metrics = mapOf(
                "alarm_id" to alarm.id,
                "trigger_at_ms" to nextAlarmTime,
                "drift_ms" to drift,
                "repeat" to alarm.repeatDays.isNotEmpty(),
                "repeat_days" to alarm.repeatDays
            )
        )
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        LogExt.logStructured(
            tag = "ALM",
            phase = "ALARM_SCHEDULE",
            event = "cancel",
            metrics = mapOf("alarm_id" to alarmId)
        )
    }

    private fun calculateNextAlarmTime(alarm: ScheduledAlarm): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val original = calendar.timeInMillis
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        if (alarm.repeatDays.isNotEmpty()) {
            val repeatDaysList = alarm.repeatDays.split(",").mapNotNull { it.toIntOrNull() }
            if (repeatDaysList.isNotEmpty()) {
                var daysToAdd = 0
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                for (i in 0..6) {
                    val checkDay = (currentDayOfWeek + i - 1) % 7
                    if (repeatDaysList.contains(checkDay)) {
                        daysToAdd = i
                        break
                    }
                }
                if (daysToAdd > 0) {
                    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
                }
            }
        }
        val result = calendar.timeInMillis
        LogExt.logStructured(
            tag = "ALM",
            phase = "ALARM_SCHEDULE",
            event = "calc_time",
            metrics = mapOf(
                "alarm_id" to alarm.id,
                "base_ms" to original,
                "final_ms" to result,
                "added_days" to ((result - original) / (24*60*60*1000))
            )
        )
        return result
    }

    fun rescheduleAllAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val dao = database.scheduledAlarmDao()
                val enabledAlarms = dao.getEnabledAlarms().first()
                enabledAlarms.forEach { alarm ->
                    scheduleAlarm(alarm)
                }
                LogExt.logStructured(
                    tag = "ALM",
                    phase = "RESCHEDULE",
                    event = "reschedule_done",
                    metrics = mapOf("count" to enabledAlarms.size)
                )
            } catch (e: Exception) {
                LogExt.logStructured(
                    tag = "ALM",
                    phase = "ERROR",
                    event = "reschedule_exception",
                    severity = LogExt.Severity.ERROR,
                    metrics = mapOf("error" to (e.message ?: "unknown")),
                    msg = e.javaClass.simpleName
                )
                e.printStackTrace()
            }
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.felipeplazas.zzztimerpro.ALARM_TRIGGER" -> {
                val alarmId = intent.getLongExtra("ALARM_ID", -1)
                if (alarmId != -1L) {
                    LogExt.logStructured(
                        tag = "ALM",
                        phase = "ALARM_TRIGGER",
                        event = "receive",
                        metrics = mapOf("alarm_id" to alarmId)
                    )
                    triggerAlarm(context, alarmId)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                LogExt.logStructured(
                    tag = "ALM",
                    phase = "RESCHEDULE",
                    event = "boot_completed"
                )
                AlarmScheduler(context).rescheduleAllAlarms()
            }
        }
    }

    private fun triggerAlarm(context: Context, alarmId: Long) {
        val intent = Intent(context, com.felipeplazas.zzztimerpro.ui.alarm.AlarmRingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
        }
        context.startActivity(intent)
        LogExt.logStructured(
            tag = "ALM",
            phase = "ALARM_TRIGGER",
            event = "launch_activity",
            metrics = mapOf("alarm_id" to alarmId)
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val dao = database.scheduledAlarmDao()
                val alarm = dao.getAlarmById(alarmId)
                if (alarm != null && alarm.repeatDays.isNotEmpty()) {
                    AlarmScheduler(context).scheduleAlarm(alarm)
                    LogExt.logStructured(
                        tag = "ALM",
                        phase = "RESCHEDULE",
                        event = "repeat_reschedule",
                        metrics = mapOf("alarm_id" to alarmId)
                    )
                }
            } catch (e: Exception) {
                LogExt.logStructured(
                    tag = "ALM",
                    phase = "ERROR",
                    event = "trigger_exception",
                    severity = LogExt.Severity.ERROR,
                    metrics = mapOf(
                        "alarm_id" to alarmId,
                        "error" to (e.message ?: "unknown")
                    ),
                    msg = e.javaClass.simpleName
                )
                e.printStackTrace()
            }
        }
    }
}
