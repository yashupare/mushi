package com.mushi.app.core.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mushi.app.core.paths.Paths
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
class Database(val paths: Paths) {
    init {
        migrateLegacyDataFiles(paths)
    }

    /**
     * True when the app had already written its own data before this process started. Captured
     * synchronously at construction time — the earliest point, before any eager service can
     * create a fresh store — so it reliably tells a brand-new install apart from an upgrade of
     * an existing one (used to skip the first-run onboarding on upgrades).
     */
    val isExistingInstall: Boolean = runCatching {
        val dataDirPath = paths.getApplicationDataDirPath()
        EXISTING_INSTALL_PROBE_FILES.any { fileName ->
            FileSystem.SYSTEM.exists("$dataDirPath/$fileName".toPath())
        }
    }.getOrDefault(false)

    val settingsDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${paths.getApplicationDataDirPath()}/mushi.preferences_pb".toPath() }
        )
    }
    val pluginsDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${paths.getApplicationDataDirPath()}/mushi_plugins.preferences_pb".toPath() }
        )
    }

    val matchedTracksDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${paths.getApplicationDataDirPath()}/mushi_matched_tracks.preferences_pb".toPath() }
        )
    }

    val audioPlayerQueueDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { dropIfOversized("${paths.getApplicationDataDirPath()}/mushi_audio_player_queue.preferences_pb".toPath()) }
        )
    }

    val localMediaDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { dropIfOversized("${paths.getApplicationDataDirPath()}/mushi_local_media.preferences_pb".toPath()) }
        )
    }

    val blacklistDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${paths.getApplicationDataDirPath()}/mushi_blacklist.preferences_pb".toPath() }
        )
    }

    val listeningHistoryDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { dropIfOversized("${paths.getApplicationDataDirPath()}/mushi_history.preferences_pb".toPath()) }
        )
    }

    val windowStateDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${paths.getApplicationDataDirPath()}/mushi_window.preferences_pb".toPath() }
        )
    }

    /**
     * DataStore reads a whole preferences file into the heap. Earlier builds stored full-size
     * embedded covers as JSON number arrays in the queue and local media stores, growing them to
     * hundreds of MB and crashing with OutOfMemoryError on load (#3193). Both are rebuildable, so
     * drop one that is far past any sane size instead of loading it.
     */
    private fun dropIfOversized(path: okio.Path): okio.Path {
        runCatching {
            val size = FileSystem.SYSTEM.metadataOrNull(path)?.size ?: return path
            if (size > MAX_CACHE_STORE_BYTES) FileSystem.SYSTEM.delete(path)
        }
        return path
    }

    private companion object {
        const val MAX_CACHE_STORE_BYTES = 8L * 1024 * 1024

        /** Preference files written by previous launches; their presence means an upgrade. */
        val EXISTING_INSTALL_PROBE_FILES = LEGACY_DATA_FILE_NAMES.values.toList()
    }
}

/**
 * Data files the app wrote under its former name, mapped to their current names. They are
 * renamed in place once, so an existing install keeps its settings, plugins and queue.
 */
private val LEGACY_DATA_FILE_NAMES = mapOf(
    "spotube.preferences_pb" to "mushi.preferences_pb",
    "spotube_plugins.preferences_pb" to "mushi_plugins.preferences_pb",
    "spotube_matched_tracks.preferences_pb" to "mushi_matched_tracks.preferences_pb",
    "spotube_audio_player_queue.preferences_pb" to "mushi_audio_player_queue.preferences_pb",
    "spotube_local_media.preferences_pb" to "mushi_local_media.preferences_pb",
    "spotube_blacklist.preferences_pb" to "mushi_blacklist.preferences_pb",
    "spotube_window.preferences_pb" to "mushi_window.preferences_pb",
    "spotube_cookies.preferences_pb" to "mushi_cookies.preferences_pb")

/**
 * Renames data files left by the former app name. Idempotent and safe to call from every store
 * that may open first: a file is only moved when its new name doesn't exist yet.
 */
internal fun migrateLegacyDataFiles(paths: Paths) {
    runCatching {
        val dir = paths.getApplicationDataDirPath()
        LEGACY_DATA_FILE_NAMES.forEach { (old, new) ->
            val from = "$dir/$old".toPath()
            val to = "$dir/$new".toPath()
            if (FileSystem.SYSTEM.exists(from) && !FileSystem.SYSTEM.exists(to)) {
                FileSystem.SYSTEM.atomicMove(from, to)
            }
        }
    }
}

object DatabaseKeys {
    val PLUGINS_STATE_KEY = stringPreferencesKey("plugins_state")
    val AUDIO_PLAYER_QUEUE_STATE_KEY = stringPreferencesKey("audio_player_queue_state")
    val LOCAL_MEDIA_CACHE_STATE_KEY = stringPreferencesKey("local_media_cache_state")
    val LOCAL_MEDIA_LAST_SCAN_AT_KEY = longPreferencesKey("local_media_last_scan_at")
    val WINDOW_STATE_KEY = stringPreferencesKey("window_state")
}
