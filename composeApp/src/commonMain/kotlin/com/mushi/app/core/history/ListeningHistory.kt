package com.mushi.app.core.history

import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.serialization.Serializable

/** One song in the listening history, with when it was played (newest first). */
@Serializable
data class HistoryItem(
    val entry: QueueEntry,
    val playedAtMs: List<Long>,
    val playCount: Int,
    /** Where playback of the latest play stopped. */
    val positionMs: Long = 0) {
    val key: String get() = entry.historyKey()
    val lastPlayedAtMs: Long get() = playedAtMs.firstOrNull() ?: 0L
}

/** Where to pick this song up again, or null if it was barely started or all but finished. */
internal fun HistoryItem.resumeAtMs(): Long? {
    val durationMs = entry.durationMs()
    return positionMs.takeIf { it >= MIN_RESUME_MS && durationMs - it >= MIN_LEFT_MS }
}

internal fun QueueEntry.durationMs(): Long = when (this) {
    is QueueEntry.StreamingTrack -> track.durationMs
    is QueueEntry.LocalTrack -> duration
}

/** The history with the stop position of one song updated; the order stays. */
internal fun List<HistoryItem>.withPosition(key: String, positionMs: Long): List<HistoryItem> =
    map { if (it.key == key) it.copy(positionMs = positionMs) else it }

private const val MIN_RESUME_MS = 15_000L
private const val MIN_LEFT_MS = 20_000L

internal const val MAX_HISTORY_ITEMS = 200
internal const val MAX_PLAY_TIMES_PER_ITEM = 20

internal fun QueueEntry.historyKey(): String = when (this) {
    is QueueEntry.StreamingTrack -> track.id
    is QueueEntry.LocalTrack -> url
}

/**
 * A copy that's safe to keep. A streaming url points at this launch's local proxy, so it's
 * dropped and rebuilt when played; a local track's url is its file, so it stays.
 */
private fun QueueEntry.forHistory(): QueueEntry = when (this) {
    is QueueEntry.StreamingTrack -> copy(url = "", addedBy = "")
    is QueueEntry.LocalTrack -> copy(coverBytes = null, addedBy = "")
}

/** The history after [entry] was played at [playedAtMs]: that song first, one item per song. */
internal fun List<HistoryItem>.withPlay(entry: QueueEntry, playedAtMs: Long): List<HistoryItem> {
    val key = entry.historyKey()
    val previous = firstOrNull { it.key == key }
    val item = HistoryItem(
        entry = entry.forHistory(),
        playedAtMs = (listOf(playedAtMs) + previous?.playedAtMs.orEmpty()).take(MAX_PLAY_TIMES_PER_ITEM),
        playCount = (previous?.playCount ?: 0) + 1)
    return (listOf(item) + filterNot { it.key == key }).take(MAX_HISTORY_ITEMS)
}
