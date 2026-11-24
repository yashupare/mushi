package com.mushi.app.modules.settings

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mushi.app.core.db.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

/** Narrow read-only view over the current [UserSettings], for consumers (like
 * [com.mushi.app.core.server.StreamingUrlRepository]) that only need to read the
 * user's streaming preferences without depending on the full [SettingsRepository]. */
interface UserSettingsSource {
    val userSettings: StateFlow<UserSettings>
}

/** [UserSettingsSource] that can also save, for consumers that update a setting or two. */
interface UserSettingsStore : UserSettingsSource {
    suspend fun updateSettings(newSettings: UserSettings)
}

class SettingsRepository(private val database: Database) : UserSettingsStore {
    companion object {
        private val SETTINGS_KEY = stringPreferencesKey("user_settings")

        /**
         * Lenient so settings written by older builds (which stored `accentColor` /
         * `surfaceTheme` instead of the Millennium `palette`) still decode, and so a
         * missing field falls back to its default.
         */
        private val settingsJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        /**
         * Jam server values that were only ever the app's default, never a user's choice:
         * Spotube's public server (saved by builds from before the rebrand, since defaults are
         * written out) and blank (saved while Mushi had no server yet). Both mean "use the default".
         */
        private val SUPERSEDED_JAM_SERVER_URLS = setOf("https://jam.spotube.cc", "")

        private fun decode(json: String): UserSettings {
            val settings = settingsJson.decodeFromString<UserSettings>(json)
            return if (settings.jamServerUrl.trim().trimEnd('/') in SUPERSEDED_JAM_SERVER_URLS) {
                settings.copy(jamServerUrl = DEFAULT_JAM_SERVER_URL)
            } else {
                settings
            }
        }
    }

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val userSettings: StateFlow<UserSettings> = database.settingsDataStore.data.map { prefs ->
        val json = prefs[SETTINGS_KEY]
        if (json != null) {
            decode(json)
        } else {
            UserSettings() // Default value
        }
    }.stateIn(
        scope,
        started = SharingStarted.Eagerly,
        initialValue = UserSettings() // Default value
    )

    override suspend fun updateSettings(newSettings: UserSettings) {
        database.settingsDataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = settingsJson.encodeToString(newSettings)
        }
    }

    suspend fun getCurrentSettings(): UserSettings {
        val preferences = database.settingsDataStore.data.first()
        val storedJson = preferences[SETTINGS_KEY] ?: return UserSettings()
        return decode(storedJson)
    }
}
