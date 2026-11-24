package org.spotube.plugin_interfaces.plugin_apis.metadata.search

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import org.spotube.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MetadataSupportedSearchType {
    TRACK,
    ARTIST,
    ALBUM,
    PLAYLIST,
    USER,
    ALL
}

@Serializable
sealed class MetadataSearchResult{
    @Serializable
    @SerialName("track")
    data class Track(val data: MetadataTrack): MetadataSearchResult()

    @Serializable
    @SerialName("artist")
    data class Artist(val data: MetadataArtist.Basic): MetadataSearchResult()

    @Serializable
    @SerialName("album")
    data class Album(val data: MetadataAlbum.Basic): MetadataSearchResult()

    @Serializable
    @SerialName("playlist")
    data class Playlist(val data: MetadataPlaylist): MetadataSearchResult()

    @Serializable
    @SerialName("user")
    data class User(val data: MetadataUser): MetadataSearchResult()
}