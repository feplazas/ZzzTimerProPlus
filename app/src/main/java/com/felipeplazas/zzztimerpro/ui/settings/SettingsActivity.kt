package com.felipeplazas.zzztimerpro.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.ZzzTimerApplication
import com.felipeplazas.zzztimerpro.databinding.ActivitySettingsBinding
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.felipeplazas.zzztimerpro.utils.ThemeManager
import com.felipeplazas.zzztimerpro.data.settings.SettingsRepository

/**
 * Settings Activity - Simplified for paid app (no license section)
 */
class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var settingsRepository: SettingsRepository

    companion object {
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_DND_ENABLED = "dnd_enabled"
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
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, isChecked).apply()
        }

        // DND setting
        binding.switchDnd.setOnCheckedChangeListener { _, isChecked ->
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