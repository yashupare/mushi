package com.mushi.app.core.history

import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.FakeAudioPlayer
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueCollectionEntry
import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ListeningHistoryRecorderTest {

    private val player = FakeAudioPlayer()
    private val queue = CurrentEntryQueue()
    private val recorded = mutableListOf<String>()
    private val positions = mutableListOf<Pair<String, Long>>()
    private var positionMs = 0L

    private fun TestScope.start() {
        ListeningHistoryRecorder(
            audioPlayer = player,
            audioPlayerQueue = queue,
            record = { entry, _ -> recorded += entry.url },
            savePosition = { key, position -> positions += key to position },
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
            nowMs = { 0L })
    }

    private fun play(url: String, durationMs: Long = 200_000) {
        positionMs = 0
        player.setPosition(0.milliseconds)
        queue.current.value = QueueEntry.LocalTrack(
            name = url, artists = emptyList(), duration = durationMs, album = null, url = url)
        player.setPlayerState(PlayerState.PLAYING)
    }

    private fun listen(seconds: Int) = repeat(seconds) {
        positionMs += 1_000
        player.setPosition(positionMs.milliseconds)
    }

    @Test
    fun `records a song once after fifteen seconds`() = runTest {
        start()
        play("a")
        listen(14)
        assertEquals(emptyList(), recorded)

        listen(30)
        assertEquals(listOf("a"), recorded)
    }

    @Test
    fun `leaves out skipped songs`() = runTest {
        start()
        play("a")
        listen(5)
        play("b")
        listen(20)

        assertEquals(listOf("b"), recorded)
    }

    @Test
    fun `a forward seek is not listening`() = runTest {
        start()
        play("a")
        listen(5)
        positionMs = 120_000
        player.setPosition(positionMs.milliseconds)
        listen(5)

        assertEquals(emptyList(), recorded)
    }

    @Test
    fun `records a very short song after half of it`() = runTest {
        start()
        play("short", durationMs = 10_000)
        listen(5)

        assertEquals(listOf("short"), recorded)
    }

    @Test
    fun `repeating a song records it again`() = runTest {
        start()
        play("a")
        listen(20)
        positionMs = 0
        player.setPosition(0.milliseconds)
        listen(20)

        assertEquals(listOf("a", "a"), recorded)
    }

    @Test
    fun `saves where a song stopped on pause`() = runTest {
        start()
        play("a")
        listen(20)
        player.setPlayerState(PlayerState.PAUSED)

        assertEquals(listOf("a" to 20_000L), positions)
    }

    @Test
    fun `saves where a song stopped when the next one starts`() = runTest {
        start()
        play("a")
        listen(20)
        play("b")

        assertEquals("a" to 20_000L, positions.last())
    }

    @Test
    fun `saves no position for a skipped song`() = runTest {
        start()
        play("a")
        listen(5)
        play("b")

        assertEquals(emptyList(), positions)
    }

    @Test
    fun `saves the position now and then while playing`() = runTest {
        start()
        play("a")
        listen(50)

        assertEquals(listOf("a" to 45_000L), positions)
    }

    private class CurrentEntryQueue : AudioPlayerQueue {
        val current = MutableStateFlow<QueueEntry?>(null)
        override val queueFlow: StateFlow<List<QueueEntry>> = MutableStateFlow(emptyList())
        override val currentQueueEntryFlow: StateFlow<QueueEntry?> = current
        override val currentCollectionEntryFlow: StateFlow<QueueCollectionEntry?> = MutableStateFlow(null)
        override val collectionHistoryFlow: StateFlow<List<QueueCollectionEntry>> = MutableStateFlow(emptyList())

        override suspend fun load(
            entries: List<QueueEntry>,
            autoPlay: Boolean,
            startPosition: Int,
            collectionEntry: QueueCollectionEntry?) = Unit

        override suspend fun addToQueue(entry: QueueEntry) = Unit
        override suspend fun addAllToQueue(entries: List<QueueEntry>, collectionEntry: QueueCollectionEntry?) = Unit
        override suspend fun addAllAfterCurrent(entries: List<QueueEntry>) = Unit
        override suspend fun removeFromQueue(entry: QueueEntry) = Unit
        override suspend fun removeFromQueueByMediaUrl(mediaUrl: String) = Unit
        override suspend fun move(fromIndex: Int, toIndex: Int) = Unit
        override suspend fun jumpTo(index: Int, autoPlay: Boolean) = Unit
        override suspend fun reloadCurrent() = Unit
        override suspend fun clear() = Unit
        override suspend fun getQueue(): List<QueueEntry> = emptyList()
        override suspend fun getCurrentQueueEntry(): QueueEntry? = current.value
        override suspend fun getCurrentCollectionEntry(): QueueCollectionEntry? = null
        override suspend fun getCollectionHistory(): List<QueueCollectionEntry> = emptyList()
    }
}
