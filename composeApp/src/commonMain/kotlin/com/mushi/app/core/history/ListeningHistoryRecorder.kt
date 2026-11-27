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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Watches local playback and records a song once it has actually been listened to, so skipped
 * songs stay out. Only time spent playing counts: pauses and forward seeks add nothing.
 * Each play is recorded once; replaying or repeating the song counts again.
 *
 * For a recorded song it also saves where playback stopped: on pause, on moving to another
 * song, and every so often while playing in case the app is closed.
 */
class ListeningHistoryRecorder(
    audioPlayer: AudioPlayerInterface,
    audioPlayerQueue: AudioPlayerQueue,
    private val record: suspend (QueueEntry, Long) -> Unit,
    private val savePosition: suspend (String, Long) -> Unit = { _, _ -> },
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }) {

    private val logger = Logger.withTag("ListeningHistory")

    private var activeKey: String? = null
    private var lastPositionMs = 0L
    private var listenedMs = 0L
    private var recorded = false
    private var wasPlaying = false
    private var unsavedPositionMs: Long? = null
    private var sinceSaveMs = 0L

    /** One at a time and in order, so a position is never written before its play. */
    private val writes = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (write in writes) {
                try {
                    write()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // A failed write (disk full, I/O error) only loses this one update
                    logger.e(e) { "Failed to write listening history" }
                }
            }
        }
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
        val stopped = wasPlaying && state != PlayerState.PLAYING
        wasPlaying = state == PlayerState.PLAYING
        if (key != activeKey) {
            flushPosition()
            activeKey = key
            restart(positionMs)
            return
        }
        if (entry == null) return

        val deltaMs = positionMs - lastPositionMs
        lastPositionMs = positionMs
        // A jump back to the very start is a replay or the next song loading, not a stop position
        if (recorded && (deltaMs >= 0 || positionMs > RESTART_POSITION_MS)) unsavedPositionMs = positionMs
        if (stopped) flushPosition()
        if (state != PlayerState.PLAYING) return
        if (deltaMs < 0) {
            // Back to the start: the song is being played again
            if (positionMs <= RESTART_POSITION_MS) restart(positionMs)
            return
        }
        // A bigger jump is a seek, not listening
        if (deltaMs > MAX_COUNTED_DELTA_MS) return

        listenedMs += deltaMs
        if (recorded) {
            sinceSaveMs += deltaMs
            if (sinceSaveMs >= SAVE_EVERY_MS) flushPosition()
            return
        }
        if (listenedMs < thresholdMs(entry)) return
        recorded = true
        unsavedPositionMs = positionMs
        sinceSaveMs = 0
        val playedAt = nowMs()
        writes.trySend { record(entry, playedAt) }
    }

    private fun flushPosition() {
        val key = activeKey ?: return
        val positionMs = unsavedPositionMs ?: return
        unsavedPositionMs = null
        sinceSaveMs = 0
        writes.trySend { savePosition(key, positionMs) }
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
        const val SAVE_EVERY_MS = 30_000L
    }
}
