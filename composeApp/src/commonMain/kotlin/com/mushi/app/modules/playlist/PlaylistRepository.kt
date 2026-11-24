package com.mushi.app.modules.playlist

import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.modules.library.LibraryRepository
import com.mushi.app.modules.plugin.PluginManager
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistRepository(
    val pluginManager: PluginManager,
    private val libraryRepository: LibraryRepository) {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val plugin
        get() = pluginManager.selectedMetadataPlugin.value

    private val playlistInfoCache = Cache.Builder<String, Pair<MetadataPlaylist, Boolean>>().build()
    private val playlistTracksCache = Cache.Builder<Pair<String, PaginationStrategy>, PaginationResult<MetadataTrack>>().build()

    init {
        scope.launch {
            pluginManager.selectedMetadataPlugin
                .filterNotNull()
                .flatMapLatest { it.loggedInFlow }
                .distinctUntilChanged()
                .collect { loggedIn ->
                    isLoggedIn = loggedIn
                    invalidateCaches()
                }
        }
    }

    private var isLoggedIn: Boolean = false

    fun invalidateCaches() {
        playlistInfoCache.invalidateAll()
        playlistTracksCache.invalidateAll()
    }

    suspend fun getPlaylistInfo(playlistId: String) = plugin?.let { plugin ->
        playlistInfoCache.get(playlistId) {
            pluginManager.withScope {
                plugin.use {
                    val playlist = metadataPlaylistAPI.getPlaylist(playlistId)
                    val isSaved = libraryRepository.isSavedPlaylists(listOf(playlistId)).firstOrNull() ?: false
                    playlist to isSaved
                }
            }
        }
    }

    suspend fun getPlaylistTracks(playlistId: String, paginationStrategy: PaginationStrategy? = null) =
        plugin?.let { plugin ->
            playlistTracksCache.get(playlistId to (paginationStrategy ?: PaginationStrategy.Offset(0, 20))) {
                pluginManager.withScope {
                    plugin.use {
                        metadataPlaylistAPI.getPlaylistTracks(
                            id = playlistId,
                            pagination = paginationStrategy
                        )
                    }
                }
            }
        }

    suspend fun toggleSavedPlaylist(playlistId: String, currentIsSaved: Boolean) = plugin?.let { plugin ->
        if (currentIsSaved) {
            libraryRepository.removeSavedPlaylists(listOf(playlistId))
        } else {
            libraryRepository.savePlaylists(listOf(playlistId))
        }
        !currentIsSaved
    }
}
