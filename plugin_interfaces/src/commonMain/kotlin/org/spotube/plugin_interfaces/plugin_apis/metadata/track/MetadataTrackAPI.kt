package org.spotube.plugin_interfaces.plugin_apis.metadata.track

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult

const val MetadataTrackAPI_SERVICE_NAME = "MetadataTrackAPI"

interface MetadataTrackAPI : ZiplineService {
    suspend fun getTrack(id: String): MetadataTrack

    suspend fun savedTracks(
        pagination: PaginationStrategy? = null
    ): PaginationResult<MetadataTrack>

    suspend fun isSavedTracks(ids: List<String>): List<Boolean>

    suspend fun saveTracks(ids: List<String>)
    suspend fun removeSavedTracks(ids: List<String>)

    suspend fun recommendationsBasedOnTracks(
        seedTrackIds: List<String>,
        limit: Int = 20
    ): List<MetadataTrack>
}