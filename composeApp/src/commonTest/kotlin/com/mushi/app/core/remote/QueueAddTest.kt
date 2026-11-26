package com.mushi.app.core.remote

import com.mushi.app.core.audioplayer.QueueEntry
import kotlinx.coroutines.test.runTest
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class QueueAddTest {
    private fun track(id: String) = MetadataTrack(
        id = id, title = id, durationMs = 1000, trackNumber = null, discNumber = null,
        artists = emptyList(), album = null, thumbnails = null, explicit = null,
        popularity = null, isrcCode = null, externalUri = null)

    @Test
    fun `adds a track that is not queued and hands back what to undo`() = runTest {
        val added = mutableListOf<QueueEntry>()

        val result = addTrackOnce(emptyList(), track("a")) { added += it }

        assertEquals(added.single(), assertIs<QueueAdd.Added>(result).entry)
    }

    @Test
    fun `a track already in the queue is not added twice`() = runTest {
        val added = mutableListOf<QueueEntry>()
        val queue = listOf(QueueEntry.StreamingTrack(track = track("a"), url = "http://x/stream/a"))

        val result = addTrackOnce(queue, track("a")) { added += it }

        assertEquals(QueueAdd.AlreadyQueued, result)
        assertEquals(emptyList<QueueEntry>(), added)
    }
}
