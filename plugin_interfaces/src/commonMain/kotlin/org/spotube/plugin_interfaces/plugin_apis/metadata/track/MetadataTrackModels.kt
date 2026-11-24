package org.spotube.plugin_interfaces.plugin_apis.metadata.track

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import kotlinx.serialization.Serializable

@Serializable
data class MetadataTrack(
    val id: String,
    val title: String,
    val durationMs: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val artists: List<MetadataArtist.Basic>,
    val album: MetadataAlbum.Detailed?,
    val thumbnails: List<Thumbnail>?,
    val explicit: Boolean?,
    val popularity: Int?,
    val isrcCode: String?,
    val externalUri: String?
)