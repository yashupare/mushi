package org.spotube.plugin_interfaces.plugin_apis.metadata.search

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult

const val MetadataSearchAPI_SERVICE_NAME = "MetadataSearchAPI"

interface MetadataSearchAPI : ZiplineService {
    val supportedSearchTypes: List<MetadataSupportedSearchType>

    suspend fun search(query: String): List<MetadataSearchResult>

    suspend fun searchTracks(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Track>

    suspend fun searchArtists(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Artist>

    suspend fun searchAlbums(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Album>

    suspend fun searchPlaylists(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.Playlist>

    suspend fun searchUsers(
        query: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataSearchResult.User>
}