package com.mushi.app.modules.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.HistoryItem
import com.mushi.app.core.history.ListeningHistoryRepository
import com.mushi.app.core.history.durationMs
import com.mushi.app.core.history.historyKey
import com.mushi.app.core.history.resumeAtMs
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/** An unfinished song on Home. */
data class ContinueCard(
    val key: String,
    val entry: QueueEntry,
    val title: String,
    val timeLeft: String,
    val progress: Float,
    val positionMs: Long)

private const val MAX_CONTINUE_CARDS = 5

/** The most recent unfinished songs, leaving out [activeKey], the one already under way. */
internal fun List<HistoryItem>.toContinueCards(activeKey: String?): List<ContinueCard> =
    sortedByDescending { it.lastPlayedAtMs }
        .filter { it.key != activeKey }
        .mapNotNull { item ->
            val positionMs = item.resumeAtMs() ?: return@mapNotNull null
            val durationMs = item.entry.durationMs()
            ContinueCard(
                key = item.key,
                entry = item.entry,
                title = when (val song = item.entry) {
                    is QueueEntry.StreamingTrack -> song.track.title
                    is QueueEntry.LocalTrack -> song.name
                },
                timeLeft = formatMinutes(durationMs - positionMs),
                progress = positionMs.toFloat() / durationMs,
                positionMs = positionMs)
        }
        .take(MAX_CONTINUE_CARDS)

private fun formatMinutes(ms: Long): String {
    val seconds = ms / 1000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

/** Plays [card]'s song from where it stopped, keeping the queue when the song is still in it. */
internal suspend fun resumeFrom(
    audioPlayerQueue: AudioPlayerQueue, audioPlayer: AudioPlayerInterface, card: ContinueCard) {
    val index = audioPlayerQueue.getQueue().indexOfFirst { it.historyKey() == card.key }
    if (index >= 0) audioPlayerQueue.jumpTo(index) else audioPlayerQueue.load(listOf(card.entry))
    audioPlayer.seekTo(card.positionMs.milliseconds)
}

class ContinueViewModel(
    repository: ListeningHistoryRepository,
    private val audioPlayer: AudioPlayerInterface,
    private val audioPlayerQueue: AudioPlayerQueue,
    jamRoomService: JamRoomService) : ViewModel() {

    // The queue's current song is only "under way" once it has moved: after a restart it sits
    // at the start, and Continue is then the way back to where it stopped.
    private val activeKey = combine(
        audioPlayerQueue.currentQueueEntryFlow,
        combine(audioPlayer.playerStateFlow, audioPlayer.positionFlow) { state, position ->
            state == PlayerState.PLAYING || position.inWholeMilliseconds > 1_000
        }.distinctUntilChanged()) { entry, underWay ->
        entry?.takeIf { underWay }?.historyKey()
    }.distinctUntilChanged()

    val uiState: StateFlow<List<ContinueCard>> = combine(
        repository.history,
        activeKey,
        // A jam guest's queue belongs to the host
        jamRoomService.role.map { it == JamRole.Guest }) { history, active, isGuest ->
        if (isGuest) emptyList() else history.toContinueCards(active)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun resume(card: ContinueCard) {
        viewModelScope.launch { resumeFrom(audioPlayerQueue, audioPlayer, card) }
    }
}
