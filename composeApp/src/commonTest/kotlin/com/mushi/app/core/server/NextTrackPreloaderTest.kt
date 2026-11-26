package com.mushi.app.core.server

import com.mushi.app.core.audioplayer.LoopState
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NextTrackPreloaderTest {
    private fun entry(id: String) = QueueEntry.StreamingTrack(
        track = MetadataTrack(
            id = id, title = id, durationMs = 1000, trackNumber = null, discNumber = null,
            artists = emptyList(), album = null, thumbnails = null, explicit = null,
            popularity = null, isrcCode = null, externalUri = null),
        url = "")

    private class Harness(scope: TestScope, entries: List<QueueEntry>) {
        val current = MutableStateFlow<QueueEntry?>(entries.first())
        val state = MutableStateFlow(PlayerState.PLAYING)
        val loop = MutableStateFlow(LoopState.NONE)
        val preloaded = mutableListOf<String>()

        init {
            NextTrackPreloader(
                queue = MutableStateFlow(entries),
                currentEntry = current,
                playerState = state,
                loopState = loop,
                preload = { preloaded += it.id },
                scope = scope.backgroundScope)
        }
    }

    private fun TestScope.settle() {
        advanceTimeBy(4_000)
        runCurrent()
    }

    @Test
    fun `preloads the following track once playback has settled`() = runTest {
        val h = Harness(this, listOf(entry("a"), entry("b"), entry("c")))
        advanceTimeBy(1_000)
        assertEquals(emptyList<String>(), h.preloaded)
        settle()
        assertEquals(listOf("b"), h.preloaded)
    }

    @Test
    fun `does nothing while paused`() = runTest {
        val h = Harness(this, listOf(entry("a"), entry("b")))
        h.state.value = PlayerState.PAUSED
        settle()
        assertEquals(emptyList<String>(), h.preloaded)
    }

    @Test
    fun `skips over tracks passed quickly`() = runTest {
        val entries = listOf(entry("a"), entry("b"), entry("c"))
        val h = Harness(this, entries)
        advanceTimeBy(1_000)
        h.current.value = entries[1]
        settle()
        assertEquals(listOf("c"), h.preloaded)
    }

    @Test
    fun `wraps to the first track only when the whole queue repeats`() = runTest {
        val entries = listOf(entry("a"), entry("b"))
        val h = Harness(this, entries)
        h.current.value = entries[1]
        settle()
        assertEquals(emptyList<String>(), h.preloaded)
        h.loop.value = LoopState.ALL
        settle()
        assertEquals(listOf("a"), h.preloaded)
    }
}
