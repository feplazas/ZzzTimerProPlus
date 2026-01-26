package com.felipeplazas.zzztimerpro.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.felipeplazas.zzztimerpro.ZzzTimerApplication
import com.felipeplazas.zzztimerpro.license.LicenseManager
import com.felipeplazas.zzztimerpro.utils.ThemeManager

/**
 * Base activity with common functionality:
 * - Locale application
 * - Theme application
 * - Premium access checks
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        try {
            super.attachBaseContext(ZzzTimerApplication.localeManager.applyLocale(newBase))
        } catch (e: Exception) {
            e.printStackTrace()
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            val themeManager = ThemeManager(this)
            themeManager.applyActivityTheme(this)
            themeManager.applyTheme()
            super.onCreate(savedInstanceState)
        } catch (e: Exception) {
            e.printStackTrace()
            super.onCreate(savedInstanceState)
        }
    }

    /**
     * Check if user has premium access
     */
    protected fun isPremium(): Boolean {
        return LicenseManager(this).isPremium()
    }
    
    /**
     * Check premium access - returns true if user has premium
     * No longer redirects to TrialActivity (trial removed)
     */
    protected fun checkPremiumAccess(): Boolean {
        return LicenseManager(this).isPremium()
    }
    
    /**
     * Require premium access - returns true if user has premium
     * If not premium, finishes activity
     */
    protected fun requirePremiumAccess(): Boolean {
        return if (LicenseManager(this).isPremium()) {
            true
        } else {
            finish()
            false
        }
    }
}
