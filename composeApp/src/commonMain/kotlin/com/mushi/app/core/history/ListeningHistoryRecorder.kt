package com.mushi.app.core.history

import co.touchlab.kermit.Logger
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Watches local playback and records a song once it has actually been listened to, so skipped
 * songs stay out. Only time spent playing counts: pauses and forward seeks add nothing.
 * Each play is recorded once; replaying or repeating the song counts again.
 */
class ListeningHistoryRecorder(
    audioPlayer: AudioPlayerInterface,
    audioPlayerQueue: AudioPlayerQueue,
    private val record: suspend (QueueEntry, Long) -> Unit,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }) {

    private val logger = Logger.withTag("ListeningHistory")

    private var activeKey: String? = null
    private var lastPositionMs = 0L
    private var listenedMs = 0L
    private var recorded = false

    init {
        scope.launch {
            combine(
                audioPlayerQueue.currentQueueEntryFlow,
                audioPlayer.playerStateFlow,
                audioPlayer.positionFlow) { entry, state, position ->
                Triple(entry, state, position.inWholeMilliseconds)
            }.collect { (entry, state, positionMs) -> onPlayback(entry, state, positionMs) }
        }
    }

    private fun onPlayback(entry: QueueEntry?, state: PlayerState, positionMs: Long) {
        val key = entry?.historyKey()
        if (key != activeKey) {
            activeKey = key
            restart(positionMs)
            return
        }
        if (entry == null) return

        val deltaMs = positionMs - lastPositionMs
        lastPositionMs = positionMs
        if (state != PlayerState.PLAYING) return
        if (deltaMs < 0) {
            // Back to the start: the song is being played again
            if (positionMs <= RESTART_POSITION_MS) restart(positionMs)
            return
        }
        // A bigger jump is a seek, not listening
        if (deltaMs > MAX_COUNTED_DELTA_MS) return

        listenedMs += deltaMs
        if (recorded || listenedMs < thresholdMs(entry)) return
        recorded = true
        val playedAt = nowMs()
        scope.launch {
            try {
                record(entry, playedAt)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A failed write (disk full, I/O error) only loses this one play
                logger.e(e) { "Failed to record listening history" }
            }
        }
    }

    private fun restart(positionMs: Long) {
        lastPositionMs = positionMs
        listenedMs = 0L
        recorded = false
    }

    /** 15 seconds, or half of a song shorter than 30 seconds. */
    private fun thresholdMs(entry: QueueEntry): Long {
        val durationMs = when (entry) {
            is QueueEntry.StreamingTrack -> entry.track.durationMs
            is QueueEntry.LocalTrack -> entry.duration
        }
        return if (durationMs > 0) minOf(LISTENED_MS, durationMs / 2) else LISTENED_MS
    }

    private companion object {
        const val LISTENED_MS = 15_000L
        const val MAX_COUNTED_DELTA_MS = 2_000L
        const val RESTART_POSITION_MS = 5_000L
    }
}
