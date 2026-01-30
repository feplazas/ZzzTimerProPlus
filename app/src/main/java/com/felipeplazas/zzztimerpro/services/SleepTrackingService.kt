package com.felipeplazas.zzztimerpro.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.*
import androidx.core.app.NotificationCompat
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.ZzzTimerApplication
import com.felipeplazas.zzztimerpro.data.local.*
import com.felipeplazas.zzztimerpro.utils.LogExt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.*
import kotlin.math.sqrt

class SleepTrackingService : Service(), SensorEventListener {

    companion object {
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_TARGET_WAKE_TIME = "EXTRA_TARGET_WAKE_TIME"
        const val EXTRA_SMART_WAKE_ENABLED = "EXTRA_SMART_WAKE_ENABLED"

        private const val NOTIFICATION_ID = 1002
        private const val CYCLE_CHECK_INTERVAL_MS = 30000L // 30 seconds
        private const val MOVEMENT_THRESHOLD = 2.0f // m/s^2
    }

    private val binder = SleepTrackingBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val analysisScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var mediaRecorder: MediaRecorder? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var currentSessionId: Long = 0
    private var targetWakeTime: Long? = null
    private var smartWakeEnabled: Boolean = false
    private var isTracking = false

    private val _trackingState = MutableStateFlow(SleepTrackingState())
    val trackingState: StateFlow<SleepTrackingState> = _trackingState.asStateFlow()

    private var trackingJob: Job? = null
    private val cycleData = mutableListOf<CycleDataPoint>()

    // Accelerometer data
    private var lastAcceleration = FloatArray(3)
    private var currentAcceleration = 0f
    private var movementSum = 0f
    private var movementCount = 0
    
    // Calibration
    private var isCalibrating = true
    private var calibrationData = mutableListOf<Float>()
    private var baseNoiseLevel = 0f
    private val CALIBRATION_DURATION_MS = 10000L // 10 seconds calibration

    inner class SleepTrackingBinder : Binder() {
        fun getService(): SleepTrackingService = this@SleepTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        // Initialize WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZzzTimer::SleepTrackingWakeLock")
        
        LogExt.logStructured(
            tag = "SLP",
            phase = "INIT",
            event = "onCreate",
            metrics = mapOf(
                "accelerometer_available" to (accelerometer != null)
            )
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogExt.logStructured(
            tag = "SLP",
            phase = "START_CMD",
            event = intent?.action ?: "unknown",
            metrics = mapOf(
                "startId" to startId,
                "is_redelivery" to ((flags and START_FLAG_REDELIVERY) != 0)
            )
        )
        if ((flags and START_FLAG_REDELIVERY) != 0) {
            LogExt.logStructured(
                tag = "SLP", 
                phase = "LIFECYCLE", 
                event = "service_restored_after_kill", 
                metrics = mapOf("flags" to flags)
            )
        }
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                currentSessionId = intent.getLongExtra(EXTRA_SESSION_ID, 0)
                targetWakeTime = intent.getLongExtra(EXTRA_TARGET_WAKE_TIME, 0).takeIf { it > 0 }
                smartWakeEnabled = intent.getBooleanExtra(EXTRA_SMART_WAKE_ENABLED, false)
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "START_TRACKING",
                    event = "session_init",
                    metrics = mapOf(
                        "session_id" to currentSessionId,
                        "smart_wake" to smartWakeEnabled,
                        "target_wake_ms" to (targetWakeTime ?: -1)
                    )
                )
                startTracking()
            }
            ACTION_STOP_TRACKING -> {
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "STOP_TRACKING",
                    event = "request",
                    metrics = mapOf("session_id" to currentSessionId)
                )
                stopTracking()
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun startTracking() {
        if (isTracking) return
        isTracking = true
        
        // Acquire WakeLock
        wakeLock?.let {
            if (!it.isHeld) {
                it.acquire(10 * 60 * 60 * 1000L) // Limit to 10 hours safety
                LogExt.logStructured("SLP", "POWER", "wakelock_acquired")
            }
        }
        
        updateState(isTracking = true, currentPhase = SleepPhase.AWAKE)
        
        // Android 14+ requires specifying foreground service type for microphone
        if (Build.VERSION.SDK_INT >= 34) { // UPSIDE_DOWN_CAKE
            startForeground(NOTIFICATION_ID, createNotification(), 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        accelerometer?.let { sensor ->
            try {
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            } catch (e: Exception) {
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "ERROR",
                    event = "sensor_register_failed",
                    metrics = mapOf("error" to e.localizedMessage)
                )
            }
        } ?: run {
            LogExt.logStructured(
                tag = "SLP",
                phase = "WARN",
                event = "accelerometer_missing",
                metrics = mapOf()
            )
        }
        startAudioMonitoring()
        startCycleAnalysis()
        LogExt.logStructured(
            tag = "SLP",
            phase = "START_TRACKING",
            event = "start",
            metrics = mapOf(
                "session_id" to currentSessionId,
                "sensor_registered" to (accelerometer != null),
                "smart_wake" to smartWakeEnabled
            )
        )
        
        // Start calibration
        isCalibrating = true
        calibrationData.clear()
        serviceScope.launch {
            delay(CALIBRATION_DURATION_MS)
            finishCalibration()
        }
    }

    private fun stopTracking() {
        isTracking = false
        updateState(isTracking = false)
        
        // CRITICAL: Mark session as completed and needing aggregation
        // This ensures phases are calculated even if service is killed
        serviceScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SleepTrackingService)
                currentSessionId?.let { sessionId ->
                    val session = database.sleepSessionDao().getSessionById(sessionId)
                    session?.let {
                        val updated = it.copy(
                            endTime = System.currentTimeMillis(),
                            completed = true,
                            needsAggregation = true
                        )
                        database.sleepSessionDao().updateSession(updated)
                        LogExt.logStructured(
                            tag = "SLP",
                            phase = "STOP_TRACKING",
                            event = "session_marked_complete",
                            metrics = mapOf("session_id" to sessionId)
                        )
                    }
                }
            } catch (e: Exception) {
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "ERROR",
                    event = "stop_save_failed",
                    severity = LogExt.Severity.ERROR,
                    metrics = mapOf("error" to (e.message ?: "unknown"))
                )
            }
        }
        
        sensorManager.unregisterListener(this)
        stopAudioMonitoring()
        trackingJob?.cancel()
        
        // Release WakeLock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                LogExt.logStructured("SLP", "POWER", "wakelock_released")
            }
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        LogExt.logStructured(
            tag = "SLP",
            phase = "STOP_TRACKING",
            event = "stop",
            metrics = mapOf(
                "session_id" to currentSessionId,
                "cycle_points" to cycleData.size
            )
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate acceleration magnitude
            val acceleration = sqrt(x * x + y * y + z * z)
            val delta = Math.abs(acceleration - currentAcceleration)

            currentAcceleration = acceleration

            // Accumulate movement
            movementSum += delta
            movementCount++

            if (movementCount % 50 == 0) {
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "SENSOR",
                    event = "acc_sample",
                    metrics = mapOf(
                        "movement_sum" to movementSum,
                        "movement_count" to movementCount,
                        "current_acc" to String.format(Locale.US, "%.2f", currentAcceleration)
                    )
                )
            }
            
            if (isCalibrating) {
                calibrationData.add(currentAcceleration)
            }
        }
    }

    private fun finishCalibration() {
        isCalibrating = false
        if (calibrationData.isNotEmpty()) {
            // Calculate average noise level
            baseNoiseLevel = calibrationData.average().toFloat()
        }
        LogExt.logStructured(
            tag = "SLP",
            phase = "CALIBRATION",
            event = "complete",
            metrics = mapOf(
                "base_noise" to baseNoiseLevel,
                "samples" to calibrationData.size
            )
        )
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun startAudioMonitoring() {
        try {
            // Release existing recorder if any (safety for restarts)
            mediaRecorder?.release()
            mediaRecorder = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                mediaRecorder = MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null") 
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Catch ALL exceptions (IOException, SecurityException, RuntimeException, IllegalStateException)
            // to prevent Service crash. Tracking continues without audio level.
            LogExt.logStructured(
                tag = "SLP",
                phase = "ERROR",
                event = "audio_monitor_fail",
                severity = LogExt.Severity.ERROR,
                metrics = mapOf("error" to (e.message ?: "unknown")),
                msg = e.javaClass.simpleName
            )
            e.printStackTrace()
            mediaRecorder = null
        }
    }

    private fun stopAudioMonitoring() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCycleAnalysis() {
        trackingJob = analysisScope.launch {
            while (isTracking) {
                delay(CYCLE_CHECK_INTERVAL_MS)
                analyzeSleepCycle()
            }
        }
    }

    private suspend fun analyzeSleepCycle() {
        // Calculate average movement in the last period
        val avgMovement = if (movementCount > 0) {
            movementSum / movementCount
        } else {
            0f
        }

        // Get audio level (simplified - in production use actual amplitude)
        val audioLevel = try {
            mediaRecorder?.maxAmplitude?.toFloat()?.div(32768f) ?: 0f
        } catch (e: Exception) {
            0f
        }

        // Determine sleep phase based on movement and sound
        val phase = determineSleepPhase(avgMovement, audioLevel)

        // Store data point
        val dataPoint = CycleDataPoint(
            timestamp = System.currentTimeMillis(),
            movementScore = avgMovement.coerceIn(0f, 1f),
            soundLevel = audioLevel.coerceIn(0f, 1f),
            phase = phase
        )
        cycleData.add(dataPoint)

        // Update state
        updateState(currentPhase = phase)

        // Save immediately to prevent data loss if service is killed
        saveCyclesToDatabase()

        // Check if we should trigger smart wake-up
        if (smartWakeEnabled && targetWakeTime != null) {
            checkSmartWakeup(phase)
        }

        LogExt.logStructured(
            tag = "SLP",
            phase = "SLEEP_PHASE",
            event = "analyze",
            metrics = mapOf(
                "avg_movement" to String.format(Locale.US, "%.3f", avgMovement),
                "audio_level" to String.format(Locale.US, "%.3f", audioLevel),
                "phase" to phase.name,
                "points_buffer" to cycleData.size
            )
        )

        // Reset counters
        movementSum = 0f
        movementCount = 0
    }

    private fun determineSleepPhase(movement: Float, sound: Float): SleepPhase {
        // Adjust movement based on calibration
        val adjustedMovement = (movement - baseNoiseLevel).coerceAtLeast(0f)
        val combinedActivity = (adjustedMovement + sound) / 2f

        return when {
            combinedActivity > 0.5f -> SleepPhase.AWAKE
            combinedActivity > 0.25f -> SleepPhase.LIGHT
            combinedActivity > 0.1f -> SleepPhase.REM
            else -> SleepPhase.DEEP
        }
    }

    private suspend fun saveCyclesToDatabase() {
        if (cycleData.isEmpty()) return

        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@SleepTrackingService)
                val dao = database.sleepCycleDao()

                val cycles = cycleData.mapIndexed { index, point ->
                    val endTime = if (index < cycleData.size - 1) {
                        cycleData[index + 1].timestamp
                    } else {
                        System.currentTimeMillis()
                    }

                    SleepCycle(
                        sessionId = currentSessionId,
                        startTime = point.timestamp,
                        endTime = endTime,
                        phase = point.phase,
                        movementScore = point.movementScore,
                        soundLevel = point.soundLevel
                    )
                }

                dao.insertCycles(cycles)
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "SAVE_DB",
                    event = "insert_cycles",
                    metrics = mapOf(
                        "session_id" to currentSessionId,
                        "cycles_saved" to cycles.size
                    )
                )
                cycleData.clear()
            } catch (e: Exception) {
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "SAVE_DB",
                    event = "exception",
                    severity = LogExt.Severity.ERROR,
                    metrics = mapOf(
                        "session_id" to currentSessionId,
                        "buffer_size" to cycleData.size,
                        "error" to (e.message ?: "unknown")
                    ),
                    msg = e.javaClass.simpleName
                )
                e.printStackTrace()
            }
        }
    }

    private fun checkSmartWakeup(currentPhase: SleepPhase) {
        val now = System.currentTimeMillis()
        val targetTime = targetWakeTime ?: return
        val windowStart = targetTime - (30 * 60 * 1000) // 30 minutes before target

        if (now >= windowStart && now <= targetTime) {
            // We're in the wake window
            if (currentPhase == SleepPhase.LIGHT || currentPhase == SleepPhase.AWAKE) {
                LogExt.logStructured(
                    tag = "SLP",
                    phase = "SMART_WAKE",
                    event = "trigger",
                    metrics = mapOf(
                        "session_id" to currentSessionId,
                        "current_phase" to currentPhase.name,
                        "now_ms" to now,
                        "target_ms" to targetTime
                    )
                )
                triggerSmartAlarm()
            }
        }
    }

    private fun triggerSmartAlarm() {
        // Send broadcast to trigger alarm
        val intent = Intent("com.felipeplazas.zzztimerpro.SMART_WAKE_TRIGGER")
        sendBroadcast(intent)
        LogExt.logStructured(
            tag = "SLP",
            phase = "SMART_WAKE",
            event = "broadcast",
            metrics = mapOf(
                "session_id" to currentSessionId
            ),
            msg = "Emite SMART_WAKE_TRIGGER"
        )
        stopTracking()
    }

    private fun updateState(
        isTracking: Boolean? = null,
        currentPhase: SleepPhase? = null
    ) {
        _trackingState.value = _trackingState.value.copy(
            isTracking = isTracking ?: _trackingState.value.isTracking,
            currentPhase = currentPhase ?: _trackingState.value.currentPhase,
            sessionId = currentSessionId
        )
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, ZzzTimerApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.sleep_tracking_active))
            .setContentText(getString(R.string.monitoring_sleep_patterns))
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        // Attempt to save final data before cancelling scope
        // Note: usage of runBlocking here is risky for ANR but ensures save.
        // Given we save every 30s, we can skip this or use a non-cancellable scope if critical.
        // For now, removing the launch-after-cancel bug.
        analysisScope.cancel()
        serviceScope.cancel()
        
        LogExt.logStructured(
            tag = "SLP",
            phase = "SHUTDOWN",
            event = "onDestroy",
            metrics = mapOf("session_id" to currentSessionId)
        )
    }
}

data class SleepTrackingState(
    val isTracking: Boolean = false,
    val currentPhase: SleepPhase = SleepPhase.AWAKE,
    val sessionId: Long = 0
)

data class CycleDataPoint(
    val timestamp: Long,
    val movementScore: Float,
    val soundLevel: Float,
    val phase: SleepPhase
)
