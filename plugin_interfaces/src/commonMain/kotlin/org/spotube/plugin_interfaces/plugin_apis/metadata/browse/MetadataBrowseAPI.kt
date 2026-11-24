package org.spotube.plugin_interfaces.plugin_apis.metadata.browse

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult

const val MetadataBrowseAPI_SERVICE_NAME = "MetadataBrowseAPI"

interface MetadataBrowseAPI: ZiplineService {
    suspend fun featured(): List<MetadataBrowseItem>
    suspend fun genres(): List<MetadataBrowseGenre>
    suspend fun list(genreId: String, pagination: PaginationStrategy? = null): PaginationResult<MetadataBrowseSection>
    suspend fun sublist(genreId: String, sectionId: String, pagination: PaginationStrategy? = null): PaginationResult<MetadataBrowseItem>
}