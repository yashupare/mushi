package com.mushi.app.core.server

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioSource
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.db.Database
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A matched [AudioSource.Basic] tagged with the id of the audio plugin that produced it.
 * The [pluginId] tag lets [MatchedTracksRepository] detect and discard matches that were
 * made by a plugin the user is no longer using, since a source id from one plugin (e.g. a
 * YouTube video id) is meaningless - or worse, silently wrong - when handed to a different
 * plugin's `getStreamsOfAudioSource`.
 */
@Serializable
data class PluginTaggedAudioSource(
    val pluginId: String,
    val source: AudioSource.Basic)

/** Persists (and looks up) the audio source matched to a [MetadataTrack] by a given plugin. */
interface TrackSourceRepository {
    suspend fun getTrackSource(track: MetadataTrack, pluginId: String): AudioSource.Basic?
    suspend fun saveTrackSource(track: MetadataTrack, source: AudioSource.Basic, pluginId: String)
}

class MatchedTracksRepository(private val database: Database) : TrackSourceRepository {
    /**
     * Returns the previously matched source for [track], but only if it was produced by
     * [pluginId]. A match saved by a different (or since-removed) plugin is treated as a
     * cache miss rather than being handed to the wrong plugin.
     */
    override suspend fun getTrackSource(track: MetadataTrack, pluginId: String): AudioSource.Basic? {
        return database.matchedTracksDataStore.data.map { prefs ->
            val json = prefs[stringPreferencesKey(track.id)] ?: return@map null
            val tagged = try {
                Json.decodeFromString<PluginTaggedAudioSource>(json)
            } catch (_: Exception) {
                // Legacy entries stored before plugin-tagging was introduced; treat as
                // unverifiable and force a fresh match instead of risking a wrong plugin.
                return@map null
            }
            if (tagged.pluginId != pluginId) return@map null
            tagged.source
        }.first()
    }

    override suspend fun saveTrackSource(track: MetadataTrack, source: AudioSource.Basic, pluginId: String) {
        database.matchedTracksDataStore.edit { prefs ->
            prefs[stringPreferencesKey(track.id)] =
                Json.encodeToString(PluginTaggedAudioSource(pluginId, source))
        }
    }
}