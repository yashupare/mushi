package org.spotube.plugin_interfaces.plugin_apis.metadata.artist

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class MetadataArtist {
    abstract val id: String
    abstract val name: String
    abstract val thumbnails: List<Thumbnail>
    abstract val externalUri: String?

    @Serializable
    @SerialName("base")
    data class Basic(
        override val id: String,
        override val name: String,
        override val thumbnails: List<Thumbnail>,
        override val externalUri: String?
    ) : MetadataArtist()

    @Serializable
    @SerialName("detailed")
    data class Detailed(
        val genres: List<String>,
        val biography: String?,
        val followersCount: Int?,
        override val id: String,
        override val name: String,
        override val thumbnails: List<Thumbnail>,
        override val externalUri: String?
    ) : MetadataArtist()
}

@Serializable
data class MetadataArtistOverview(
    val artist: MetadataArtist.Detailed,
    val top10Tracks: List<MetadataTrack>,
    val albums: PaginationResult<MetadataAlbum.Detailed>,
    val relatedArtists: PaginationResult<MetadataArtist.Basic>,
    val featuredPlaylists: PaginationResult<MetadataPlaylist>
)