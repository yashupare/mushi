package com.mushi.app.modules.library.local_tracks.media

class NoopLocalMediaDiscoveryService : LocalMediaDiscoveryService {
    override suspend fun discoverFolders(roots: List<String>): List<LocalMediaFolder> {
        return emptyList()
    }

    override fun observeChanges(
        roots: List<String>,
        onChanged: LocalMediaChangeCallback): LocalMediaObservation? {
        return null
    }
}
