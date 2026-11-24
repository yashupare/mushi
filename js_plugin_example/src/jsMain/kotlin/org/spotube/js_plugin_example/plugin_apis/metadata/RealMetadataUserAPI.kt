package org.spotube.js_plugin_example.plugin_apis.metadata

import org.spotube.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import org.spotube.plugin_interfaces.plugin_apis.metadata.user.MetadataUserAPI

class RealMetadataUserAPI : MetadataUserAPI {
    override suspend fun getUser(id: String): MetadataUser? {
        return FakeMetadataStore.getUser(id)
    }
}