package com.mushi.app.modules.search

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.search.MetadataSearchResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.search.MetadataSupportedSearchType
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import org.spotube.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import com.mushi.app.modules.plugin.PluginManager
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import com.mushi.app.core.i18n.getString
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.search_error_no_metadata_plugin

class SearchRepository(
    private val pluginManager: PluginManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val supportedTypesCache = Cache.Builder<Unit, List<MetadataSupportedSearchType>>().build()
    private val allSearchCache = Cache.Builder<String, List<MetadataSearchResult>>().build()
    private val trackSearchCache = Cache.Builder<Pair<String, PaginationStrategy?>, PaginationResult<MetadataSearchResult.Track>>().build()
    private val albumSearchCache = Cache.Builder<Pair<String, PaginationStrategy?>, PaginationResult<MetadataSearchResult.Album>>().build()
    private val artistSearchCache = Cache.Builder<Pair<String, PaginationStrategy?>, PaginationResult<MetadataSearchResult.Artist>>().build()
    private val playlistSearchCache = Cache.Builder<Pair<String, PaginationStrategy?>, PaginationResult<MetadataSearchResult.Playlist>>().build()
    private val userSearchCache = Cache.Builder<Pair<String, PaginationStrategy?>, PaginationResult<MetadataSearchResult.User>>().build()

    init {
        scope.launch {
            pluginManager.selectedMetadataPlugin
                .filterNotNull()
                .distinctUntilChanged()
                .collect { invalidateCaches() }
        }
    }

    fun invalidateCaches() {
        supportedTypesCache.invalidateAll()
        allSearchCache.invalidateAll()
        trackSearchCache.invalidateAll()
        albumSearchCache.invalidateAll()
        artistSearchCache.invalidateAll()
        playlistSearchCache.invalidateAll()
        userSearchCache.invalidateAll()
    }

    suspend fun loadSupportedSearchTypes(): List<MetadataSupportedSearchType> {
        return supportedTypesCache.get(Unit) {
            val plugin = pluginManager.selectedMetadataPlugin.value
                ?: throw IllegalStateException(getString(Res.string.search_error_no_metadata_plugin))
            pluginManager.withScope {
                plugin.use { metadataSearchAPI.supportedSearchTypes }
            }
        }
    }

    suspend fun searchAll(query: String): List<MetadataSearchResult> {
        return allSearchCache.get(query) {
            val plugin = pluginManager.selectedMetadataPlugin.value
                ?: throw IllegalStateException(getString(Res.string.search_error_no_metadata_plugin))
            pluginManager.withScope {
                plugin.use { metadataSearchAPI.search(query) }
            }
        }
    }

    suspend fun searchTracks(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Track> {
        val key = query to pagination
        return trackSearchCache.get(key) {
            val plugin = pluginManager.selectedMetadataPlugin.value
                ?: throw IllegalStateException(getString(Res.string.search_error_no_metadata_plugin))
            pluginManager.withScope {
                plugin.use { metadataSearchAPI.searchTracks(query, pagination) }
            }
        }
    }

    suspend fun searchAlbums(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Album> {
        val key = query to pagination
        return albumSearchCache.get(key) {
            val plugin = pluginManager.selectedMetadataPlugin.value
                ?: throw IllegalStateException(getString(Res.string.search_error_no_metadata_plugin))
            pluginManager.withScope {
                plugin.use { metadataSearchAPI.searchAlbums(query, pagination) }
            }
        }
    }

    suspend fun searchArtists(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Artist> {
        val key = query to pagination
        return artistSearchCache.get(key) {
            val plugin = pluginManager.selectedMetadataPlugin.value
                ?: throw IllegalStateException(getString(Res.string.search_error_no_metadata_plugin))
            pluginManager.withScope {
                plugin.use { metadataSearchAPI.searchArtists(query, pagination) }
            }
        }
    }

    suspend fun searchPlaylists(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Playlist> {
        val key = query to pagination
        return playlistSearchCache.get(key) {
            val plugin = pluginManager.selectedMetadataPlugin.value
                ?: throw IllegalStateException(getString(Res.string.search_error_no_metadata_plugin))
            pluginManager.withScope {
                plugin.use { metadataSearchAPI.searchPlaylists(query, pagination) }
            }
        }
    }

    suspend fun searchUsers(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.User> {
        val key = query to pagination
        return userSearchCache.get(key) {
            val plugin = pluginManager.selectedMetadataPlugin.value
                ?: throw IllegalStateException(getString(Res.string.search_error_no_metadata_plugin))
            pluginManager.withScope {
                plugin.use { metadataSearchAPI.searchUsers(query, pagination) }
            }
        }
    }
}
