package org.spotube.plugin_interfaces.plugin_apis.metadata.album

import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MetadataAlbumType {
    Single,
    Album,
    Collection
}

@Serializable
sealed class MetadataAlbum {
    abstract val id: String
    abstract val title: String
    abstract val description: String?
    abstract val thumbnails: List<Thumbnail>
    abstract val albumType: MetadataAlbumType
    abstract val artists: List<MetadataArtist.Basic>
    abstract val externalUri: String?

    @Serializable
    @SerialName("base")
    data class Basic(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val thumbnails: List<Thumbnail>,
        override val albumType: MetadataAlbumType,
        override val artists: List<MetadataArtist.Basic>,
        override val externalUri: String?

    ) : MetadataAlbum()

    @Serializable
    @SerialName("detailed")
    data class Detailed(
        val releaseDate: String?,
        val genres: List<String>,
        val trackCount: Int,
        override val id: String,
        override val title: String,
        override val description: String?,
        override val thumbnails: List<Thumbnail>,
        override val albumType: MetadataAlbumType,
        override val artists: List<MetadataArtist.Basic>,
        override val externalUri: String?
    ) : MetadataAlbum()
}