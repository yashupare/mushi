package com.mushi.app.core.server

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TrackPreloaderTest {
    private val track = MetadataTrack(
        id = "a", title = "a", durationMs = 1000, trackNumber = null, discNumber = null,
        artists = emptyList(), album = null, thumbnails = null, explicit = null,
        popularity = null, isrcCode = null, externalUri = null)

    @Test
    fun `looks the track up once the screen has settled`() = runTest {
        val lookedUp = mutableListOf<String>()
        val preloader = TrackPreloader({ lookedUp += it.id }, backgroundScope)

        backgroundScope.launch { preloader.preloadOnceSettled(track) }
        advanceTimeBy(500)
        assertEquals(emptyList<String>(), lookedUp)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(listOf("a"), lookedUp)
    }

    @Test
    fun `leaving the screen straight away looks nothing up`() = runTest {
        val lookedUp = mutableListOf<String>()
        val preloader = TrackPreloader({ lookedUp += it.id }, backgroundScope)

        val screen = backgroundScope.launch { preloader.preloadOnceSettled(track) }
        advanceTimeBy(500)
        screen.cancel()
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(emptyList<String>(), lookedUp)
    }
}
