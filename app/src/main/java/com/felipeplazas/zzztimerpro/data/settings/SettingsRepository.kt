package com.felipeplazas.zzztimerpro.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

/**
 * Repositorio para configuraciones de la aplicación usando DataStore.
 *
 * Gestiona preferencias de usuario:
 * - Idioma seleccionado (language)
 * - Tema de la aplicación (theme: light/dark/system)
 *
 * Usa Preferences DataStore para persistencia eficiente y reactiva.
 *
 * @see languageFlow para observar cambios de idioma
 * @see getThemeFlow para observar cambios de tema
 */
class SettingsRepository(private val context: Context) {

    object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "en"
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = lang
        }
    }

}
