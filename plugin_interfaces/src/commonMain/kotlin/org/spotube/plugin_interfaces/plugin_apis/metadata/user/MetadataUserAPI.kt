package org.spotube.plugin_interfaces.plugin_apis.metadata.user

import app.cash.zipline.ZiplineService
import kotlinx.coroutines.flow.StateFlow

const val MetadataUserAPI_SERVICE_NAME = "MetadataUserAPI"

interface MetadataUserAPI: ZiplineService {
    suspend fun getUser(id: String): MetadataUser?
}