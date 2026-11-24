package com.mushi.app.modules.album

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumRepository(
    val pluginManager: PluginManager,
    private val libraryRepository: LibraryRepository) {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val plugin
        get() = pluginManager.selectedMetadataPlugin.value

    private val albumInfoCache = Cache.Builder<String, Pair<MetadataAlbum.Detailed, Boolean>>().build()
    private val albumTracksCache = Cache.Builder<Pair<String, PaginationStrategy>, PaginationResult<MetadataTrack>>().build()

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

    private val authPlugin get() = if (isLoggedIn) plugin else null

    fun invalidateCaches() {
        albumInfoCache.invalidateAll()
        albumTracksCache.invalidateAll()
    }

    suspend fun getAlbumInfo(albumId: String) = plugin?.let { plugin ->
        albumInfoCache.get(albumId) {
            pluginManager.withScope {
                plugin.use {
                    val album = metadataAlbumAPI.getAlbum(albumId)
                    val isSaved = libraryRepository.isSavedAlbums(listOf(albumId)).firstOrNull() ?: false
                    album to isSaved
                }
            }
        }
    }

    suspend fun getAlbumTracks(albumId: String, paginationStrategy: PaginationStrategy? = null) =
        authPlugin?.let { plugin ->
            albumTracksCache.get(albumId to (paginationStrategy ?: PaginationStrategy.Offset(0, 20))) {
                pluginManager.withScope {
                    plugin.use {
                        metadataAlbumAPI.getAlbumTracks(
                            id = albumId,
                            pagination = paginationStrategy
                        )
                    }
                }
            }
        }

    suspend fun toggleSavedAlbum(albumId: String, currentIsSaved: Boolean) = plugin?.let { plugin ->
        if (currentIsSaved) {
            libraryRepository.removeSavedAlbums(listOf(albumId))
        } else {
            libraryRepository.saveAlbums(listOf(albumId))
        }
        !currentIsSaved
    }
}
