package org.spotube.plugin_interfaces.plugin_apis.metadata.user

import org.spotube.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import kotlinx.serialization.Serializable

@Serializable
data class MetadataUser(
    val id: String,
    val username: String,
    val displayName: String?,
    val thumbnails: List<Thumbnail>,
    val externalUri: String?
)