package com.felipeplazas.zzztimerpro.services

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.ZzzTimerApplication
import com.felipeplazas.zzztimerpro.ui.timer.TimerActivity
import com.felipeplazas.zzztimerpro.utils.LogExt
import java.util.Locale
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

// Data class to represent the current timer state
data class TimerState(
    val remainingMillis: Long,
    val totalMillis: Long,
    val isPaused: Boolean,
    val isComplete: Boolean = false,
    val isFading: Boolean = false
)

/**
 * Servicio foreground que gestiona el temporizador de sueño.
 *
 * Características:
 * - Cuenta regresiva con actualización cada segundo
 * - Fade gradual de volumen durante los últimos minutos (Logarítmico para sensación Premium)
 * - Controla AudioService directamente en lugar del volumen del sistema
 * - Persistencia de estado para sobrevivir kill del proceso
 * - Notificación persistente durante ejecución
 * - Vibración y notificación al completar
 *
 * @see TimerPersistence para persistencia de estado
 */
class TimerService : Service() {
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_DURATION_MILLIS = "EXTRA_DURATION_MILLIS"
        const val EXTRA_FADE_DURATION_MILLIS = "EXTRA_FADE_DURATION_MILLIS"
        const val EXTRA_OVERRIDE_REMAINING = "EXTRA_OVERRIDE_REMAINING"

        private const val NOTIFICATION_ID = 1001

        var isServiceRunning = false
    }

    private val binder = TimerBinder()
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val backgroundScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // StateFlow to hold and emit the timer state
    private lateinit var _timerState: MutableStateFlow<TimerState>
    val timerState: StateFlow<TimerState> by lazy { _timerState.asStateFlow() }

    private var totalDurationMillis: Long = 0
    private var fadeDurationMillis: Long = 5 * 60 * 1000 // Default 5 minutes

    private var isFading = false

    private lateinit var notificationManager: NotificationManager
    private lateinit var vibrator: Vibrator

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        LogExt.logStructured(
            tag = "TMR",
            phase = "INIT",
            event = "onCreate",
            metrics = mapOf("thread" to Thread.currentThread().name),
            msg = "TimerService creado"
        )
        restorePersistedTimerIfNeeded()
    }

    private fun restorePersistedTimerIfNeeded() {
        if (TimerPersistence.isRunning(this)) {
            val remaining = TimerPersistence.computeRemaining(this)
            val total = TimerPersistence.getTotalDuration(this)
            if (remaining > 0 && total > 0) {
                totalDurationMillis = total
                _timerState = MutableStateFlow(TimerState(remaining, total, false))
                LogExt.logStructured(
                    tag = "TMR",
                    phase = "RESTORE",
                    event = "restore_state",
                    metrics = mapOf(
                        "remaining_ms" to remaining,
                        "total_ms" to total
                    ),
                    msg = "Estado restaurado tras kill del proceso"
                )
                startForeground(NOTIFICATION_ID, createNotification(remaining))
                startCountdownLoop() // Reanudar loop
            } else {
                TimerPersistence.clear(this)
                LogExt.logStructured(
                    tag = "TMR",
                    phase = "RESTORE",
                    event = "clear_inconsistent",
                    metrics = mapOf(
                        "remaining_ms" to remaining,
                        "total_ms" to total
                    ),
                    severity = LogExt.Severity.WARN,
                    msg = "Persistencia inconsistente limpiada"
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogExt.logStructured(
            tag = "TMR",
            phase = "START_CMD",
            event = intent?.action ?: "unknown",
            metrics = mapOf(
                "startId" to startId,
                "has_intent" to (intent != null)
            )
        )
        when (intent?.action) {
            ACTION_START -> {
                totalDurationMillis = intent.getLongExtra(EXTRA_DURATION_MILLIS, 30 * 60 * 1000)
                fadeDurationMillis = intent.getLongExtra(EXTRA_FADE_DURATION_MILLIS, 5 * 60 * 1000)
                val overrideRemaining = intent.getLongExtra(EXTRA_OVERRIDE_REMAINING, -1)
                val initialRemaining = if (overrideRemaining in 1 until totalDurationMillis) overrideRemaining else totalDurationMillis
                _timerState = MutableStateFlow(TimerState(initialRemaining, totalDurationMillis, false))
                LogExt.logStructured(
                    tag = "TMR",
                    phase = "START_TIMER",
                    event = "init_state",
                    metrics = mapOf(
                        "total_ms" to totalDurationMillis,
                        "fade_ms" to fadeDurationMillis,
                        "override_remaining" to overrideRemaining,
                        "initial_remaining" to initialRemaining
                    )
                )
                startTimer()
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun startTimer() {
        isFading = false
        // Asegurar volumen al 100% al inicio
        sendVolumeToAudioService(1.0f)
        
        LogExt.logStructured(
            tag = "TMR",
            phase = "START_TIMER",
            event = "startTimer",
            metrics = mapOf(
                "remaining_ms" to _timerState.value.remainingMillis
            )
        )
        updateState(isPaused = false, isFading = false)
        startForeground(NOTIFICATION_ID, createNotification(_timerState.value.remainingMillis))
        // Guardar persistencia (endTimestamp = ahora + remaining)
        val endTs = System.currentTimeMillis() + _timerState.value.remainingMillis
        TimerPersistence.saveStart(this, endTs, totalDurationMillis)
        LogExt.logStructured(
            tag = "TMR",
            phase = "PERSISTENCE",
            event = "save_start",
            metrics = mapOf(
                "end_ts" to endTs,
                "total_ms" to totalDurationMillis
            )
        )
        startCountdownLoop()
    }

    private fun startCountdownLoop() {
        timerJob?.cancel()
        timerJob = backgroundScope.launch {
            var tickCounter = 0
            while (_timerState.value.remainingMillis > 0 && !_timerState.value.isPaused) {
                delay(1000)
                val newRemaining = _timerState.value.remainingMillis - 1000
                updateState(remainingMillis = newRemaining)
                tickCounter++
                if (tickCounter % 30 == 0) { // log cada 30s
                    LogExt.logStructured(
                        tag = "TMR",
                        phase = "TIMER_TICK",
                        event = "tick30s",
                        metrics = mapOf(
                            "remaining_ms" to _timerState.value.remainingMillis
                        )
                    )
                }
                if (_timerState.value.remainingMillis <= fadeDurationMillis && !isFading) {
                    isFading = true
                    updateState(isFading = true)
                    LogExt.logStructured(
                        tag = "TMR",
                        phase = "VOLUME_FADE",
                        event = "start_fade",
                        metrics = mapOf(
                            "fade_ms" to fadeDurationMillis,
                            "remaining_ms" to _timerState.value.remainingMillis
                        )
                    )
                    startVolumeFade()
                }
                updateNotification(_timerState.value.remainingMillis)
            }
            if (_timerState.value.remainingMillis <= 0) {
                onTimerComplete()
            }
        }
    }

    fun pauseTimer() {
        updateState(isPaused = true)
        timerJob?.cancel()
        LogExt.logStructured(
            tag = "TMR",
            phase = "STATE_UPDATE",
            event = "pause",
            metrics = mapOf("remaining_ms" to _timerState.value.remainingMillis)
        )
        updateNotification(_timerState.value.remainingMillis)
    }

    fun resumeTimer() {
        if (_timerState.value.isPaused) {
            LogExt.logStructured(
                tag = "TMR",
                phase = "STATE_UPDATE",
                event = "resume",
                metrics = mapOf("remaining_ms" to _timerState.value.remainingMillis)
            )
            continueTimer()
        }
    }

    private fun continueTimer() {
        updateState(isPaused = false)
        LogExt.logStructured(
            tag = "TMR",
            phase = "START_TIMER",
            event = "continueTimer",
            metrics = mapOf(
                "remaining_ms" to _timerState.value.remainingMillis,
                "fading" to isFading
            )
        )
        startForeground(NOTIFICATION_ID, createNotification(_timerState.value.remainingMillis))
        startCountdownLoop()
    }

    fun stopTimer() {
        timerJob?.cancel()
        LogExt.logStructured(
            tag = "TMR",
            phase = "SHUTDOWN",
            event = "stopTimer",
            metrics = mapOf("remaining_ms" to _timerState.value.remainingMillis)
        )
        TimerPersistence.clear(this)
        LogExt.logStructured(
            tag = "TMR",
            phase = "PERSISTENCE",
            event = "clear",
            metrics = mapOf("reason" to "stop")
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startVolumeFade() {
        backgroundScope.launch {
            val totalSteps = (fadeDurationMillis / 1000).toInt()
            // Empezamos desde el paso 1 hasta totalSteps
            for (i in 1..totalSteps) {
                if (_timerState.value.remainingMillis <= 0 || _timerState.value.isPaused) break
                
                // Cálculo de volumen logarítmico (taper de audio)
                // Usamos una curva cuadrática para que el descenso se sienta más natural al oído humano
                val remainingSteps = totalSteps - i
                val progress = remainingSteps.toFloat() / totalSteps.toFloat()
                val newVolume = progress.pow(2) // Curva cuadrática
                
                sendVolumeToAudioService(newVolume)
                
                if (i % 10 == 0) {
                    LogExt.logStructured(
                        tag = "TMR",
                        phase = "VOLUME_FADE",
                        event = "fade_step",
                        metrics = mapOf(
                            "step" to i,
                            "total_steps" to totalSteps,
                            "new_volume" to newVolume,
                            "remaining_ms" to _timerState.value.remainingMillis
                        )
                    )
                }
                delay(1000)
            }
            LogExt.logStructured(
                tag = "TMR",
                phase = "VOLUME_FADE",
                event = "fade_complete",
                metrics = mapOf("remaining_ms" to _timerState.value.remainingMillis)
            )
        }
    }

    private fun sendVolumeToAudioService(volume: Float) {
        val intent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_SET_VOLUME
            putExtra(AudioService.EXTRA_VOLUME, volume)
        }
        startService(intent)
    }

    private fun onTimerComplete() {
        sendVolumeToAudioService(0f) // Asegurar silencio
        vibrateDevice()
        updateState(isComplete = true, isFading = false)
        LogExt.logStructured(
            tag = "TMR",
            phase = "TIMER_COMPLETE",
            event = "complete",
            metrics = mapOf(
                "total_ms" to totalDurationMillis,
                "fade_ms" to fadeDurationMillis
            ),
            msg = "Temporizador completado"
        )
        TimerPersistence.clear(this)
        LogExt.logStructured(
            tag = "TMR",
            phase = "PERSISTENCE",
            event = "clear",
            metrics = mapOf("reason" to "complete")
        )
        showCompletionNotification()
        stopTimer()
    }

    private fun updateState(remainingMillis: Long? = null, isPaused: Boolean? = null, isComplete: Boolean? = null, isFading: Boolean? = null) {
        val currentState = _timerState.value
        _timerState.value = currentState.copy(
            remainingMillis = remainingMillis ?: currentState.remainingMillis,
            isPaused = isPaused ?: currentState.isPaused,
            isComplete = isComplete ?: currentState.isComplete,
            isFading = isFading ?: currentState.isFading
        )
        if (remainingMillis != null || isPaused != null || isComplete != null || isFading != null) {
            // Logging reducido para evitar spam en logcat, solo cambios de estado importantes
            if (isPaused != null || isComplete != null || isFading != null) {
                LogExt.logStructured(
                    tag = "TMR",
                    phase = "STATE_UPDATE",
                    event = "update_state",
                    metrics = mapOf(
                        "remaining_ms" to _timerState.value.remainingMillis,
                        "paused" to _timerState.value.isPaused,
                        "complete" to _timerState.value.isComplete,
                        "fading" to _timerState.value.isFading
                    )
                )
            }
        }
    }

    private fun vibrateDevice() {
        val prefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("vibration_enabled", true)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1)
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
        }
    }

    private fun createNotification(remainingMillis: Long): Notification {
        val intent = Intent(this, TimerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val timeText = formatTime(remainingMillis)

        return NotificationCompat.Builder(this, ZzzTimerApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_timer_running))
            .setContentText(getString(R.string.remaining_time) + ": $timeText")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(remainingMillis: Long) {
        val notification = createNotification(remainingMillis)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification() {
        val intent = Intent(this, TimerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, ZzzTimerApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.timer_complete))
            .setContentText(getString(R.string.time_to_sleep))
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Si el temporizador sigue corriendo, recrear notificación para mayor robustez y evitar ser matado
        if (this::_timerState.isInitialized && !_timerState.value.isPaused && !_timerState.value.isComplete && _timerState.value.remainingMillis > 0) {
            LogExt.logStructured(
                tag = "TMR",
                phase = "LIFECYCLE",
                event = "onTaskRemoved_reschedule",
                metrics = mapOf(
                    "remaining_ms" to _timerState.value.remainingMillis
                )
            )
            startForeground(NOTIFICATION_ID, createNotification(_timerState.value.remainingMillis))
        } else {
            LogExt.logStructured(
                tag = "TMR",
                phase = "LIFECYCLE",
                event = "onTaskRemoved_ignore",
                metrics = mapOf(
                    "initialized" to this::_timerState.isInitialized,
                    "remaining_ms" to if (this::_timerState.isInitialized) _timerState.value.remainingMillis else -1
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        timerJob?.cancel()
        serviceScope.cancel()
        backgroundScope.cancel()
        TimerPersistence.clear(this)
        LogExt.logStructured(
            tag = "TMR",
            phase = "PERSISTENCE",
            event = "clear",
            metrics = mapOf("reason" to "destroy")
        )
        LogExt.logStructured(
            tag = "TMR",
            phase = "SHUTDOWN",
            event = "onDestroy"
        )
    }
}
