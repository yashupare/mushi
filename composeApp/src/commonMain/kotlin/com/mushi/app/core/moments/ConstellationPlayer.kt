package com.mushi.app.core.moments

import co.touchlab.kermit.Logger
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.history.historyKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/** The moments being played, and which one is playing now. */
data class PathPlayback(val moments: List<Moment>, val index: Int) {
    val current: Moment get() = moments[index]
}

/**
 * Plays moments in order: each song starts at its kept spot and runs to its end. A song linked
 * twice plays once, from its first moment. Playing anything else ends the path.
 */
class ConstellationPlayer(
    private val audioPlayer: AudioPlayerInterface,
    private val audioPlayerQueue: AudioPlayerQueue,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)) {

    private val logger = Logger.withTag("ConstellationPlayer")

    private val _playback = MutableStateFlow<PathPlayback?>(null)
    val playback: StateFlow<PathPlayback?> = _playback.asStateFlow()

    /** Asked for but not yet seen in the player, whose state arrives a moment later. */
    private var pending: List<Moment>? = null

    init {
        scope.launch {
            combine(audioPlayerQueue.currentQueueEntryFlow, audioPlayerQueue.queueFlow) { current, queue ->
                current?.historyKey() to queue.mapTo(mutableSetOf()) { it.historyKey() }
            }.collect { (currentKey, queuedKeys) -> follow(currentKey, queuedKeys) }
        }
    }

    fun play(moments: List<Moment>) {
        val path = moments.distinctBy { it.songKey }
        if (path.isEmpty()) return
        scope.launch {
            try {
                _playback.value = null
                pending = path
                audioPlayerQueue.load(path.map { it.entry })
                // The songs may already have been in the player, with nothing new to follow
                follow(
                    audioPlayerQueue.getCurrentQueueEntry()?.historyKey(),
                    audioPlayerQueue.getQueue().mapTo(mutableSetOf()) { it.historyKey() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Failed to play the path" }
                pending = null
            }
        }
    }

    private suspend fun follow(currentKey: String?, queuedKeys: Set<String>) {
        pending?.let { path ->
            if (currentKey == path.first().songKey && queuedKeys.containsAll(path.map { it.songKey })) {
                pending = null
                start(path, 0)
            }
            return
        }
        val active = _playback.value ?: return
        val index = active.moments.indexOfFirst { it.songKey == currentKey }
        when {
            index < 0 || !queuedKeys.containsAll(active.moments.map { it.songKey }) -> _playback.value = null
            index != active.index -> start(active.moments, index)
        }
    }

    private suspend fun start(path: List<Moment>, index: Int) {
        _playback.value = PathPlayback(path, index)
        audioPlayer.seekTo(path[index].positionMs.milliseconds)
    }
}
