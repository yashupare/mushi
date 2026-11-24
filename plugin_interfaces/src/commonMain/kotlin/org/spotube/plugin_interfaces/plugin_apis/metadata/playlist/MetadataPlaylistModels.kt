package org.spotube.plugin_interfaces.plugin_apis.metadata.playlist

import org.spotube.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import org.spotube.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import kotlinx.serialization.Serializable

@Serializable
data class MetadataPlaylist (
    val id: String,
    val title: String,
    val description: String?,
    val thumbnails: List<Thumbnail>,
    val trackCount: Int,
    val externalUri: String?,
    val owner: MetadataUser?
)