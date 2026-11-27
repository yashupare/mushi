package com.mushi.app.modules.home

import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.HistoryItem
import kotlin.test.Test
import kotlin.test.assertEquals

class ContinueTest {

    private fun item(url: String, positionMs: Long, playedAt: Long = 0) = HistoryItem(
        entry = QueueEntry.LocalTrack(
            name = url, artists = emptyList(), duration = 200_000, album = null, url = url),
        playedAtMs = listOf(playedAt),
        playCount = 1,
        positionMs = positionMs)

    @Test
    fun `only unfinished songs are offered, newest first`() {
        val cards = listOf(
            item("barely started", 5_000, playedAt = 4),
            item("older", 60_000, playedAt = 1),
            item("finished", 195_000, playedAt = 3),
            item("newer", 122_000, playedAt = 2)).toContinueCards(activeKey = null)

        assertEquals(listOf("newer", "older"), cards.map { it.key })
        assertEquals("1:18", cards.first().timeLeft)
        assertEquals(0.61f, cards.first().progress)
    }

    @Test
    fun `the song under way is left out`() {
        val cards = listOf(item("a", 60_000), item("b", 60_000)).toContinueCards(activeKey = "a")

        assertEquals(listOf("b"), cards.map { it.key })
    }

    @Test
    fun `the sky follows the hour`() {
        assertEquals(
            listOf(SkyTime.Night, SkyTime.Morning, SkyTime.Day, SkyTime.Evening, SkyTime.Night),
            listOf(4, 5, 11, 18, 21).map(::skyTimeFor))
    }
}
