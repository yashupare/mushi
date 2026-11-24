package com.mushi.app.core.audioplayer

import androidx.datastore.preferences.core.edit
import com.mushi.app.core.db.Database
import com.mushi.app.core.db.DatabaseKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

interface QueueStateRepository {
    suspend fun getPersistedState(): PersistedQueueState?
    suspend fun saveState(state: PersistedQueueState)
    suspend fun clearState()
}

class AudioPlayerQueueRepository(private val database: Database) : QueueStateRepository {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getPersistedState(): PersistedQueueState? {
        return database.audioPlayerQueueDataStore.data.map { preferences ->
            val payload = preferences[DatabaseKeys.AUDIO_PLAYER_QUEUE_STATE_KEY]
            payload?.let {
                runCatching { json.decodeFromString<PersistedQueueState>(it) }
                    .getOrNull()
            }
        }.first()
    }

    override suspend fun saveState(state: PersistedQueueState) {
        database.audioPlayerQueueDataStore.edit { preferences ->
            preferences[DatabaseKeys.AUDIO_PLAYER_QUEUE_STATE_KEY] = json.encodeToString(state)
        }
    }

    override suspend fun clearState() {
        database.audioPlayerQueueDataStore.edit { preferences ->
            preferences.remove(DatabaseKeys.AUDIO_PLAYER_QUEUE_STATE_KEY)
        }
    }
}