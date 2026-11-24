package org.spotube.plugin_interfaces.plugin_apis.scrobble

import kotlinx.serialization.Serializable

@Serializable
data class ScrobbleTrack(
    val timestamp: Long,
    val trackId: String,
    val artistId: String,
    val albumId: String?,
    val trackName: String,
    val artistName: String,
    val albumName: String?,
    val streamingProvider: String, // e.g. "greedy_green_corp", "apple_music" etc
    val durationMs: Long? = null,
)