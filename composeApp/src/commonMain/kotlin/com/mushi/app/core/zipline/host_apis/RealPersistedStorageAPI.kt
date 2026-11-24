package com.mushi.app.core.zipline.host_apis

import androidx.datastore.preferences.core.stringPreferencesKey
import org.spotube.plugin_interfaces.host_apis.PersistedStorageAPI
import com.mushi.app.core.db.Database
import com.mushi.app.modules.plugin.PluginEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RealPersistedStorageAPI(
    private val pluginInfo: PluginEntry
) : PersistedStorageAPI, KoinComponent {
    val database: Database by inject()

    override suspend fun putString(key: String, value: String) {
        return withContext(Dispatchers.IO) {
            database.pluginsDataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    this[stringPreferencesKey("${pluginInfo.id}:$key")] = value
                }
            }
        }
    }

    override suspend fun getString(key: String): String? {
        return withContext(Dispatchers.IO) {
            val prefs = database.pluginsDataStore.data.first()
            prefs[stringPreferencesKey("${pluginInfo.id}:$key")]
        }
    }

    override suspend fun remove(key: String) {
        return withContext(Dispatchers.IO) {
            database.pluginsDataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    remove(stringPreferencesKey("${pluginInfo.id}:$key"))
                }
            }
        }
    }

    override suspend fun getKeys(): List<String> {
        return withContext(Dispatchers.IO) {
            val prefs = database.pluginsDataStore.data.first()
            prefs.asMap().keys.mapNotNull { prefKey ->
                val keyString = prefKey.name
                if (keyString.startsWith("${pluginInfo.id}:")) {
                    keyString.removePrefix("${pluginInfo.id}:")
                } else null
            }
        }
    }
}