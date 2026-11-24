package org.spotube.plugin_interfaces.plugin_apis.lyrics

import kotlinx.serialization.Serializable

@Serializable
data class LyricsLine(
    val time: Long, // Time in milliseconds
    val text: String
)

@Serializable
data class LyricsResponse(
    val syncedLyrics: List<LyricsLine>?,
    val plainLyrics: String?,
)