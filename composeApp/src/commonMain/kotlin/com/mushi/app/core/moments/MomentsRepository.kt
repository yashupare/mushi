package com.mushi.app.core.moments

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.backup.mergeMoments
import com.mushi.app.core.db.Database
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/** Kept moments and saved constellations. They're made by the listener and stay on the device. */
class MomentsRepository(private val database: Database) {
    private val json = Json { ignoreUnknownKeys = true }

    val library: Flow<MomentLibrary> = database.momentsDataStore.data.map(::read)

    suspend fun toggle(entry: QueueEntry, positionMs: Long) = update {
        it.toggled(entry, positionMs, now(), TimeZone.currentSystemDefault())
    }

    suspend fun remove(momentId: String) = update { it.without(momentId) }

    suspend fun saveConstellation(name: String, momentIds: List<String>) = update {
        it.withConstellation(now().toString(), name, momentIds)
    }

    suspend fun deleteConstellation(id: String) = update { library ->
        library.copy(constellations = library.constellations.filterNot { it.id == id })
    }

    suspend fun merge(restored: MomentLibrary) = update { mergeMoments(it, restored) }

    private suspend fun update(change: (MomentLibrary) -> MomentLibrary) {
        database.momentsDataStore.edit { prefs ->
            prefs[LIBRARY_KEY] = json.encodeToString(change(read(prefs)))
        }
    }

    /** Unlike a cache, an unreadable store is never silently replaced by an empty one on write. */
    private fun read(prefs: Preferences): MomentLibrary =
        prefs[LIBRARY_KEY]?.let { json.decodeFromString<MomentLibrary>(it) } ?: MomentLibrary()

    private fun now() = Clock.System.now().toEpochMilliseconds()

    private companion object {
        val LIBRARY_KEY = stringPreferencesKey("moment_library")
    }
}
