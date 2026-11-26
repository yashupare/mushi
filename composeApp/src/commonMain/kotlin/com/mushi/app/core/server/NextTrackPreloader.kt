package com.mushi.app.core.server

import com.mushi.app.core.audioplayer.LoopState
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * While a song plays, looks up the stream of the one after it, so that pressing next starts at
 * once. Nothing is looked up while paused (a restored queue costs no data), nor for tracks
 * passed over quickly.
 */
class NextTrackPreloader(
    queue: Flow<List<QueueEntry>>,
    currentEntry: Flow<QueueEntry?>,
    playerState: Flow<PlayerState>,
    loopState: Flow<LoopState>,
    preload: suspend (MetadataTrack) -> Unit,
    scope: CoroutineScope,
    settleTime: Duration = 3.seconds) {

    init {
        scope.launch {
            combine(queue, currentEntry, playerState, loopState) { entries, current, state, loop ->
                if (state == PlayerState.PLAYING) nextTrack(entries, current, loop) else null
            }.distinctUntilChanged { old, new -> old?.id == new?.id }
                .collectLatest { track ->
                    if (track == null) return@collectLatest
                    delay(settleTime)
                    // Its own coroutine: a lookup under way must finish even when the queue moves
                    // on, because moving on to this very track is what it was started for.
                    scope.launch { preload(track) }
                }
        }
    }

    private fun nextTrack(entries: List<QueueEntry>, current: QueueEntry?, loop: LoopState): MetadataTrack? {
        val index = entries.indexOf(current)
        if (index < 0) return null
        val next = entries.getOrNull(index + 1)
            ?: entries.firstOrNull()?.takeIf { loop == LoopState.ALL && index > 0 }
        return (next as? QueueEntry.StreamingTrack)?.track
    }
}
