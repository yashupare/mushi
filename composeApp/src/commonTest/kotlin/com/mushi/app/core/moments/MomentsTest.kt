package com.mushi.app.core.moments

import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.datetime.TimeZone
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MomentsTest {

    private fun song(url: String) = QueueEntry.LocalTrack(
        name = url, artists = emptyList(), duration = 200_000, album = null, url = url)

    private fun MomentLibrary.keep(url: String, positionMs: Long, nowMs: Long = positionMs) =
        toggled(song(url), positionMs, nowMs, TimeZone.UTC)

    @Test
    fun `keeping twice in the same spot removes the moment`() {
        val once = MomentLibrary().keep("a", 60_000, nowMs = 1)

        assertEquals(1, once.moments.size)
        assertEquals(0, once.keep("a", 63_000, nowMs = 2).moments.size)
        assertEquals(2, once.keep("a", 90_000, nowMs = 2).moments.size)
    }

    @Test
    fun `removing a star drops constellations left too short`() {
        val library = MomentLibrary().keep("a", 1_000).keep("b", 2_000).keep("c", 3_000)
        val (a, b, c) = library.moments.map { it.id }
        val saved = library.withConstellation("1", "two", listOf(a, b))
            .withConstellation("2", "three", listOf(a, b, c))

        val after = saved.without(a)

        assertEquals(listOf("three"), after.constellations.map { it.name })
        assertEquals(listOf(b, c), after.constellations.single().momentIds)
    }

    @Test
    fun `stars keep apart and inside the sky`() {
        var library = MomentLibrary()
        repeat(20) { library = library.keep("s$it", 10_000, nowMs = it.toLong()) }

        val stars = library.moments
        assertTrue(stars.all { it.x in 0.04f..0.96f && it.y in 0.18f..0.95f })
        val closest = stars.indices.flatMap { i ->
            (i + 1 until stars.size).map { j -> hypot(stars[i].x - stars[j].x, stars[i].y - stars[j].y) }
        }.min()
        assertTrue(closest > 0.08f, "stars $closest apart can't be tapped separately")
    }

    @Test
    fun `each month has its own sky`() {
        val november = 1_795_000_000_000
        val january = november + 60L * 24 * 3600 * 1000

        assertTrue(monthKey(november, TimeZone.UTC) != monthKey(january, TimeZone.UTC))
        assertEquals(7, monthKey(november, TimeZone.UTC).length)
    }
}
