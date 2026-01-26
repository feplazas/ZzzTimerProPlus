package com.felipeplazas.zzztimerpro.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ActivityMainBinding
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.ui.sounds.AmbientSoundsActivity
import com.felipeplazas.zzztimerpro.ui.settings.SettingsActivity
import com.felipeplazas.zzztimerpro.ui.timer.TimerActivity
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
    }

    override fun onResume() {
        super.onResume()
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
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Duration buttons
        binding.btnDecreaseDuration.setOnClickListener {
            val currentProgress = binding.seekBarDuration.progress
            if (currentProgress > 0) {
                binding.seekBarDuration.progress = currentProgress - 1
            }
        }

        binding.btnIncreaseDuration.setOnClickListener {
            val currentProgress = binding.seekBarDuration.progress
            if (currentProgress < durationOptions.size - 1) {
                binding.seekBarDuration.progress = currentProgress + 1
            }
        }
        
        // Sound Selector
        binding.cardSoundSelector.setOnClickListener {
            showSoundSelectionDialog()
        }

        // Start Timer button
        binding.btnStartTimer.setOnClickListener {
            startTimerActivity()
        }

        // Ambient sounds card
        binding.cardAmbientSounds.setOnClickListener {
            startActivity(Intent(this, AmbientSoundsActivity::class.java))
        }

        // Breathing exercises card
        binding.cardBreathing.setOnClickListener {
            startActivity(Intent(this, com.felipeplazas.zzztimerpro.ui.breathing.BreathingActivity::class.java))
        }

        // Sleep Tracking card
        binding.cardSleepTracking.setOnClickListener {
            startActivity(Intent(this, com.felipeplazas.zzztimerpro.ui.sleeptracking.SleepTrackingActivity::class.java))
        }
        
        // Statistics card
        binding.cardStatistics.setOnClickListener {
            startActivity(Intent(this, com.felipeplazas.zzztimerpro.ui.statistics.StatisticsActivity::class.java))
        }

        // Settings button
        binding.btnSettings.setOnClickListener {
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