package org.spotube.plugin_interfaces.host_apis

import app.cash.zipline.ZiplineService

const val PersistedStorageAPI_SERVICE_NAME = "PersistedStorageAPI"

interface PersistedStorageAPI: ZiplineService {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)

    suspend fun getKeys(): List<String>
}