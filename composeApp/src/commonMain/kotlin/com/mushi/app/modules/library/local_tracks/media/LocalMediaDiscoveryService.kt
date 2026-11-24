package com.mushi.app.modules.library.local_tracks.media

typealias LocalMediaChangeCallback = () -> Unit

fun interface LocalMediaObservation {
    fun stop()
}

interface LocalMediaDiscoveryService {
    suspend fun discoverFolders(roots: List<String>): List<LocalMediaFolder>
    suspend fun ensureInitialized() {}
    fun observeChanges(roots: List<String>, onChanged: LocalMediaChangeCallback): LocalMediaObservation?
}
