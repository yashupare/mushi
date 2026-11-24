package com.mushi.app.modules.library.local_tracks.media

import androidx.datastore.preferences.core.edit
import com.mushi.app.core.db.Database
import com.mushi.app.core.db.DatabaseKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class LocalMediaCacheRepository(private val database: Database) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    val cacheFlow: Flow<LocalMediaCache> = database.localMediaDataStore.data.map { prefs ->
        prefs[DatabaseKeys.LOCAL_MEDIA_CACHE_STATE_KEY]?.let { raw ->
            runCatching {
                json.decodeFromString<LocalMediaCache>(raw)
            }.getOrDefault(LocalMediaCache())
        } ?: LocalMediaCache()
    }

    suspend fun save(cache: LocalMediaCache) {
        database.localMediaDataStore.edit { prefs ->
            prefs[DatabaseKeys.LOCAL_MEDIA_CACHE_STATE_KEY] = json.encodeToString(cache)
            prefs[DatabaseKeys.LOCAL_MEDIA_LAST_SCAN_AT_KEY] = cache.indexedAtEpochMs
        }
    }
}
