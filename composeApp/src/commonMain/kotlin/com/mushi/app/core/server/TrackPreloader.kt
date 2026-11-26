package com.mushi.app.core.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Looks up a track's stream before it is asked for, so that playing it starts sooner. Nothing is
 * downloaded: only the stream's address is found and remembered.
 */
class TrackPreloader(
    private val lookUp: suspend (MetadataTrack) -> Unit,
    private val scope: CoroutineScope) {

    /**
     * For a track on a screen the user has just opened. Cancelling the caller within [settleTime]
     * (the user moved straight on) costs nothing. A lookup that has started runs to the end.
     */
    suspend fun preloadOnceSettled(track: MetadataTrack, settleTime: Duration = 1.seconds) {
        delay(settleTime)
        scope.launch { lookUp(track) }
    }
}
