package com.mushi.app.modules.library.local_tracks.media

import com.mushi.app.core.di.injectLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import org.koin.core.component.KoinComponent

class LocalMediaLibraryCoordinator(
    private val discoveryService: LocalMediaDiscoveryService,
    private val cacheRepository: LocalMediaCacheRepository,
    private val foldersConfig: LocalMediaFoldersConfig) : KoinComponent {
    private val logger by injectLogger<LocalMediaLibraryCoordinator>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _cacheState = MutableStateFlow(LocalMediaCache())
    val cacheState: StateFlow<LocalMediaCache> = _cacheState.asStateFlow()

    private var observation: LocalMediaObservation? = null
    private var periodicJob: Job? = null
    private var configuredRoots: List<String> = emptyList()
    private val refreshMutex = Mutex()

    init {
        scope.launch {
            cacheRepository.cacheFlow.collect { cache ->
                _cacheState.value = cache
            }
        }

        scope.launch {
            discoveryService.ensureInitialized()
            foldersConfig.configuredRootsFlow.collect { roots ->
                configuredRoots = roots
                observation?.stop()
                observation = discoveryService.observeChanges(roots) {
                    scheduleRefresh(reason = "filesystem_change")
                }
                scheduleRefresh(reason = "roots_changed")
            }
        }

        periodicJob = scope.launch {
            while (true) {
                delay(PERIODIC_REFRESH_MS)
                refreshIfStale()
            }
        }
    }

    fun scheduleRefresh(reason: String) {
        scope.launch {
            runCatching { refreshNow(reason) }
                .onFailure { throwable ->
                    logger.w(throwable) { "Local media refresh failed. reason=$reason" }
                }
        }
    }

    suspend fun refreshIfStale() {
        val lastIndexedAt = _cacheState.value.indexedAtEpochMs
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastIndexedAt >= STALE_AFTER_MS) {
            refreshNow("stale")
        }
    }

    suspend fun refreshNow(reason: String) {
        refreshMutex.withLock {
            logger.i { "Refreshing local media cache. reason=$reason" }
            runCatching {
                discoveryService.discoverFolders(configuredRoots)
            }.onSuccess { folders ->
                val cache = LocalMediaCache(
                    folders = folders,
                    indexedAtEpochMs = Clock.System.now().toEpochMilliseconds())
                cacheRepository.save(cache)
                _cacheState.value = cache
            }.onFailure { throwable ->
                logger.w(throwable) { "Failed to discover local media. reason=$reason" }
            }
        }
    }

    companion object {
        private const val PERIODIC_REFRESH_MS = 15 * 60 * 1000L
        private const val STALE_AFTER_MS = 30 * 60 * 1000L
    }
}
