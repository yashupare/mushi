package com.mushi.app.modules.update

import co.touchlab.kermit.Logger
import com.mushi.app.AppBuildInfo
import com.mushi.app.core.network.HttpClientFactory
import com.mushi.app.modules.settings.SettingsRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class AvailableAppUpdate(
    val tag: String,
    val releaseNotesMarkdown: String,
    /** Null when the release has nothing this app can install itself. */
    val apk: UpdatePackage? = null)

/** How far an update the listener asked for has got. */
sealed interface UpdateProgress {
    data object Idle : UpdateProgress
    data object NeedsPermission : UpdateProgress
    data class Downloading(val fraction: Float) : UpdateProgress
    /** Downloaded, but installing closes the app and music is playing. */
    data object Ready : UpdateProgress
    data object Installing : UpdateProgress
    data object Failed : UpdateProgress
}

enum class UpdateCheckResult { Found, UpToDate, Failed }

/**
 * Looks for a newer release on GitHub, on startup and on request, and downloads and installs it
 * when asked. This is an app-scoped singleton (not a ViewModel) since an update outlives any
 * screen.
 */
class AppUpdateService(
    private val settingsRepository: SettingsRepository,
    httpClientFactory: HttpClientFactory,
    private val installer: AppInstaller,
    private val isMusicPlaying: () -> Boolean) : KoinComponent {
    private val logger = Logger.withTag("AppUpdateService")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val httpClient = httpClientFactory.json

    private val _availableUpdate = MutableStateFlow<AvailableAppUpdate?>(null)
    val availableUpdate: StateFlow<AvailableAppUpdate?> = _availableUpdate.asStateFlow()

    private val _progress = MutableStateFlow<UpdateProgress>(UpdateProgress.Idle)
    val progress: StateFlow<UpdateProgress> = _progress.asStateFlow()

    private var updating: Job? = null
    private var downloaded: String? = null

    init {
        scope.launch {
            // Read directly from DataStore so the check honors persisted preferences,
            // rather than the repository StateFlow's default value during startup.
            if (settingsRepository.getCurrentSettings().autoCheckForUpdates) check(skipIgnored = true)
        }
        scope.launch {
            installer.failures.collect { _progress.value = UpdateProgress.Failed }
        }
    }

    /** A check the listener asked for, which also offers a release they chose to ignore. */
    suspend fun checkNow(): UpdateCheckResult = check(skipIgnored = false)

    private suspend fun check(skipIgnored: Boolean): UpdateCheckResult {
        // No public repository configured for this build (see composeApp/build.gradle.kts).
        if (AppBuildInfo.RELEASES_API_URL.isBlank()) return UpdateCheckResult.UpToDate
        return try {
            val currentVersion = AppReleaseSelector.parseTag(AppBuildInfo.VERSION)
                ?: error("Invalid app version: ${AppBuildInfo.VERSION}")

            val releases = httpClient.get(AppBuildInfo.RELEASES_API_URL) {
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header(HttpHeaders.UserAgent, "Mushi/${AppBuildInfo.VERSION}")
            }.body<List<GitHubRelease>>()

            val ignored =
                if (skipIgnored) settingsRepository.getCurrentSettings().ignoredAppUpdateVersion else null
            val release = AppReleaseSelector.select(currentVersion, releases, ignored)
            if (release == null) {
                UpdateCheckResult.UpToDate
            } else {
                _progress.value = UpdateProgress.Idle
                _availableUpdate.value = AvailableAppUpdate(
                    tag = release.tagName,
                    releaseNotesMarkdown = release.body.orEmpty(),
                    apk = release.apk())
                UpdateCheckResult.Found
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w(e) { "Failed to check for app updates" }
            UpdateCheckResult.Failed
        }
    }

    /** True when the update can be installed from here; otherwise the caller shows the website. */
    fun canUpdateInApp(): Boolean = installer.supported && _availableUpdate.value?.apk != null

    fun startUpdate() {
        val apk = _availableUpdate.value?.apk ?: return
        if (updating?.isActive == true) return
        if (!installer.canInstall()) {
            _progress.value = UpdateProgress.NeedsPermission
            installer.openInstallPermission()
            return
        }
        _progress.value = UpdateProgress.Downloading(0f)
        updating = scope.launch {
            attempt {
                val path = installer.download(apk) { _progress.value = UpdateProgress.Downloading(it) }
                downloaded = path
                if (isMusicPlaying()) _progress.value = UpdateProgress.Ready else install(path)
            }
        }
    }

    fun installNow() {
        val path = downloaded ?: return
        if (updating?.isActive == true) return
        updating = scope.launch { attempt { install(path) } }
    }

    private suspend fun install(path: String) {
        _progress.value = UpdateProgress.Installing
        installer.install(path)
    }

    private suspend fun attempt(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w(e) { "Failed to update the app" }
            _progress.value = UpdateProgress.Failed
        }
    }

    fun ignoreUpdate() {
        val update = _availableUpdate.value ?: return
        dismissUpdate()
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
        updating?.cancel()
        updating = null
        downloaded = null
        _progress.value = UpdateProgress.Idle
        _availableUpdate.value = null
    }
}
