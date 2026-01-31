package com.felipeplazas.zzztimerpro.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ActivityMainBinding
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.ui.sounds.AmbientSoundsActivity
import com.felipeplazas.zzztimerpro.ui.settings.SettingsActivity
import com.felipeplazas.zzztimerpro.ui.timer.TimerActivity
import com.felipeplazas.zzztimerpro.services.TimerService
import com.felipeplazas.zzztimerpro.ZzzTimerApplication
import com.felipeplazas.zzztimerpro.utils.PermissionManager
import java.util.*

/**
 * Main Activity - PAID APP (All features unlocked)
 */
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedDuration: Int = 30

    // Full duration options - no restrictions (paid app)
    private val durationOptions = intArrayOf(
        5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120
    )

    private var selectedSoundResId: Int = R.raw.soft_rain
    
    // Timer service binding
    private var timerService: TimerService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isServiceBound = true
            observeTimerState()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isServiceBound = false
        }
    }
    
    // Sound options mapping
    private val soundOptions = mapOf(
        R.string.sound_soft_rain to R.raw.soft_rain,
        R.string.sound_ocean_waves to R.raw.ocean_waves,
        R.string.sound_night_forest to R.raw.night_forest,
        R.string.sound_gentle_wind to R.raw.gentle_wind,
        R.string.sound_white_noise to R.raw.white_noise,
        R.string.sound_night_birds to R.raw.night_birds
    )

    private val requestNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        maybeRequestNotificationPermission()
        com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)
    }

    override fun onResume() {
        super.onResume()
        // Only bind if TimerService is actually running to avoid crashes
        try {
            if (TimerService.isServiceRunning) {
                val intent = Intent(this, TimerService::class.java)
                bindService(intent, serviceConnection, 0) // Use 0 instead of BIND_AUTO_CREATE to not start service
            } else {
                // Service not running, hide status row
                binding.timerStatusRow.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Fail silently, just hide the status row
            binding.timerStatusRow.visibility = View.GONE
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Unbind from service safely
        try {
            if (isServiceBound) {
                unbindService(serviceConnection)
                isServiceBound = false
            }
        } catch (e: Exception) {
            // Ignore unbind errors
            isServiceBound = false
        }
    }
    
    private fun observeTimerState() {
        timerService?.let { service ->
            lifecycleScope.launch {
                service.timerState.collect { state ->
                    updateTimerStatusUI(state.remainingMillis > 0)
                }
            }
        }
    }
    
    private fun updateTimerStatusUI(isRunning: Boolean) {
        binding.timerStatusRow.visibility = if (isRunning) View.VISIBLE else View.GONE
        
        // Disable duration controls when timer is running
        binding.btnDecreaseDuration.isEnabled = !isRunning
        binding.btnIncreaseDuration.isEnabled = !isRunning
        binding.seekBarDuration.isEnabled = !isRunning
        
        // Update start button text
        if (isRunning) {
            binding.btnStartTimer.text = getString(R.string.view_timer)
        } else {
            binding.btnStartTimer.text = getString(R.string.start_timer)
        }
    }

    private fun setupUI() {
        val initialIndex = durationOptions.indexOf(selectedDuration).coerceAtLeast(0)
        binding.seekBarDuration.max = durationOptions.size - 1
        binding.seekBarDuration.progress = initialIndex
        if (initialIndex >= 0 && initialIndex < durationOptions.size) {
            selectedDuration = durationOptions[initialIndex]
        }
        updateDurationDisplay(selectedDuration)
        updateSelectedSoundDisplay()
    }
    
    private fun updateSelectedSoundDisplay() {
        // Find the string resource key for the current sound ID
        val stringResId = soundOptions.entries.find { it.value == selectedSoundResId }?.key 
            ?: R.string.sound_soft_rain
        binding.tvSelectedSound.text = getString(stringResId)
    }

    private fun setupListeners() {
        // SeekBar listener
        binding.seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedDuration = durationOptions[progress]
                updateDurationDisplay(selectedDuration)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it) }
            }
        })

        // Duration buttons with haptic feedback
        binding.btnDecreaseDuration.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            val currentProgress = binding.seekBarDuration.progress
            if (currentProgress > 0) {
                binding.seekBarDuration.progress = currentProgress - 1
            }
        }

        binding.btnIncreaseDuration.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            val currentProgress = binding.seekBarDuration.progress
            if (currentProgress < durationOptions.size - 1) {
                binding.seekBarDuration.progress = currentProgress + 1
            }
        }
        
        // Stop Timer button (from main screen)
        binding.btnStopTimerFromMain.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            stopTimerFromMain()
        }
        
        // Sound Selector
        binding.cardSoundSelector.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            showSoundSelectionDialog()
        }

        // Start Timer button
        binding.btnStartTimer.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.confirm(it)
            startTimerActivity()
        }

        // Ambient sounds card
        binding.cardAmbientSounds.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            startActivity(Intent(this, AmbientSoundsActivity::class.java))
        }

        // Breathing exercises card
        binding.cardBreathing.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            startActivity(Intent(this, com.felipeplazas.zzztimerpro.ui.breathing.BreathingActivity::class.java))
        }

        // Sleep Tracking card
        binding.cardSleepTracking.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            startActivity(Intent(this, com.felipeplazas.zzztimerpro.ui.sleeptracking.SleepTrackingActivity::class.java))
        }
        
        // Statistics card
        binding.cardStatistics.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            startActivity(Intent(this, com.felipeplazas.zzztimerpro.ui.statistics.StatisticsActivity::class.java))
        }

        // Settings button
        binding.btnSettings.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun showSoundSelectionDialog() {
        val soundNames = soundOptions.keys.map { getString(it) }.toTypedArray()
        val soundIds = soundOptions.values.toList()
        
        val currentSoundIndex = soundIds.indexOf(selectedSoundResId).coerceAtLeast(0)

        android.app.AlertDialog.Builder(this)
            .setTitle(R.string.select_sound)
            .setSingleChoiceItems(soundNames, currentSoundIndex) { dialog, which ->
                selectedSoundResId = soundIds[which]
                updateSelectedSoundDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateDurationDisplay(duration: Int) {
        binding.tvTimerDisplay.text = duration.toString()
    }

    private fun stopTimerFromMain() {
        try {
            // Check if service is actually running to avoid crashes or unnecessary intents
            if (TimerService.isServiceRunning) {
                val stopIntent = Intent(this, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP
                }
                startService(stopIntent)
            }
            
            // Immediately hide status row for responsiveness
            binding.timerStatusRow.visibility = View.GONE
            binding.btnDecreaseDuration.isEnabled = true
            binding.btnIncreaseDuration.isEnabled = true
            binding.seekBarDuration.isEnabled = true
            binding.btnStartTimer.text = getString(R.string.start_timer)
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }

    private fun startTimerActivity() {
        val intent = Intent(this, TimerActivity::class.java).apply {
            putExtra(TimerActivity.EXTRA_DURATION_MINUTES, selectedDuration)
            putExtra(TimerActivity.EXTRA_SOUND_RES_ID, selectedSoundResId)
        }
        startActivity(intent)
    }

    private fun maybeRequestNotificationPermission() {
        if (PermissionManager.shouldRequestPostNotifications() && !PermissionManager.hasPostNotifications(this)) {
            PermissionManager.requestPostNotifications(requestNotificationsLauncher)
        }
    }
}