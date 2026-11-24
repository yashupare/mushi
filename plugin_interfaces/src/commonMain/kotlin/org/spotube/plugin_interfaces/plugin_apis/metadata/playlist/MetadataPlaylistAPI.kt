package org.spotube.plugin_interfaces.plugin_apis.metadata.playlist

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

const val MetadataPlaylistAPI_SERVICE_NAME = "MetadataPlaylistAPI"

interface MetadataPlaylistAPI : ZiplineService {
    suspend fun getPlaylist(id: String): MetadataPlaylist

    suspend fun getPlaylistTracks(
        id: String,
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataTrack>

    suspend fun savedPlaylists(
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataPlaylist>

    suspend fun isSavedPlaylists(ids: List<String>): List<Boolean>
    suspend fun savePlaylists(ids: List<String>)
    suspend fun removeSavedPlaylists(ids: List<String>)

    suspend fun createPlaylist(
        name: String,
        description: String?,
        isPublic: Boolean,
        isCollaborating: Boolean,
        imageBase64: String,
        trackIds: List<String>,
    ): MetadataPlaylist

    suspend fun updatePlaylist(
        id: String,
        name: String?,
        description: String?,
        isPublic: Boolean?,
        isCollaborating: Boolean?,
        imageBase64: String?,
        trackIds: List<String>?
    ): MetadataPlaylist

    suspend fun deletePlaylist(id: String)

    suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>)
    suspend fun removeTracksFromPlaylist(playlistId: String, trackIds: List<String>)
}