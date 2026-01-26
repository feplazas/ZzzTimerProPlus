package com.felipeplazas.zzztimerpro.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        applyTheme()
    }

    fun isOledBlackEnabled(): Boolean {
        return prefs.getBoolean(KEY_USE_OLED_BLACK, false)
    }

    fun setOledBlackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_OLED_BLACK, enabled).apply()
    }

    fun getCustomAccentColor(): Int? {
        val color = prefs.getInt(KEY_CUSTOM_ACCENT_COLOR, -1)
        return if (color != -1) color else null
    }

    fun setCustomAccentColor(color: Int?) {
        if (color != null) {
            prefs.edit().putInt(KEY_CUSTOM_ACCENT_COLOR, color).apply()
        } else {
            prefs.edit().remove(KEY_CUSTOM_ACCENT_COLOR).apply()
        }
    }

    fun applyTheme() {
        when (getThemeMode()) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_OLED -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // OLED is also dark
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun applyActivityTheme(activity: android.app.Activity) {
        when (getThemeMode()) {
            THEME_OLED -> activity.setTheme(com.felipeplazas.zzztimerpro.R.style.Theme_ZzzTimerPro_OLED)
            THEME_LIGHT -> activity.setTheme(com.felipeplazas.zzztimerpro.R.style.Theme_ZzzTimerPro_Light)
            THEME_DARK -> activity.setTheme(com.felipeplazas.zzztimerpro.R.style.Theme_ZzzTimerPro_Dark)
            else -> {} // System default, let the system handle it
        }
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_USE_OLED_BLACK = "use_oled_black"
        private const val KEY_CUSTOM_ACCENT_COLOR = "custom_accent_color"

        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2
        const val THEME_OLED = 3
    }
}
