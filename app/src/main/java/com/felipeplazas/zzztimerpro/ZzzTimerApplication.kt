package com.felipeplazas.zzztimerpro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.felipeplazas.zzztimerpro.utils.LocaleManager
import com.felipeplazas.zzztimerpro.utils.LogExt

class ZzzTimerApplication : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "timer_channel"
        lateinit var localeManager: LocaleManager
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Detectar y aplicar idioma del sistema automáticamente
        setupLanguage()
        
        // Configurar Logging (solo debug)
        LogExt.setEnabled(BuildConfig.DEBUG) // Requiere importar BuildConfig
        
        // Create notification channel
        createNotificationChannel()
        setupGlobalCrashHandler()
    }
    
    private fun setupLanguage() {
        try {
            // Detectar idioma del sistema
            val systemLocale = resources.configuration.locales[0]
            val appLanguage = if (systemLocale.language == "es") "es" else "en"
            
            // Aplicar idioma detectado
            localeManager.setLocaleFromCode(this, appLanguage)
            
            LogExt.logStructured(
                tag = "APP",
                phase = "INIT",
                event = "language_setup",
                metrics = mapOf(
                    "system_locale" to systemLocale.language,
                    "app_language" to appLanguage
                ),
                msg = "Idioma configurado automáticamente"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback a inglés si hay error
            localeManager.setLocaleFromCode(this, "en")
        }
    }
    
    override fun attachBaseContext(base: Context) {
        localeManager = LocaleManager()
        super.attachBaseContext(localeManager.applyLocale(base))
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
                enableVibration(false) // No vibrar en notificaciones de servicio
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            LogExt.logStructured(
                tag = "CRASH",
                phase = "UNCAUGHT",
                event = "exception",
                severity = LogExt.Severity.FATAL,
                metrics = mapOf(
                    "thread" to thread.name,
                    "message" to (throwable.message ?: "no_message"),
                    "type" to throwable.javaClass.simpleName
                ),
                msg = "Excepción no capturada"
            )
            // Delegar al handler original para que el sistema procese el crash (opcional)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
