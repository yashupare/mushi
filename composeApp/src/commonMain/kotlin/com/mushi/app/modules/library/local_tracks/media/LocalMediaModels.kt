package com.mushi.app.modules.library.local_tracks.media

import kotlinx.serialization.Serializable

@Serializable
data class LocalMediaTrack(
    val path: String,
    val name: String,
    val artists: List<String>,
    val durationMs: Long,
    val album: String? = null,
    val coverBytes: ByteArray? = null)

@Serializable
data class LocalMediaFolder(
    val path: String,
    val name: String,
    val tracks: List<LocalMediaTrack>) {
    val trackCount: Int
        get() = tracks.size
}

@Serializable
data class LocalMediaCache(
    val folders: List<LocalMediaFolder> = emptyList(),
    val indexedAtEpochMs: Long = 0)
