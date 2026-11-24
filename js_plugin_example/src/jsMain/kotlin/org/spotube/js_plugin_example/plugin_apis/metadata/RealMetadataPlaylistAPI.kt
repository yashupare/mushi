package org.spotube.js_plugin_example.plugin_apis.metadata

import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylistAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

class RealMetadataPlaylistAPI : MetadataPlaylistAPI {

    override suspend fun getPlaylist(id: String): MetadataPlaylist {
        return FakeMetadataStore.getPlaylist(id)
    }

    override suspend fun getPlaylistTracks(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataTrack> {
        return FakeMetadataStore.paginate(FakeMetadataStore.getPlaylistTracks(id), pagination)
    }

    override suspend fun savedPlaylists(
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataPlaylist> {
        return FakeMetadataStore.paginate(FakeMetadataStore.savedPlaylists(), pagination)
    }

    override suspend fun isSavedPlaylists(ids: List<String>): List<Boolean> {
        return FakeMetadataStore.isSavedPlaylists(ids)
    }

    override suspend fun savePlaylists(ids: List<String>) {
        FakeMetadataStore.savePlaylists(ids)
    }

    override suspend fun removeSavedPlaylists(ids: List<String>) {
        FakeMetadataStore.removeSavedPlaylists(ids)
    }

    override suspend fun createPlaylist(
        name: String,
        description: String?,
        isPublic: Boolean,
        isCollaborating: Boolean,
        imageBase64: String,
        trackIds: List<String>
    ): MetadataPlaylist {
        return FakeMetadataStore.createPlaylist(
            name = name,
            description = description,
            isPublic = isPublic,
            isCollaborating = isCollaborating,
            imageBase64 = imageBase64,
            trackIds = trackIds,
        )
    }

    override suspend fun updatePlaylist(
        id: String,
        name: String?,
        description: String?,
        isPublic: Boolean?,
        isCollaborating: Boolean?,
        imageBase64: String?,
        trackIds: List<String>?
    ): MetadataPlaylist {
        return FakeMetadataStore.updatePlaylist(
            id = id,
            name = name,
            description = description,
            isPublic = isPublic,
            isCollaborating = isCollaborating,
            imageBase64 = imageBase64,
            trackIds = trackIds,
        )
    }

    override suspend fun deletePlaylist(id: String) {
        FakeMetadataStore.deletePlaylist(id)
    }

    override suspend fun addTracksToPlaylist(
        playlistId: String,
        trackIds: List<String>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun removeTracksFromPlaylist(
        playlistId: String,
        trackIds: List<String>
    ) {
        TODO("Not yet implemented")
    }
}
