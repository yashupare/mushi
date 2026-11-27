package com.mushi.app.modules.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.HistoryItem
import com.mushi.app.core.history.ListeningHistoryRepository
import com.mushi.app.core.remote.RemotePlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

enum class HistoryDayLabel { Today, Yesterday, Date }

data class HistoryRow(
    val key: String,
    val entry: QueueEntry,
    val title: String,
    val artists: String,
    val playCount: Int)

data class HistoryDay(val label: HistoryDayLabel, val date: String, val rows: List<HistoryRow>)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val days: List<HistoryDay> = emptyList(),
    val confirmingClear: Boolean = false)

class HistoryViewModel(
    private val repository: ListeningHistoryRepository,
    private val audioPlayerQueue: AudioPlayerQueue,
    private val remotePlaybackController: RemotePlaybackController) : ViewModel() {

    private val confirmingClear = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(repository.history, confirmingClear) { items, confirming ->
        val zone = TimeZone.currentSystemDefault()
        HistoryUiState(
            isLoading = false,
            days = items.toHistoryDays(Clock.System.now().toLocalDateTime(zone).date, zone),
            confirmingClear = confirming)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun play(row: HistoryRow) {
        when (val entry = row.entry) {
            is QueueEntry.StreamingTrack -> remotePlaybackController.requestTrackPlay(entry.track)
            is QueueEntry.LocalTrack -> viewModelScope.launch { audioPlayerQueue.load(listOf(entry)) }
        }
    }

    fun askToClear() {
        confirmingClear.value = true
    }

    fun cancelClear() {
        confirmingClear.value = false
    }

    fun clear() {
        confirmingClear.value = false
        viewModelScope.launch { repository.clear() }
    }
}

/** Groups the history (newest first) by the day each song was last played. */
internal fun List<HistoryItem>.toHistoryDays(today: LocalDate, zone: TimeZone): List<HistoryDay> {
    val yesterday = today.minus(DatePeriod(days = 1))
    return sortedByDescending { it.lastPlayedAtMs }
        .groupBy { Instant.fromEpochMilliseconds(it.lastPlayedAtMs).toLocalDateTime(zone).date }
        .map { (date, items) ->
            HistoryDay(
                label = when (date) {
                    today -> HistoryDayLabel.Today
                    yesterday -> HistoryDayLabel.Yesterday
                    else -> HistoryDayLabel.Date
                },
                date = date.toString(),
                rows = items.map { it.toRow() })
        }
}

private fun HistoryItem.toRow(): HistoryRow = when (val song = entry) {
    is QueueEntry.StreamingTrack -> HistoryRow(
        key = key,
        entry = song,
        title = song.track.title,
        artists = song.track.artists.joinToString(", ") { it.name },
        playCount = playCount)

    is QueueEntry.LocalTrack -> HistoryRow(
        key = key,
        entry = song,
        title = song.name,
        artists = song.artists.joinToString(", "),
        playCount = playCount)
}
