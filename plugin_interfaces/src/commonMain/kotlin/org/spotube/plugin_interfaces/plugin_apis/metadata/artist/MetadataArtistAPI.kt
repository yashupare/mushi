package org.spotube.plugin_interfaces.plugin_apis.metadata.artist

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

const val MetadataArtistAPI_SERVICE_NAME = "MetadataArtistAPI"

interface MetadataArtistAPI : ZiplineService {
    suspend fun getArtist(id: String): MetadataArtist.Detailed
    suspend fun artistOverview(id: String): MetadataArtistOverview
    suspend fun getArtistTop10Tracks(id: String): List<MetadataTrack>

    suspend fun relatedArtists(
        id: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataArtist.Basic>

    suspend fun featuredPlaylists(
        id: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataPlaylist>

    suspend fun getArtistAlbums(
        id: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataAlbum.Detailed>

    suspend fun savedArtists(pagination: PaginationStrategy? = null): PaginationResult<MetadataArtist.Detailed>

    suspend fun isSavedArtists(ids: List<String>): List<Boolean>
    suspend fun saveArtists(ids: List<String>)
    suspend fun removeSavedArtists(ids: List<String>)
}