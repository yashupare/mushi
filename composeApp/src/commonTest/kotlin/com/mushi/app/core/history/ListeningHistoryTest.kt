package com.mushi.app.core.history

import com.mushi.app.core.audioplayer.QueueEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class ListeningHistoryTest {

    private fun song(url: String) = QueueEntry.LocalTrack(
        name = url,
        artists = listOf("Artist"),
        duration = 200_000,
        album = null,
        url = url)

    @Test
    fun `newest song comes first`() {
        val history = emptyList<HistoryItem>()
            .withPlay(song("a"), 1)
            .withPlay(song("b"), 2)

        assertEquals(listOf("b", "a"), history.map { it.key })
    }

    @Test
    fun `replaying a song moves it up and counts it once more`() {
        val history = emptyList<HistoryItem>()
            .withPlay(song("a"), 1)
            .withPlay(song("b"), 2)
            .withPlay(song("a"), 3)

        assertEquals(listOf("a", "b"), history.map { it.key })
        assertEquals(2, history.first().playCount)
        assertEquals(listOf(3L, 1L), history.first().playedAtMs)
    }

    @Test
    fun `history and play times are capped`() {
        var history = emptyList<HistoryItem>()
        repeat(MAX_HISTORY_ITEMS + 5) { history = history.withPlay(song("s$it"), it.toLong()) }
        repeat(MAX_PLAY_TIMES_PER_ITEM + 5) { history = history.withPlay(song("again"), 1_000L + it) }

        assertEquals(MAX_HISTORY_ITEMS, history.size)
        assertEquals(MAX_PLAY_TIMES_PER_ITEM, history.first().playedAtMs.size)
        assertEquals(MAX_PLAY_TIMES_PER_ITEM + 5, history.first().playCount)
    }
}
