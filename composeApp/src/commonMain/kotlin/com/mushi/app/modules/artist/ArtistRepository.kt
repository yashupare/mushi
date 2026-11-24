package com.mushi.app.modules.artist

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistOverview
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
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
class ArtistRepository(
    private val pluginManager: PluginManager
) {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val plugin
        get() = pluginManager.selectedMetadataPlugin.value

    private val overviewCache = Cache.Builder<String, MetadataArtistOverview>().build()
    private val albumsCache =
        Cache.Builder<Pair<String, PaginationStrategy>, PaginationResult<MetadataAlbum.Detailed>>().build()
    private val relatedArtistsCache =
        Cache.Builder<Pair<String, PaginationStrategy>, PaginationResult<MetadataArtist.Basic>>().build()
    private val featuredPlaylistsCache =
        Cache.Builder<Pair<String, PaginationStrategy>, PaginationResult<MetadataPlaylist>>().build()
    private val topTracksCache = Cache.Builder<String, List<MetadataTrack>>().build()

    init {
        scope.launch {
            pluginManager.selectedMetadataPlugin
                .filterNotNull()
                .flatMapLatest { it.loggedInFlow }
                .distinctUntilChanged()
                .collect {
                    invalidateCaches()
                }
        }
    }

    fun invalidateCaches() {
        overviewCache.invalidateAll()
        albumsCache.invalidateAll()
        relatedArtistsCache.invalidateAll()
        featuredPlaylistsCache.invalidateAll()
        topTracksCache.invalidateAll()
    }

    suspend fun artistOverview(artistId: String) = plugin?.let { plugin ->
        overviewCache.get(artistId) {
            pluginManager.withScope {
                plugin.use {
                    metadataArtistAPI.artistOverview(artistId)
                }
            }
        }
    }

    suspend fun topTracks(artistId: String) = plugin?.let { plugin ->
        topTracksCache.get(artistId) {
            pluginManager.withScope {
                plugin.use {
                    metadataArtistAPI.getArtistTop10Tracks(artistId)
                }
            }
        }
    }

    suspend fun albums(
        artistId: String,
        paginationStrategy: PaginationStrategy? = null
    ) = plugin?.let { plugin ->
        albumsCache.get(
            artistId to (paginationStrategy ?: PaginationStrategy.Offset(0, 20))
        ) {
            pluginManager.withScope {
                plugin.use {
                    metadataArtistAPI.getArtistAlbums(artistId, paginationStrategy)
                }
            }
        }
    }

    suspend fun relatedArtists(
        artistId: String,
        paginationStrategy: PaginationStrategy? = null
    ) = plugin?.let { plugin ->
        relatedArtistsCache.get(
            artistId to (paginationStrategy ?: PaginationStrategy.Offset(0, 20))
        ) {
            pluginManager.withScope {
                plugin.use {
                    metadataArtistAPI.relatedArtists(artistId, paginationStrategy)
                }
            }
        }
    }

    suspend fun featuredPlaylists(
        artistId: String,
        paginationStrategy: PaginationStrategy? = null
    ) = plugin?.let { plugin ->
        featuredPlaylistsCache.get(
            artistId to (paginationStrategy ?: PaginationStrategy.Offset(0, 20))
        ) {
            pluginManager.withScope {
                plugin.use {
                    metadataArtistAPI.featuredPlaylists(artistId, paginationStrategy)
                }
            }
        }
    }
}
