package org.spotube.js_plugin_example.plugin_apis.metadata

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistOverview
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

class RealMetadataArtistAPI : MetadataArtistAPI {

    override suspend fun getArtist(id: String): MetadataArtist.Detailed {
        return FakeMetadataStore.getArtist(id)
    }

    override suspend fun artistOverview(id: String): MetadataArtistOverview {
        TODO("Not yet implemented")
    }

    override suspend fun getArtistTop10Tracks(id: String): List<MetadataTrack> {
        return FakeMetadataStore.getArtistTopTracks(id)
    }

    override suspend fun relatedArtists(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataArtist.Basic> {
        TODO("Not yet implemented")
    }

    override suspend fun featuredPlaylists(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataPlaylist> {
        TODO("Not yet implemented")
    }

    override suspend fun getArtistAlbums(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataAlbum.Detailed> {
        return FakeMetadataStore.paginate(FakeMetadataStore.getArtistAlbums(id), pagination)
    }

    override suspend fun savedArtists(
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataArtist.Detailed> {
        return FakeMetadataStore.paginate(FakeMetadataStore.savedArtists(), pagination)
    }

    override suspend fun isSavedArtists(ids: List<String>): List<Boolean> {
        return FakeMetadataStore.isSavedArtists(ids)
    }

    override suspend fun saveArtists(ids: List<String>) {
        FakeMetadataStore.saveArtists(ids)
    }

    override suspend fun removeSavedArtists(ids: List<String>) {
        FakeMetadataStore.removeSavedArtists(ids)
    }
}
