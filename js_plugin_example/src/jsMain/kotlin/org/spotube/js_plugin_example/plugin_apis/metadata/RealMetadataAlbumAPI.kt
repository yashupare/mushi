package org.spotube.js_plugin_example.plugin_apis.metadata

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

class RealMetadataAlbumAPI : MetadataAlbumAPI {

    override suspend fun getAlbum(id: String): MetadataAlbum.Detailed {
        return FakeMetadataStore.getAlbum(id)
    }

    override suspend fun getTrackAlbum(track: MetadataTrack): MetadataAlbum.Detailed {
        TODO("Not yet implemented")
    }

    override suspend fun getAlbumTracks(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataTrack> {
        return FakeMetadataStore.paginate(FakeMetadataStore.getAlbumTracks(id), pagination)
    }

    override suspend fun savedAlbums(
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataAlbum.Detailed> {
        return FakeMetadataStore.paginate(FakeMetadataStore.savedAlbums(), pagination)
    }

    override suspend fun isSavedAlbums(ids: List<String>): List<Boolean> {
        return FakeMetadataStore.isSavedAlbums(ids)
    }

    override suspend fun saveAlbums(ids: List<String>) {
        FakeMetadataStore.saveAlbums(ids)
    }

    override suspend fun removeSavedAlbums(ids: List<String>) {
        FakeMetadataStore.removeSavedAlbums(ids)
    }
}
