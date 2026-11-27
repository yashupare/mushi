package com.mushi.app.core.moments

import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.FakeAudioPlayer
import com.mushi.app.core.audioplayer.QueueCollectionEntry
import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class ConstellationPlayerTest {

    private val seeks = mutableListOf<Long>()
    private val fake = FakeAudioPlayer()
    private val audioPlayer = object : AudioPlayerInterface by fake {
        override suspend fun seekTo(position: Duration) {
            seeks += position.inWholeMilliseconds
        }
    }
    private val queue = ListQueue()

    private fun moment(url: String, positionMs: Long) = Moment(
        id = "$url-$positionMs",
        entry = QueueEntry.LocalTrack(
            name = url, artists = emptyList(), duration = 200_000, album = null, url = url),
        positionMs = positionMs,
        keptAtMs = 0,
        x = 0f,
        y = 0f)

    private fun TestScope.player() = ConstellationPlayer(
        audioPlayer,
        queue,
        CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun `each song starts at its kept spot`() = runTest {
        val player = player()
        player.play(listOf(moment("a", 40_000), moment("b", 70_000)))
        queue.current.value = queue.queueFlow.value[1]

        assertEquals(listOf(40_000L, 70_000L), seeks)
        assertEquals(1, player.playback.value?.index)
    }

    @Test
    fun `a song linked twice plays once`() = runTest {
        val player = player()
        player.play(listOf(moment("a", 40_000), moment("b", 70_000), moment("a", 90_000)))

        assertEquals(listOf("a", "b"), queue.queueFlow.value.map { it.url })
    }

    @Test
    fun `playing something else ends the path`() = runTest {
        val player = player()
        player.play(listOf(moment("a", 40_000), moment("b", 70_000)))
        queue.load(listOf(moment("z", 0).entry))

        assertNull(player.playback.value)
        assertEquals(listOf(40_000L), seeks)
    }

    private class ListQueue : AudioPlayerQueue {
        override val queueFlow = MutableStateFlow<List<QueueEntry>>(emptyList())
        val current = MutableStateFlow<QueueEntry?>(null)
        override val currentQueueEntryFlow = current
        override val currentCollectionEntryFlow = MutableStateFlow<QueueCollectionEntry?>(null)
        override val collectionHistoryFlow = MutableStateFlow<List<QueueCollectionEntry>>(emptyList())

        override suspend fun load(
            entries: List<QueueEntry>,
            autoPlay: Boolean,
            startPosition: Int,
            collectionEntry: QueueCollectionEntry?) {
            queueFlow.value = entries
            current.value = entries.getOrNull(startPosition)
        }

        override suspend fun addToQueue(entry: QueueEntry) = Unit
        override suspend fun addAllToQueue(entries: List<QueueEntry>, collectionEntry: QueueCollectionEntry?) = Unit
        override suspend fun addAllAfterCurrent(entries: List<QueueEntry>) = Unit
        override suspend fun removeFromQueue(entry: QueueEntry) = Unit
        override suspend fun removeFromQueueByMediaUrl(mediaUrl: String) = Unit
        override suspend fun move(fromIndex: Int, toIndex: Int) = Unit
        override suspend fun jumpTo(index: Int, autoPlay: Boolean) = Unit
        override suspend fun reloadCurrent() = Unit
        override suspend fun clear() = Unit
        override suspend fun getQueue(): List<QueueEntry> = queueFlow.value
        override suspend fun getCurrentQueueEntry(): QueueEntry? = current.value
        override suspend fun getCurrentCollectionEntry(): QueueCollectionEntry? = null
        override suspend fun getCollectionHistory(): List<QueueCollectionEntry> = emptyList()
    }
}
