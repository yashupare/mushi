package com.mushi.app.modules.library.local_tracks.media

import com.mushi.app.core.paths.Paths
import com.mushi.app.modules.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalMediaFoldersConfig(
    private val settingsRepository: SettingsRepository,
    private val paths: Paths) {
    val configuredRootsFlow: Flow<List<String>> = settingsRepository.userSettings.map { settings ->
        val cacheRoot = if (settings.enableMusicCaching) {
            listOf(settings.cacheFolder ?: paths.getMusicCacheDirPath())
        } else {
            emptyList()
        }
        (listOfNotNull(settings.overloadedDownloadFolder) + settings.localMediaFolders + cacheRoot)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
