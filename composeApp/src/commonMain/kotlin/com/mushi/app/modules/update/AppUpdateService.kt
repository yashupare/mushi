package com.mushi.app.modules.update

import com.mushi.app.core.network.HttpClientFactory
import co.touchlab.kermit.Logger
import com.mushi.app.AppBuildInfo
import com.mushi.app.modules.settings.SettingsRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class AvailableAppUpdate(
    val tag: String,
    val releaseNotesMarkdown: String)

/**
 * Checks GitHub for a newer app release once on startup and exposes it for the UI to
 * surface via [com.mushi.app.modules.update.AppUpdateDialog]. This is an
 * app-scoped singleton (not a ViewModel) since the check runs once for the whole
 * process, independent of any screen's lifecycle.
 */
class AppUpdateService(
    private val settingsRepository: SettingsRepository,
    httpClientFactory: HttpClientFactory) : KoinComponent {
    private val logger = Logger.withTag("AppUpdateService")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val httpClient = httpClientFactory.json

    private val _availableUpdate = MutableStateFlow<AvailableAppUpdate?>(null)
    val availableUpdate: StateFlow<AvailableAppUpdate?> = _availableUpdate.asStateFlow()

    init {
        scope.launch {
            checkForUpdate()
        }
    }

    private suspend fun checkForUpdate() {
        // No public repository configured for this build (see composeApp/build.gradle.kts).
        if (AppBuildInfo.RELEASES_API_URL.isBlank()) return
        try {
            // Read directly from DataStore so the check honors persisted preferences,
            // rather than the repository StateFlow's default value during startup.
            val settings = settingsRepository.getCurrentSettings()
            if (!settings.autoCheckForUpdates) return

            val currentVersion = AppReleaseSelector.parseTag(AppBuildInfo.VERSION)
                ?: return logger.w { "Ignoring update check for invalid app version: ${AppBuildInfo.VERSION}" }

            val releases = httpClient.get(AppBuildInfo.RELEASES_API_URL) {
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header(HttpHeaders.UserAgent, "Mushi/${AppBuildInfo.VERSION}")
            }.body<List<GitHubRelease>>()

            val release = AppReleaseSelector.select(currentVersion, releases, settings.ignoredAppUpdateVersion)
            if (release != null) {
                _availableUpdate.value = AvailableAppUpdate(
                    tag = release.tagName,
                    releaseNotesMarkdown = release.body.orEmpty())
            }
        } catch (e: Exception) {
            logger.w(e) { "Failed to check for app updates" }
        }
    }

    fun ignoreUpdate() {
        val update = _availableUpdate.value ?: return
        _availableUpdate.value = null
        scope.launch {
            runCatching {
                val settings = settingsRepository.getCurrentSettings()
                settingsRepository.updateSettings(settings.copy(ignoredAppUpdateVersion = update.tag))
            }.onFailure { e ->
                logger.e(e) { "Failed to persist ignored app version" }
            }
        }
    }

    fun dismissUpdate() {
        _availableUpdate.value = null
    }
}

