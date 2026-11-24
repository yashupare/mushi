package com.mushi.app.modules.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioSource
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioStream
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.server.CacheEntry
import com.mushi.app.core.server.CacheManager
import com.mushi.app.core.server.StreamingUrlRepository
import com.mushi.app.core.server.TrackSourceRepository
import com.mushi.app.core.server.normalizeManifestUrl
import com.mushi.app.core.server.selectPreferredAudioStream
import com.mushi.app.modules.plugin.AudioPluginSource
import com.mushi.app.modules.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.StringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.player_track_details_load_failed
import com.mushi.app.generated.resources.player_track_details_no_cached_source
import com.mushi.app.generated.resources.player_track_details_streams_not_cached

data class PlayerTrackDetails(
    val track: MetadataTrack,
    val matchedSource: AudioSource.Basic? = null,
    val streams: List<AudioStream> = emptyList(),
    /** Stream picked by the current streaming format/quality preference. */
    val preferredStream: AudioStream? = null,
    /** URL currently used for playback, read from the playback URL cache. */
    val activeStreamUrl: String? = null,
    /** Whether the active URL matches what the current preferences select. */
    val activeStreamMatchesPreference: Boolean? = null,
    val localCacheEntry: CacheEntry? = null,
    val isLoading: Boolean = false,
    /** Raw error message (e.g. from an exception); takes precedence over [errorRes]. */
    val error: String? = null,
    /** Localized error to show when [error] is null. */
    val errorRes: StringResource? = null)

data class PlayerOptionsUiState(
    val isMoreOptionsMenuOpen: Boolean = false,
    val isMoreOptionsSheetOpen: Boolean = false,
    val isSleepTimerDialogOpen: Boolean = false,
    val sleepTimerMinutes: String = "30",
    val sleepTimerRemainingMs: Long? = null,
    val trackDetails: PlayerTrackDetails? = null)

class PlayerOptionsViewModel(
    private val audioPlayer: AudioPlayerInterface,
    private val audioPlayerQueue: AudioPlayerQueue,
    private val matchedTracksRepository: TrackSourceRepository,
    private val streamingUrlRepository: StreamingUrlRepository,
    private val cacheManager: CacheManager,
    private val settingsRepository: SettingsRepository,
    private val pluginManager: AudioPluginSource) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerOptionsUiState())
    val uiState: StateFlow<PlayerOptionsUiState> = _uiState.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var trackDetailsJob: Job? = null

    fun showMoreOptionsMenu() {
        _uiState.update {
            it.copy(isMoreOptionsMenuOpen = true, isMoreOptionsSheetOpen = false)
        }
    }

    fun dismissMoreOptionsMenu() {
        _uiState.update { it.copy(isMoreOptionsMenuOpen = false) }
    }

    fun showMoreOptionsSheet() {
        _uiState.update {
            it.copy(isMoreOptionsSheetOpen = true, isMoreOptionsMenuOpen = false)
        }
    }

    fun dismissMoreOptionsSheet() {
        _uiState.update { it.copy(isMoreOptionsSheetOpen = false) }
    }

    fun showSleepTimerDialog() {
        _uiState.update { it.copy(isSleepTimerDialogOpen = true) }
    }

    fun dismissSleepTimerDialog() {
        _uiState.update { it.copy(isSleepTimerDialogOpen = false) }
    }

    fun updateSleepTimerMinutes(minutes: String) {
        if (minutes.all(Char::isDigit)) {
            _uiState.update { it.copy(sleepTimerMinutes = minutes) }
        }
    }

    fun startSleepTimer() {
        val minutes = _uiState.value.sleepTimerMinutes.toLongOrNull()
            ?.takeIf { it in 1..600 } ?: return
        val durationMs = minutes * MILLIS_PER_MINUTE

        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            var remainingMs = durationMs
            while (remainingMs > 0L) {
                _uiState.update { it.copy(sleepTimerRemainingMs = remainingMs) }
                val tickMs = minOf(remainingMs, MILLIS_PER_SECOND)
                delay(tickMs.milliseconds)
                remainingMs -= tickMs
            }
            runCatching { audioPlayer.pause() }
            _uiState.update { it.copy(sleepTimerRemainingMs = null, isSleepTimerDialogOpen = false) }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.update { it.copy(sleepTimerRemainingMs = null) }
    }

    fun showTrackDetails() {
        val track = (audioPlayerQueue.currentQueueEntryFlow.value as? QueueEntry.StreamingTrack)
            ?.track ?: return
        trackDetailsJob?.cancel()
        _uiState.update {
            it.copy(
                trackDetails = PlayerTrackDetails(track = track, isLoading = true))
        }
        trackDetailsJob = viewModelScope.launch {
            runCatching {
                val settings = settingsRepository.userSettings.value
                withContext(Dispatchers.IO) {
                    val pluginId = pluginManager.selectedAudioPlugin.value?.pluginId
                    val matchedSource = pluginId?.let { matchedTracksRepository.getTrackSource(track, it) }
                    val cachedStream = streamingUrlRepository.getCachedStreamUrlEntry(track.id)
                    val localCacheEntry = if (settings.enableMusicCaching) {
                        cacheManager.findCachedEntry(track.id)?.second
                    } else {
                        null
                    }
                    val cachedStreamSource = cachedStream?.source?.takeIf {
                        matchedSource == null || it.id == matchedSource.id
                    }

                    val source = matchedSource ?: cachedStreamSource?.toBasic()
                    val streams = cachedStreamSource?.streams.orEmpty()
                    val preferredStream = selectPreferredAudioStream(
                        streams = streams,
                        preferredFormat = settings.streamingMusicFormat,
                        preferredQuality = settings.streamingMusicQuality)
                    val activeStreamUrl = if (cachedStreamSource != null) cachedStream.url else null
                    val activeStreamMatchesPreference = when {
                        activeStreamUrl == null || preferredStream == null -> null
                        else -> activeStreamUrl == normalizeManifestUrl(
                            url = preferredStream.url,
                            protocol = preferredStream.protocol)
                    }

                    PlayerTrackDetails(
                        track = track,
                        matchedSource = source,
                        streams = streams,
                        preferredStream = preferredStream,
                        activeStreamUrl = activeStreamUrl,
                        activeStreamMatchesPreference = activeStreamMatchesPreference,
                        localCacheEntry = localCacheEntry,
                        errorRes = when {
                            source == null && localCacheEntry == null ->
                                Res.string.player_track_details_no_cached_source
                            streams.isEmpty() && localCacheEntry == null ->
                                Res.string.player_track_details_streams_not_cached
                            else -> null
                        })
                }
            }.onSuccess { details ->
                _uiState.update { current ->
                    if (current.trackDetails?.track?.id == track.id) {
                        current.copy(trackDetails = details)
                    } else current
                }
            }.onFailure { error ->
                if (error is CancellationException) throw error
                _uiState.update { current ->
                    if (current.trackDetails?.track?.id == track.id) {
                        current.copy(
                            trackDetails = PlayerTrackDetails(
                                track = track,
                                isLoading = false,
                                error = error.message,
                                errorRes = Res.string.player_track_details_load_failed))
                    } else current
                }
            }
        }
    }

    fun dismissTrackDetails() {
        trackDetailsJob?.cancel()
        trackDetailsJob = null
        _uiState.update { it.copy(trackDetails = null) }
    }

    override fun onCleared() {
        sleepTimerJob?.cancel()
        trackDetailsJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1_000L
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
