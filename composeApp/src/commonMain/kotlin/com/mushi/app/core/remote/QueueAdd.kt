package com.mushi.app.core.remote

import com.mushi.app.core.audioplayer.QueueEntry
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

/** What asking to queue one track did, for the message that follows it. */
sealed interface QueueAdd {
    /** [entry] is what to take out again on undo. */
    data class Added(val entry: QueueEntry) : QueueAdd
    data object AlreadyQueued : QueueAdd
}

/**
 * Adds [track] unless the queue already holds it, matching the song menu, which only offers
 * "Add to queue" for songs that aren't in it.
 */
internal suspend fun addTrackOnce(
    queue: List<QueueEntry>,
    track: MetadataTrack,
    add: suspend (QueueEntry) -> Unit
): QueueAdd {
    if (queue.any { (it as? QueueEntry.StreamingTrack)?.track?.id == track.id }) {
        return QueueAdd.AlreadyQueued
    }
    val entry = QueueEntry.StreamingTrack(track = track, url = "")
    add(entry)
    return QueueAdd.Added(entry)
}
