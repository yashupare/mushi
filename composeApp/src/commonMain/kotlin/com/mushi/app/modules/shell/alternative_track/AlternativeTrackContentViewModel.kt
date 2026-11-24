package com.mushi.app.modules.shell.alternative_track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioSource
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.server.AlternativeTracksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlternativeTrackUiState(
    val currentTrack: MetadataTrack? = null,
    val alternatives: List<AudioSource> = emptyList(),
    val activeSourceId: String? = null,
    val isLoading: Boolean = false)

class AlternativeTrackContentViewModel(
    private val audioPlayerQueue: AudioPlayerQueue,
    private val alternativeTracksRepository: AlternativeTracksRepository) : ViewModel() {
    private val alternativeVisibilityFlow = MutableStateFlow(false)
    private val alternativesFlow = MutableStateFlow<List<AudioSource>>(emptyList())
    private val activeSourceIdFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(false)

    val isAlternativeVisible: StateFlow<Boolean> = alternativeVisibilityFlow.asStateFlow()

    val alternativeTrackUiState: StateFlow<AlternativeTrackUiState> = combine(
        audioPlayerQueue.currentQueueEntryFlow,
        alternativesFlow,
        activeSourceIdFlow,
        isLoadingFlow) { currentEntry, alternatives, activeSourceId, isLoading ->
        val currentTrack = (currentEntry as? QueueEntry.StreamingTrack)?.track
        AlternativeTrackUiState(
            currentTrack = currentTrack,
            alternatives = alternatives,
            activeSourceId = activeSourceId,
            isLoading = isLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlternativeTrackUiState())

    fun toggleAlternativeVisibility() {
        alternativeVisibilityFlow.update { !it }
    }

    fun setAlternativeVisibility(isVisible: Boolean) {
        alternativeVisibilityFlow.value = isVisible
    }

    fun loadAlternatives() {
        val currentTrack = (audioPlayerQueue.currentQueueEntryFlow.value as? QueueEntry.StreamingTrack)?.track
        if (currentTrack == null) {
            alternativesFlow.value = emptyList()
            activeSourceIdFlow.value = null
            return
        }
        viewModelScope.launch {
            isLoadingFlow.value = true
            val sources = alternativeTracksRepository.resolveAlternatives(currentTrack)
            alternativesFlow.value = sources
            activeSourceIdFlow.value = alternativeTracksRepository.getActiveSourceId(currentTrack)
            isLoadingFlow.value = false
        }
    }

    fun selectAlternative(source: AudioSource) {
        val currentTrack =
            (audioPlayerQueue.currentQueueEntryFlow.value as? QueueEntry.StreamingTrack)?.track
                ?: return
        viewModelScope.launch {
            alternativeTracksRepository.selectAlternative(currentTrack, source)
            activeSourceIdFlow.value = source.id
            setAlternativeVisibility(false)
        }
    }
}
