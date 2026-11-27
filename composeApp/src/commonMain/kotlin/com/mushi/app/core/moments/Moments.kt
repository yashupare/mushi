package com.mushi.app.core.moments

import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.historyKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Instant

/** A spot in a song the listener kept. [x] and [y] place its star in that month's sky. */
@Serializable
data class Moment(
    val id: String,
    val entry: QueueEntry,
    val positionMs: Long,
    val keptAtMs: Long,
    val x: Float,
    val y: Float) {
    val songKey: String get() = entry.historyKey()
}

/** Stars linked into a mix, in playing order. */
@Serializable
data class Constellation(val id: String, val name: String, val momentIds: List<String>)

@Serializable
data class MomentLibrary(
    val moments: List<Moment> = emptyList(),
    val constellations: List<Constellation> = emptyList())

/** Keeping again this close to a kept spot removes it instead. */
internal const val SAME_MOMENT_MS = 5_000L

/** "2026-11": each month has its own sky, so stars never get crowded forever. */
internal fun monthKey(atMs: Long, zone: TimeZone): String =
    Instant.fromEpochMilliseconds(atMs).toLocalDateTime(zone).date.toString().take(7)

internal fun MomentLibrary.momentNear(songKey: String, positionMs: Long): Moment? =
    moments.firstOrNull { it.songKey == songKey && abs(it.positionMs - positionMs) <= SAME_MOMENT_MS }

/** Keeps [positionMs] of [entry], or removes the moment already kept there. */
internal fun MomentLibrary.toggled(
    entry: QueueEntry,
    positionMs: Long,
    nowMs: Long,
    zone: TimeZone): MomentLibrary {
    momentNear(entry.historyKey(), positionMs)?.let { return without(it.id) }
    val month = monthKey(nowMs, zone)
    val (x, y) = freeSpot(moments.filter { monthKey(it.keptAtMs, zone) == month })
    val moment = Moment(
        id = "$nowMs-${entry.historyKey().hashCode()}",
        entry = when (entry) {
            // The stream url points at this launch's local proxy; it's rebuilt when played
            is QueueEntry.StreamingTrack -> entry.copy(url = "", addedBy = "")
            is QueueEntry.LocalTrack -> entry.copy(coverBytes = null, addedBy = "")
        },
        positionMs = positionMs,
        keptAtMs = nowMs,
        x = x,
        y = y)
    return copy(moments = moments + moment)
}

/** Removes a moment, and any constellation left with fewer than two stars. */
internal fun MomentLibrary.without(momentId: String): MomentLibrary = MomentLibrary(
    moments = moments.filterNot { it.id == momentId },
    constellations = constellations
        .map { it.copy(momentIds = it.momentIds - momentId) }
        .filter { it.momentIds.size > 1 })

internal fun MomentLibrary.withConstellation(id: String, name: String, momentIds: List<String>): MomentLibrary =
    copy(constellations = listOf(Constellation(id, name, momentIds)) + constellations)

/**
 * The emptiest spot of a sky, as fractions of its size. The top is left for the sky's title.
 * The small offsets keep stars from looking like a grid.
 */
internal fun freeSpot(others: List<Moment>): Pair<Float, Float> {
    var best = 0.5f to 0.55f
    var bestDistance = -1f
    for (column in 0..11) for (row in 0..8) {
        val x = 0.09f + column * 0.072f + ((row * 5 + column * 3) % 7 - 3) * 0.008f
        val y = 0.24f + row * 0.081f + ((column * 7 + row * 2) % 5 - 2) * 0.012f
        val nearest = others.minOfOrNull { sqrt((it.x - x) * (it.x - x) + (it.y - y) * (it.y - y)) }
        // An empty sky starts near its middle
        val distance = nearest ?: (1f - abs(x - 0.45f) - abs(y - 0.55f))
        if (distance > bestDistance) {
            bestDistance = distance
            best = x to y
        }
    }
    return best
}
