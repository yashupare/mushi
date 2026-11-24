package com.mushi.app.modules.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.spotube.plugin_interfaces.plugin_apis.lyrics.LyricsLine
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.modules.plugin.PluginManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

@Serializable
enum class LyricType {
    STATIC,
    SYNCED
}

data class LyricsUiState(
    val mode: LyricType = LyricType.SYNCED,
    val syncedLyrics: List<LyricsLine>? = null,
    val plainLyrics: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentIndex: Int = -1,
    val trackTitle: String = "",
    val trackArtists: String = "",
    val coverUrl: String = "")

class LyricsViewModel(
    private val pluginManager: PluginManager,
    private val audioPlayer: AudioPlayerInterface,
    private val audioPlayerQueue: AudioPlayerQueue) : ViewModel() {

    private val _uiState = MutableStateFlow(LyricsUiState())

    // Only re-emits when the active line changes, not on every position tick.
    private val currentIndexFlow = combine(
        _uiState.map { it.syncedLyrics }.distinctUntilChanged(),
        audioPlayer.positionFlow
    ) { lyrics, position -> activeLineIndex(lyrics, position.inWholeMilliseconds) }
        .distinctUntilChanged()

    val uiState: StateFlow<LyricsUiState> = combine(_uiState, currentIndexFlow) { state, index ->
        state.copy(currentIndex = index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LyricsUiState())

    /**
     * How far playback is through the active line, 0 to 1, for the karaoke fill. Separate from
     * [uiState] because it ticks with every position update; the screen reads it only while
     * drawing, so it repaints the fill without recomposing the list.
     */
    val currentLineProgress: StateFlow<Float> = combine(
        _uiState.map { it.syncedLyrics }.distinctUntilChanged(),
        audioPlayer.positionFlow
    ) { lyrics, position -> lineProgress(lyrics, position.inWholeMilliseconds) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private var currentTrackId: String? = null

    fun setMode(mode: LyricType) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun loadLyrics(forceReload: Boolean = false) {
        viewModelScope.launch {
            val entry = audioPlayerQueue.currentQueueEntryFlow.value ?: return@launch
            val track = when (entry) {
                is QueueEntry.StreamingTrack -> entry.track
                is QueueEntry.LocalTrack -> return@launch
            }

            if (!forceReload && currentTrackId == track.id && _uiState.value.syncedLyrics != null) {
                return@launch
            }

            currentTrackId = track.id
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                pluginManager.selectedLyricsPlugin.value?.let { pluginService ->
                    pluginService.use {
                       val lyrics = lyricsAPI.getLyrics(track)

                        _uiState.update {
                            it.copy(
                                syncedLyrics = lyrics?.syncedLyrics,
                                plainLyrics = lyrics?.plainLyrics,
                                isLoading = false)
                        }
                    }
                } ?: run {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    init {
        viewModelScope.launch {
            audioPlayerQueue.currentQueueEntryFlow.collect { entry ->
                val trackId = when (entry) {
                    is QueueEntry.StreamingTrack -> entry.track.id
                    else -> null
                }
                if (entry is QueueEntry.StreamingTrack) {
                    val track = entry.track
                    _uiState.update {
                        it.copy(
                            trackTitle = track.title,
                            trackArtists = track.artists.joinToString(", ") { artist -> artist.name },
                            coverUrl = (track.album?.thumbnails ?: track.thumbnails)
                                ?.firstOrNull()?.url.orEmpty()
                        )
                    }
                }
                if (trackId != null && trackId != currentTrackId) {
                    loadLyrics()
                }
            }
        }
    }

    fun seekToLine(line: LyricsLine) {
        viewModelScope.launch {
            audioPlayer.seekTo(line.time.milliseconds)
        }
    }

    /**
     * Progress through the active line. A line counts as sung from its own time until the next
     * line starts, capped at [MAX_SUNG_LINE_MILLIS] so a line before a long instrumental break
     * still fills at a singing pace.
     */
    private fun lineProgress(lyrics: List<LyricsLine>?, positionMillis: Long): Float {
        if (lyrics.isNullOrEmpty()) return 0f
        val index = activeLineIndex(lyrics, positionMillis)
        val start = lyrics[index].time
        if (positionMillis < start) return 0f
        val next = lyrics.getOrNull(index + 1)?.time ?: (start + MAX_SUNG_LINE_MILLIS)
        val duration = (next - start).coerceIn(1L, MAX_SUNG_LINE_MILLIS)
        return ((positionMillis - start).toFloat() / duration).coerceIn(0f, 1f)
    }

    private companion object {
        const val MAX_SUNG_LINE_MILLIS = 7_000L
    }

    private fun activeLineIndex(lyrics: List<LyricsLine>?, positionMillis: Long): Int {
        if (lyrics.isNullOrEmpty()) return -1
        val index = lyrics.indexOfLast { it.time <= positionMillis }
        return index.coerceAtLeast(0)
    }
}
