package org.spotube.js_plugin_example.plugin_apis.metadata

import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrackAPI

class RealMetadataTrackAPI : MetadataTrackAPI {

    override suspend fun getTrack(id: String): MetadataTrack {
        return FakeMetadataStore.getTrack(id)
    }

    override suspend fun savedTracks(
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataTrack> {
        return FakeMetadataStore.paginate(FakeMetadataStore.savedTracks(), pagination)
    }

    override suspend fun isSavedTracks(ids: List<String>): List<Boolean> {
        return FakeMetadataStore.isSavedTracks(ids)
    }

    override suspend fun saveTracks(ids: List<String>) {
        FakeMetadataStore.saveTracks(ids)
    }

    override suspend fun removeSavedTracks(ids: List<String>) {
        FakeMetadataStore.removeSavedTracks(ids)
    }

    override suspend fun recommendationsBasedOnTracks(
        seedTrackIds: List<String>,
        limit: Int
    ): List<MetadataTrack> {
        return FakeMetadataStore.recommendationsBasedOnTracks(seedTrackIds, limit)
    }
}
