package com.mushi.app.core.playback

import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.audioplayer.QueueEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class StartFromTrackTest {
    private fun track(id: String) = MetadataTrack(
        id = id, title = "Track $id", durationMs = 1000, trackNumber = null, discNumber = null,
        artists = emptyList(), album = null, thumbnails = null, explicit = null,
        popularity = null, isrcCode = null, externalUri = null)

    private fun entries(vararg ids: String) = ids.map { QueueEntry.StreamingTrack(track = track(it), url = "") }

    private val topTracks = entries("a", "b", "c", "d")

    @Test
    fun tappingALaterTrackStartsThere() {
        // The artist-page bug: every tap started at index 0.
        assertEquals(StartFromTrack.Load(2), planStartFromTrack(emptyList(), topTracks, track("c")))
        assertEquals(StartFromTrack.Load(3), planStartFromTrack(entries("x", "y"), topTracks, track("d")))
    }

    @Test
    fun sameListAlreadyQueuedJumpsInsteadOfReloading() {
        assertEquals(StartFromTrack.Jump(1), planStartFromTrack(entries("a", "b", "c", "d"), topTracks, track("b")))
    }

    @Test
    fun matchingTrackInsideAnotherQueueStillLoadsTheTopTracks() {
        // "c" is in the current queue, but that queue is some other list.
        assertEquals(StartFromTrack.Load(2), planStartFromTrack(entries("z", "c"), topTracks, track("c")))
    }

    @Test
    fun unknownTrackStartsFromTheTop() {
        assertEquals(StartFromTrack.Load(0), planStartFromTrack(emptyList(), topTracks, track("nope")))
    }
}
