package com.felipeplazas.zzztimerpro.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Gestor de locale y cambio de idioma para la aplicación.
 *
 * Permite cambiar el idioma en tiempo real sin reiniciar la app.
 * Soporta persistencia del idioma seleccionado y aplicación automática.
 *
 * Idiomas soportados:
 * - Inglés (en)
 * - Español (es)
 */
class LocaleManager {
    
    private val PREFS_NAME = "app_preferences"
    private val KEY_LANGUAGE = "selected_language"
    private val DEFAULT_LANGUAGE = "en"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Set the app locale to the specified language
     */
    fun setLocale(context: Context, languageCode: String): Context {
        saveLanguage(context, languageCode)
        return updateResources(context, languageCode)
    }
    
    fun setLocaleFromCode(context: Context, languageCode: String): Context {
        return updateResources(context, languageCode)
    }

    /**
     * Get the currently selected language
     */
    fun getCurrentLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Get the display name of the current language
     */
    fun getCurrentLanguageDisplayName(context: Context): String {
        return when (getCurrentLanguage(context)) {
            "en" -> "English"
            "es" -> "Español"
            else -> "English"
        }
    }
    
    /**
     * Get the flag emoji for the current language
     */
    fun getCurrentLanguageFlag(context: Context): String {
        return when (getCurrentLanguage(context)) {
            "en" -> "🇺🇸"
            "es" -> "🇪🇸"
            else -> "🇺🇸"
        }
    }
    
    /**
     * Save the selected language to SharedPreferences
     */
    private fun saveLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Update the app resources with the new locale
     */
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            return context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            configuration.setLayoutDirection(locale)
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            return context
        }
    }
    
    /**
     * Apply the saved locale to the context
     */
    fun applyLocale(context: Context): Context {
        val languageCode = getCurrentLanguage(context)
        return updateResources(context, languageCode)
    }
}
