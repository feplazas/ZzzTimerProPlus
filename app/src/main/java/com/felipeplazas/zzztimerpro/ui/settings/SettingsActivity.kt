package com.felipeplazas.zzztimerpro.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.ZzzTimerApplication
import com.felipeplazas.zzztimerpro.databinding.ActivitySettingsBinding
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.felipeplazas.zzztimerpro.utils.ThemeManager
import com.felipeplazas.zzztimerpro.data.settings.SettingsRepository
import java.util.concurrent.TimeUnit

/**
 * Settings Activity - Includes CPAP/BPAP Therapy Reminders
 */
class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var settingsRepository: SettingsRepository

    companion object {
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_DND_ENABLED = "dnd_enabled"
        
        // CPAP Preferences
        private const val KEY_CPAP_USER = "cpap_user_enabled"
        private const val KEY_CPAP_MASK_DAILY = "cpap_mask_daily"
        private const val KEY_CPAP_TUBE_WEEKLY = "cpap_tube_weekly"
        private const val KEY_CPAP_FILTER_MONTHLY = "cpap_filter_monthly"
        private const val KEY_CPAP_MASK_REPLACE = "cpap_mask_replace"
        
        // WorkManager Tags
        private const val WORK_TAG_CPAP_MASK_DAILY = "cpap_mask_daily_reminder"
        private const val WORK_TAG_CPAP_TUBE_WEEKLY = "cpap_tube_weekly_reminder"
        private const val WORK_TAG_CPAP_FILTER = "cpap_filter_reminder"
        private const val WORK_TAG_CPAP_MASK_REPLACE = "cpap_mask_replace_reminder"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            settingsRepository = SettingsRepository(this)
            
            binding = ActivitySettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            setupToolbar()
            setupPreferences()
            loadSettings()
            setupListeners()
            setupCpapListeners()
            com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)
        } catch (e: android.view.InflateException) {
            android.util.Log.e("SettingsActivity", "Layout inflation error", e)
            android.widget.Toast.makeText(this, "Error loading settings layout: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error opening settings", e)
            android.widget.Toast.makeText(this, "Error opening settings: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupPreferences() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun loadSettings() {
        binding.switchVibration.isChecked = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        binding.switchDnd.isChecked = prefs.getBoolean(KEY_DND_ENABLED, false)
        
        // Load CPAP settings
        val cpapEnabled = prefs.getBoolean(KEY_CPAP_USER, false)
        binding.switchCpapUser.isChecked = cpapEnabled
        binding.cpapRemindersContainer.visibility = if (cpapEnabled) View.VISIBLE else View.GONE
        
        binding.switchMaskDaily.isChecked = prefs.getBoolean(KEY_CPAP_MASK_DAILY, true)
        binding.switchTubeWeekly.isChecked = prefs.getBoolean(KEY_CPAP_TUBE_WEEKLY, true)
        binding.switchFilterMonthly.isChecked = prefs.getBoolean(KEY_CPAP_FILTER_MONTHLY, true)
        binding.switchMaskReplace.isChecked = prefs.getBoolean(KEY_CPAP_MASK_REPLACE, true)
    }

    private fun getLanguageDisplayName(code: String): String {
        return when (code) {
            "en" -> "English"
            "es" -> "Español"
            "pt" -> "Português"
            "de" -> "Deutsch"
            "fr" -> "Français"
            else -> "English"
        }
    }

    private fun setupListeners() {
        // Vibration setting
        binding.switchVibration.setOnCheckedChangeListener { view, isChecked ->
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(view)
            prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, isChecked).apply()
        }

        // DND setting
        binding.switchDnd.setOnCheckedChangeListener { view, isChecked ->
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(view)
            if (isChecked) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    binding.switchDnd.isChecked = false
                    android.app.AlertDialog.Builder(this)
                        .setTitle(R.string.permission_dnd_title)
                        .setMessage(R.string.permission_dnd_message)
                        .setPositiveButton(R.string.grant_permissions) { _, _ ->
                            startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                } else {
                     prefs.edit().putBoolean(KEY_DND_ENABLED, true).apply()
                }
            } else {
                 prefs.edit().putBoolean(KEY_DND_ENABLED, false).apply()
            }
        }
    }
    
    private fun setupCpapListeners() {
        // Main CPAP toggle
        binding.switchCpapUser.setOnCheckedChangeListener { view, isChecked ->
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(view)
            prefs.edit().putBoolean(KEY_CPAP_USER, isChecked).apply()
            binding.cpapRemindersContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            
            if (isChecked) {
                // Schedule all enabled reminders
                updateCpapReminders()
            } else {
                // Cancel all CPAP reminders
                cancelAllCpapReminders()
            }
        }
        
        // Individual reminder toggles
        binding.switchMaskDaily.setOnCheckedChangeListener { view, isChecked ->
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(view)
            prefs.edit().putBoolean(KEY_CPAP_MASK_DAILY, isChecked).apply()
            if (prefs.getBoolean(KEY_CPAP_USER, false)) {
                if (isChecked) scheduleDailyMaskReminder() else cancelReminder(WORK_TAG_CPAP_MASK_DAILY)
            }
        }
        
        binding.switchTubeWeekly.setOnCheckedChangeListener { view, isChecked ->
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(view)
            prefs.edit().putBoolean(KEY_CPAP_TUBE_WEEKLY, isChecked).apply()
            if (prefs.getBoolean(KEY_CPAP_USER, false)) {
                if (isChecked) scheduleWeeklyTubeReminder() else cancelReminder(WORK_TAG_CPAP_TUBE_WEEKLY)
            }
        }
        
        binding.switchFilterMonthly.setOnCheckedChangeListener { view, isChecked ->
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(view)
            prefs.edit().putBoolean(KEY_CPAP_FILTER_MONTHLY, isChecked).apply()
            if (prefs.getBoolean(KEY_CPAP_USER, false)) {
                if (isChecked) scheduleFilterReminder() else cancelReminder(WORK_TAG_CPAP_FILTER)
            }
        }
        
        binding.switchMaskReplace.setOnCheckedChangeListener { view, isChecked ->
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(view)
            prefs.edit().putBoolean(KEY_CPAP_MASK_REPLACE, isChecked).apply()
            if (prefs.getBoolean(KEY_CPAP_USER, false)) {
                if (isChecked) scheduleMaskReplaceReminder() else cancelReminder(WORK_TAG_CPAP_MASK_REPLACE)
            }
        }
    }
    
    private fun updateCpapReminders() {
        if (prefs.getBoolean(KEY_CPAP_MASK_DAILY, true)) scheduleDailyMaskReminder()
        if (prefs.getBoolean(KEY_CPAP_TUBE_WEEKLY, true)) scheduleWeeklyTubeReminder()
        if (prefs.getBoolean(KEY_CPAP_FILTER_MONTHLY, true)) scheduleFilterReminder()
        if (prefs.getBoolean(KEY_CPAP_MASK_REPLACE, true)) scheduleMaskReplaceReminder()
    }
    
    private fun scheduleDailyMaskReminder() {
        val workRequest = PeriodicWorkRequestBuilder<CpapReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateDelayToMorning(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("reminder_type" to "mask_daily"))
            .addTag(WORK_TAG_CPAP_MASK_DAILY)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_TAG_CPAP_MASK_DAILY,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    private fun scheduleWeeklyTubeReminder() {
        val workRequest = PeriodicWorkRequestBuilder<CpapReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(calculateDelayToSunday(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("reminder_type" to "tube_weekly"))
            .addTag(WORK_TAG_CPAP_TUBE_WEEKLY)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_TAG_CPAP_TUBE_WEEKLY,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    private fun scheduleFilterReminder() {
        val workRequest = PeriodicWorkRequestBuilder<CpapReminderWorker>(30, TimeUnit.DAYS)
            .setInputData(workDataOf("reminder_type" to "filter"))
            .addTag(WORK_TAG_CPAP_FILTER)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_TAG_CPAP_FILTER,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    private fun scheduleMaskReplaceReminder() {
        val workRequest = PeriodicWorkRequestBuilder<CpapReminderWorker>(90, TimeUnit.DAYS)
            .setInputData(workDataOf("reminder_type" to "mask_replace"))
            .addTag(WORK_TAG_CPAP_MASK_REPLACE)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_TAG_CPAP_MASK_REPLACE,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    private fun cancelReminder(tag: String) {
        WorkManager.getInstance(this).cancelUniqueWork(tag)
    }
    
    private fun cancelAllCpapReminders() {
        val workManager = WorkManager.getInstance(this)
        workManager.cancelUniqueWork(WORK_TAG_CPAP_MASK_DAILY)
        workManager.cancelUniqueWork(WORK_TAG_CPAP_TUBE_WEEKLY)
        workManager.cancelUniqueWork(WORK_TAG_CPAP_FILTER)
        workManager.cancelUniqueWork(WORK_TAG_CPAP_MASK_REPLACE)
    }
    
    private fun calculateDelayToMorning(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Set to 8:00 AM tomorrow
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 8)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        
        return calendar.timeInMillis - now
    }
    
    private fun calculateDelayToSunday(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Find next Sunday at 10:00 AM
        val daysUntilSunday = (java.util.Calendar.SUNDAY - calendar.get(java.util.Calendar.DAY_OF_WEEK) + 7) % 7
        calendar.add(java.util.Calendar.DAY_OF_YEAR, if (daysUntilSunday == 0) 7 else daysUntilSunday)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 10)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        
        return calendar.timeInMillis - now
    }
    
    override fun onResume() {
        super.onResume()
        // Update DND switch based on actual permission status if it was pending
        if (binding.switchDnd.isChecked) {
             val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
             if (!notificationManager.isNotificationPolicyAccessGranted) {
                 binding.switchDnd.isChecked = false
                 prefs.edit().putBoolean(KEY_DND_ENABLED, false).apply()
             }
        }
        
        lifecycleScope.launch {
            val lang = settingsRepository.languageFlow.first()
            ZzzTimerApplication.localeManager.setLocaleFromCode(this@SettingsActivity, lang)
        }
    }
}