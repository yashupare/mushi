package com.mushi.app.core.history

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.db.Database
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** The songs played on this device. Stays on the device. */
class ListeningHistoryRepository(private val database: Database) {
    private val json = Json { ignoreUnknownKeys = true }

    val history: Flow<List<HistoryItem>> = database.listeningHistoryDataStore.data.map(::read)

    suspend fun record(entry: QueueEntry, playedAtMs: Long) {
        database.listeningHistoryDataStore.edit { prefs ->
            prefs[HISTORY_KEY] = json.encodeToString(read(prefs).withPlay(entry, playedAtMs))
        }
    }

    suspend fun clear() {
        database.listeningHistoryDataStore.edit { it.remove(HISTORY_KEY) }
    }

    private fun read(prefs: Preferences): List<HistoryItem> = prefs[HISTORY_KEY]
        ?.let { runCatching { json.decodeFromString<List<HistoryItem>>(it) }.getOrNull() }
        .orEmpty()

    private companion object {
        val HISTORY_KEY = stringPreferencesKey("listening_history")
    }
}
