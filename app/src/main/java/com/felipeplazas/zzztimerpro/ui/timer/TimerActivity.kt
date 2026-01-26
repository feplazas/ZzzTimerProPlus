package com.felipeplazas.zzztimerpro.ui.timer

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ActivityTimerBinding
import com.felipeplazas.zzztimerpro.services.TimerService
import com.felipeplazas.zzztimerpro.services.TimerState
import com.felipeplazas.zzztimerpro.services.AudioService
import com.felipeplazas.zzztimerpro.services.TimerPersistence
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.utils.LogExt
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TimerActivity : BaseActivity() {
    
    companion object {
        const val EXTRA_DURATION_MINUTES = "EXTRA_DURATION_MINUTES"
        const val EXTRA_SOUND_RES_ID = "EXTRA_SOUND_RES_ID"
        private const val DEFAULT_FADE_MINUTES = 5
    }
    
    private lateinit var binding: ActivityTimerBinding
    private var timerService: TimerService? = null
    private var isServiceBound = false
    
    // BroadcastReceiver para recibir actualizaciones del AudioService
    private val audioBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioService.BROADCAST_PLAYBACK_STATE -> {
                    val isPlaying = intent.getBooleanExtra(AudioService.EXTRA_IS_PLAYING, false)
                    val currentSoundId = intent.getIntExtra(AudioService.EXTRA_CURRENT_SOUND_ID, -1)
                    updateAudioPlaybackState(isPlaying, currentSoundId)
                }
            }
        }
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isServiceBound = true
            collectTimerState()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isServiceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupListeners()

        // Check if service is already running
        if (TimerService.isServiceRunning) {
            // Just bind to UI to sync state
            val serviceIntent = Intent(this, TimerService::class.java)
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            LogExt.logStructured("TMR_UI", "INIT", "bind_existing", msg = "Binding to existing service")
            return
        }
        
        // Restaurar si había temporizador previo (Crash-Loop recovery)
        if (TimerPersistence.isRunning(this)) {
            val remaining = TimerPersistence.computeRemaining(this)
            val total = TimerPersistence.getTotalDuration(this)
            if (remaining > 0 && total > 0) {
                LogExt.logStructured(
                    tag = "TMR_UI",
                    phase = "RESTORE",
                    event = "activity_create_restore",
                    metrics = mapOf(
                        "remaining_ms" to remaining,
                        "total_ms" to total
                    )
                )
                val fadeDurationMillis = DEFAULT_FADE_MINUTES * 60 * 1000L
                val serviceIntent = Intent(this, TimerService::class.java).apply {
                    action = TimerService.ACTION_START
                    putExtra(TimerService.EXTRA_DURATION_MILLIS, total) // total original
                    putExtra(TimerService.EXTRA_OVERRIDE_REMAINING, remaining)
                    putExtra(TimerService.EXTRA_FADE_DURATION_MILLIS, fadeDurationMillis)
                }
                startService(serviceIntent)
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                startAmbientSound()
            } else {
                LogExt.logStructured(
                    tag = "TMR_UI",
                    phase = "RESTORE",
                    event = "invalid_remaining_clear",
                    metrics = mapOf(
                        "remaining_ms" to remaining,
                        "total_ms" to total
                    )
                )
                TimerPersistence.clear(this)
                startTimerService()
            }
        } else {
            startTimerService()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Registrar el BroadcastReceiver para actualizaciones de audio
        val filter = IntentFilter(AudioService.BROADCAST_PLAYBACK_STATE)
        registerReceiver(audioBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }
    
    override fun onPause() {
        super.onPause()
        // Anular el registro del BroadcastReceiver de forma segura
        try {
            unregisterReceiver(audioBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver no estaba registrado, ignorar
            LogExt.logStructured("TMR_UI", "LIFECYCLE", "unregister_error", msg = "Receiver no registrado")
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupListeners() {
        binding.btnPauseResume.setOnClickListener {
            timerService?.let {
                if (it.timerState.value.isPaused) {
                    it.resumeTimer()
                } else {
                    it.pauseTimer()
                }
            }
        }
        
        binding.btnStop.setOnClickListener {
            timerService?.stopTimer()
            // También detener el audio
            stopAudio()
            finish()
        }
    }
    
    private fun startTimerService() {
        val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 30)
        val totalDurationMillis = durationMinutes * 60 * 1000L
        val fadeDurationMillis = DEFAULT_FADE_MINUTES * 60 * 1000L
        
        val serviceIntent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION_MILLIS, totalDurationMillis)
            putExtra(TimerService.EXTRA_FADE_DURATION_MILLIS, fadeDurationMillis)
        }
        
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        // Iniciar audio cuando comienza el temporizador
        startAmbientSound()
    }

    private fun startAmbientSound() {
        // Asegurar volumen al 100% al iniciar
        val volumeIntent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_SET_VOLUME
            putExtra(AudioService.EXTRA_VOLUME, 1.0f)
        }
        startService(volumeIntent)

        val soundResId = intent.getIntExtra(EXTRA_SOUND_RES_ID, R.raw.soft_rain)
        
        val audioIntent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_PLAY
            putExtra(AudioService.EXTRA_AUDIO_RES_ID, soundResId)
        }
        startService(audioIntent)
    }
    
    private fun stopAudio() {
        val audioIntent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_STOP
        }
        startService(audioIntent)
    }
    
    private fun collectTimerState() {
        lifecycleScope.launch {
            timerService?.timerState?.collectLatest { state ->
                updateTimerDisplay(state)
                if (state.isComplete) {
                    onTimerComplete()
                }
            }
        }
    }
    
    private fun updateTimerDisplay(state: TimerState) {
        // Update time text
        binding.tvTimerDisplay.text = formatTime(state.remainingMillis)
        
        // Update progress
        val progress = ((state.remainingMillis.toFloat() / state.totalMillis.toFloat()) * 100).toInt()
        binding.progressCircular.progress = progress
        
        // Update pause/resume button
        if (state.isPaused) {
            binding.btnPauseResume.setImageResource(R.drawable.ic_premium_play)
            binding.btnPauseResume.contentDescription = getString(R.string.resume_timer)
            binding.tvTimerStatus.text = getString(R.string.timer_paused)
        } else {
            binding.btnPauseResume.setImageResource(R.drawable.ic_premium_pause)
            binding.btnPauseResume.contentDescription = getString(R.string.pause_timer)
            binding.tvTimerStatus.text = getString(R.string.timer_running)
        }
        
        // Show fading indicator
        if (state.isFading) {
            binding.tvFadingIndicator.visibility = View.VISIBLE
            binding.tvFadingIndicator.text = getString(R.string.fading_audio)
            binding.tvTimerStatus.text = getString(R.string.timer_fading)
        } else {
            val fadeDurationMillis = DEFAULT_FADE_MINUTES * 60 * 1000L
            if (state.remainingMillis <= fadeDurationMillis && !state.isComplete) {
                // Pre-fade warning if needed, or just keep hidden until actual fade starts
                binding.tvFadingIndicator.visibility = View.GONE 
            } else {
                binding.tvFadingIndicator.visibility = View.GONE
            }
        }
    }
    
    private fun updateAudioPlaybackState(isPlaying: Boolean, currentSoundId: Int) {
        // Actualizar la UI según el estado de reproducción de audio
        if (isPlaying) {
            binding.tvAudioStatus.visibility = View.VISIBLE
            binding.tvAudioStatus.text = getString(R.string.audio_playing)
        } else {
            binding.tvAudioStatus.visibility = View.GONE
        }
        
        if (!isPlaying && currentSoundId == -1) {
            // Audio detenido
            if (binding.tvTimerStatus.text != getString(R.string.timer_paused)) {
                 // Only update if not paused (to avoid overwriting "Paused" status)
            }
        }
    }
    
    private fun onTimerComplete() {
        binding.tvTimerStatus.text = getString(R.string.timer_complete)
        binding.tvTimerDisplay.text = getString(R.string.timer_zero)
        binding.progressCircular.progress = 0
        binding.tvFadingIndicator.visibility = View.GONE
        
        // Disable buttons
        binding.btnPauseResume.isEnabled = false
        binding.btnStop.isEnabled = false
        
        // Detener audio cuando el temporizador termina
        stopAudio()
    }
    
    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}
