package com.mushi.app.core.server

import co.touchlab.kermit.Logger
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.db.Database
import com.mushi.app.core.paths.Paths
import com.mushi.app.modules.settings.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondBytesWriter
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.delay
import kotlin.test.assertTrue
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioSource
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioStream
import org.spotube.plugin_interfaces.plugin_apis.audio.StreamProtocol
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * googlevideo throttles a request without a `Range` header to about playback speed, which made
 * every song take seconds to start. The player sends none when it starts from the beginning.
 */
class StreamProxyRangeTest {
    private companion object {
        // One per process: DataStore rejects a second instance on the same file.
        val cacheManager by lazy { CacheManager(Paths(), SettingsRepository(Database(Paths()))) }
    }

    private val song = ByteArray(4096) { (it % 251).toByte() }
    private var upstreamRange: String? = null
    private var cachingEnabled = false
    private var upstreamStalls = false
    private lateinit var upstream: EmbeddedServer<*, *>
    private lateinit var proxy: EmbeddedServer<*, *>
    private var proxyPort = 0

    @BeforeTest
    fun start() = runBlocking {
        startKoin { modules(module { factory<Logger> { Logger.withTag("test") } }) }

        val upstreamServer = startServerWithPortFallback("127.0.0.1", 0) {
            routing {
                get("/audio") {
                    upstreamRange = call.request.headers[HttpHeaders.Range]
                    val from = upstreamRange?.removePrefix("bytes=")?.substringBefore('-')?.toInt()
                    if (from != null) {
                        call.response.header(HttpHeaders.ContentRange, "bytes $from-${song.size - 1}/${song.size}")
                    }
                    if (upstreamStalls) {
                        // A few bytes, then nothing: a download that does not finish
                        call.respondBytesWriter(ContentType("audio", "webm")) {
                            writeFully(song, 0, 100)
                            flush()
                            delay(20_000)
                        }
                        return@get
                    }
                    call.respondBytes(
                        song.copyOfRange(from ?: 0, song.size),
                        ContentType("audio", "webm"),
                        if (from == null) HttpStatusCode.OK else HttpStatusCode.PartialContent)
                }
            }
        }
        upstream = upstreamServer.engine
        val upstreamPort = upstreamServer.port

        val track = MetadataTrack(
            id = "t1", title = "Song", durationMs = 1000, trackNumber = null, discNumber = null,
            artists = emptyList(), album = null, thumbnails = null, explicit = null,
            popularity = null, isrcCode = null, externalUri = null)
        val api = FakeAudioAPI(onGetStreamsByTrack = {
            listOf(
                AudioSource.Streamed(
                    id = "s1", title = "Song", artist = "Artist", album = null, thumbnails = emptyList(),
                    externalUri = "", confidence = 1f,
                    streams = listOf(
                        AudioStream.Lossy(
                            url = "http://127.0.0.1:$upstreamPort/audio", codec = "opus", container = "webm",
                            bitrate = 128_000, protocol = StreamProtocol.PROGRESSIVE))))
        })
        val queue = FakeStreamingAudioPlayerQueue().apply {
            setQueue(listOf(QueueEntry.StreamingTrack(track = track, url = "")))
        }
        val repository = StreamingUrlRepository(
            pluginManager = FakeAudioPluginSource(FakeAudioPluginService("p", api)),
            audioPlayerQueue = queue,
            matchedTracksRepository = FakeTrackSourceRepository(),
            settingsRepository = FakeUserSettingsSource())
        val streamProxy = StreamProxy(
            httpClient = HttpClient(),
            streamingUrlRepository = repository,
            cacheManager = cacheManager,
            audioPlayerQueue = queue,
            isCachingEnabled = { cachingEnabled },
            activePort = { proxyPort },
            scope = CoroutineScope(Dispatchers.IO))

        val proxyServer = startServerWithPortFallback("127.0.0.1", 0) {
            routing { get("/stream/{trackId}") { streamProxy.handleStreamRequest(call, HttpMethod.Get) } }
        }
        proxy = proxyServer.engine
        proxyPort = proxyServer.port
    }

    @AfterTest
    fun stop() {
        proxy.stop()
        upstream.stop()
        stopKoin()
    }

    private fun request(range: String?): HttpURLConnection =
        (URI("http://127.0.0.1:$proxyPort/stream/t1").toURL().openConnection() as HttpURLConnection).apply {
            range?.let { setRequestProperty("Range", it) }
        }

    @Test
    fun `asks upstream for a range but gives a player without one a normal 200`() {
        val response = request(range = null)

        assertEquals(200, response.responseCode)
        assertEquals("bytes=0-", upstreamRange)
        assertNull(response.getHeaderField("Content-Range"))
        assertEquals(song.size, response.contentLength)
        assertContentEquals(song, response.inputStream.readBytes())
    }

    @Test
    fun `a second request does not wait for the first one to finish caching`() {
        cachingEnabled = true
        upstreamStalls = true
        val first = request(range = null)
        first.inputStream.read()

        val started = System.currentTimeMillis()
        val second = request(range = null)
        second.inputStream.read()
        val waited = System.currentTimeMillis() - started
        first.disconnect()
        second.disconnect()

        assertTrue(waited < 2_000, "second request waited ${waited}ms")
    }

    @Test
    fun `forwards the player's own range and its 206 unchanged`() {
        val response = request(range = "bytes=1000-")

        assertEquals(206, response.responseCode)
        assertEquals("bytes=1000-", upstreamRange)
        assertEquals("bytes 1000-4095/4096", response.getHeaderField("Content-Range"))
        assertContentEquals(song.copyOfRange(1000, song.size), response.inputStream.readBytes())
    }
}
