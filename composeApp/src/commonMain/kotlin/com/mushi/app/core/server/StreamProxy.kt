package com.mushi.app.core.server

import org.spotube.plugin_interfaces.plugin_apis.audio.StreamProtocol
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.di.injectLogger
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLParameter
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.util.cio.ChannelWriteException
import io.ktor.utils.io.ClosedWriteChannelException
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import org.koin.core.component.KoinComponent
import kotlin.time.TimeSource

internal class StreamProxy(
    private val httpClient: HttpClient,
    private val streamingUrlRepository: StreamingUrlRepository,
    private val cacheManager: CacheManager,
    private val audioPlayerQueue: AudioPlayerQueue,
    private val isCachingEnabled: () -> Boolean,
    private val activePort: () -> Int?,
    private val scope: CoroutineScope): KoinComponent {
    val logger by injectLogger<StreamProxy>()

    companion object {
        private const val HOST = "127.0.0.1"
    }

    private val fileSystem = FileSystem.SYSTEM
    private val streamingInProgress = mutableSetOf<String>()
    private val streamingMutex = Mutex()

    private fun getTrackMetadata(trackId: String): MetadataTrack? {
        return audioPlayerQueue.queueFlow.value
            .filterIsInstance<QueueEntry.StreamingTrack>()
            .firstOrNull { it.track.id == trackId }
            ?.track
    }

    suspend fun handleStreamRequest(call: ApplicationCall, requestMethod: HttpMethod) {
        val trackId = call.parameters["trackId"]?.trim().orEmpty()
        if (trackId.isBlank()) {
            logger.w { "Rejecting /stream request with missing track id" }
            call.respond(HttpStatusCode.BadRequest, "Missing track id")
            return
        }

        logger.i {
            "[Req] $requestMethod /stream/$trackId from=${call.request.local.remoteHost} " +
                "version=${call.request.local.version} range=${call.request.headers[HttpHeaders.Range]} " +
                "ua=${call.request.headers[HttpHeaders.UserAgent]} " +
                "connection=${call.request.headers[HttpHeaders.Connection]} caching=${isCachingEnabled()}"
        }

        if (isCachingEnabled()) {
            val cachedEntry = cacheManager.findCachedEntry(trackId)
            if (cachedEntry != null) {
                val (filePath, entry) = cachedEntry
                logger.d { "Serving track $trackId from cache" }
                when (requestMethod) {
                    HttpMethod.Head -> cacheManager.serveCacheHead(call, entry)
                    HttpMethod.Get -> cacheManager.serveCacheGet(call, filePath, entry)
                    else -> call.respond(HttpStatusCode.MethodNotAllowed)
                }
                return
            }

            // Only one request writes a track's cache file. Others (the player asking twice, or
            // seeking mid-download) stream straight from upstream: holding them until the file
            // is complete stalled playback for seconds.
            val writesCache = streamingMutex.withLock { streamingInProgress.add(trackId) }
            try {
                doStreamFromUpstream(call, trackId, requestMethod, allowCache = writesCache)
            } finally {
                if (writesCache) {
                    streamingMutex.withLock { streamingInProgress.remove(trackId) }
                }
            }
            return
        }

        doStreamFromUpstream(call, trackId, requestMethod)
    }

    private suspend fun doStreamFromUpstream(
        call: ApplicationCall,
        trackId: String,
        requestMethod: HttpMethod,
        allowCache: Boolean = false
    ) {
        val resolveStart = TimeSource.Monotonic.markNow()
        val streamInfo = streamingUrlRepository.resolveStreamInfo(trackId)
        logger.i {
            "[Req] resolved stream for $trackId in ${resolveStart.elapsedNow()}: " +
                "protocol=${streamInfo?.protocol} codec=${streamInfo?.codec} container=${streamInfo?.container} " +
                "url=${streamInfo?.url?.take(120)}"
        }
        if (streamInfo == null) {
            logger.w { "Unable to resolve stream for track $trackId" }
            call.respond(HttpStatusCode.NotFound, "Unable to resolve stream for track $trackId")
            return
        }

        if (streamInfo.protocol != StreamProtocol.PROGRESSIVE) {
            val port = activePort() ?: 8080
            val manifestUrl = "http://$HOST:$port/manifest/${trackId}"
            logger.d { "Redirecting $trackId to manifest URL: $manifestUrl" }
            call.response.headers.append(HttpHeaders.Location, manifestUrl)
            call.respond(HttpStatusCode.Found)
            return
        }

        proxyStream(call, streamInfo.url, trackId, requestMethod, allowCache)
    }

    suspend fun handleManifestRequest(call: ApplicationCall) {
        val trackId = call.parameters["trackId"]?.trim().orEmpty()
        if (trackId.isBlank()) {
            logger.w { "Rejecting /manifest request with missing track id" }
            call.respond(HttpStatusCode.BadRequest, "Missing track id")
            return
        }

        val streamInfo = streamingUrlRepository.resolveStreamInfo(trackId)
        if (streamInfo == null) {
            logger.w { "Unable to resolve stream for track $trackId" }
            call.respond(HttpStatusCode.NotFound, "Unable to resolve stream for track $trackId")
            return
        }

        if (streamInfo.protocol == StreamProtocol.PROGRESSIVE) {
            logger.w { "Track $trackId is not a manifest stream" }
            call.respond(HttpStatusCode.BadRequest, "Not a manifest stream")
            return
        }

        proxyManifest(call, streamInfo.url, trackId, streamInfo.protocol)
    }

    suspend fun handleSegmentRequest(call: ApplicationCall) {
        val trackId = call.parameters["trackId"]?.trim().orEmpty()
        val segmentUrl = call.parameters["url"]?.let { decodeUrl(it) }

        if (trackId.isBlank() || segmentUrl.isNullOrBlank()) {
            logger.w { "Rejecting /segment request with missing parameters" }
            call.respond(HttpStatusCode.BadRequest, "Missing track id or segment url")
            return
        }

        proxySegment(call, segmentUrl, trackId)
    }

    private suspend fun proxyManifest(
        call: ApplicationCall,
        manifestUrl: String,
        trackId: String,
        protocol: StreamProtocol
    ) {
        logger.d { "Proxying manifest for track $trackId ($protocol): $manifestUrl" }

        val response = runCatching {
            httpClient.request(manifestUrl) {
                method = HttpMethod.Get
                call.forwardRequestHeaderIfPresent(headers, HttpHeaders.Accept)
                call.forwardRequestHeaderIfPresent(headers, HttpHeaders.UserAgent)
            }
        }.getOrElse { throwable ->
            logger.w(throwable) { "Failed to fetch manifest for track $trackId" }
            call.respond(HttpStatusCode.BadGateway, "Failed to fetch manifest")
            return
        }

        if (response.status.value >= 400) {
            logger.w { "Manifest request failed for track $trackId with status ${response.status.value}" }
            call.respond(HttpStatusCode.BadGateway, "Failed to fetch manifest")
            return
        }

        val manifestText = response.bodyAsText()
        val port = activePort() ?: 8080
        val serverUrl = "http://$HOST:$port"
        val rewrittenManifest = when (protocol) {
            StreamProtocol.HLS -> rewriteHlsManifest(manifestText, manifestUrl, serverUrl, trackId)
            StreamProtocol.DASH -> rewriteDashManifest(manifestText, manifestUrl, serverUrl, trackId)
            else -> manifestText
        }

        val contentType = when (protocol) {
            StreamProtocol.HLS -> ContentType("application", "vnd.apple.mpegurl")
            StreamProtocol.DASH -> ContentType("application", "dash+xml")
            else -> ContentType.Application.OctetStream
        }

        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val status: HttpStatusCode = HttpStatusCode.OK
            override val contentType: ContentType = contentType
            override val contentLength: Long = rewrittenManifest.encodeToByteArray().size.toLong()

            override suspend fun writeTo(channel: io.ktor.utils.io.ByteWriteChannel) {
                channel.writeFully(rewrittenManifest.encodeToByteArray())
            }
        })
    }

    private suspend fun proxySegment(
        call: ApplicationCall,
        segmentUrl: String,
        trackId: String
    ) {
        logger.d { "Proxying segment for track $trackId" }

        runCatching {
            httpClient.prepareRequest(segmentUrl) {
                method = HttpMethod.Get
                call.forwardRequestHeaderIfPresent(headers, HttpHeaders.Range)
                call.forwardRequestHeaderIfPresent(headers, HttpHeaders.Accept)
                call.forwardRequestHeaderIfPresent(headers, HttpHeaders.UserAgent)
            }.execute { response -> respondWithSegment(call, response, trackId) }
        }.onFailure { throwable ->
            if (throwable is CancellationException) throw throwable
            if (throwable.isClientDisconnect()) {
                logger.d { "Player closed the connection while streaming segment of track $trackId" }
                return
            }
            logger.w(throwable) { "Failed to fetch segment for track $trackId" }
            runCatching { call.respond(HttpStatusCode.BadGateway, "Failed to fetch segment") }
        }
    }

    private suspend fun respondWithSegment(call: ApplicationCall, response: HttpResponse, trackId: String) {

        if (response.status.value >= 400) {
            logger.w { "Segment request failed for track $trackId with status ${response.status.value}" }
            call.respond(HttpStatusCode.BadGateway, "Failed to fetch segment")
            return
        }

        val downstreamHeaders = upstreamHeadersToForward(response)
        val downstreamContentLength = downstreamHeaders[HttpHeaders.ContentLength]?.toLongOrNull()

        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val status: HttpStatusCode = response.status
            override val headers: Headers = downstreamHeaders
            override val contentLength: Long? = downstreamContentLength
            override val contentType: ContentType? =
                downstreamHeaders[HttpHeaders.ContentType]?.let(ContentType.Companion::parse)

            override suspend fun writeTo(channel: io.ktor.utils.io.ByteWriteChannel) {
                response.bodyAsChannel().copyTo(channel)
            }
        })
    }

    private suspend fun proxyStream(
        call: ApplicationCall,
        streamUrl: String,
        trackId: String,
        requestMethod: HttpMethod,
        allowCache: Boolean
    ) {
        logger.d { "Proxying stream for track $trackId using $requestMethod" }

        var currentUrl = streamUrl
        var attemptedRefresh = false
        // googlevideo throttles a request without a Range header to about playback speed, which
        // held every song back for seconds. The player sends none when starting from the top, so
        // ask for the whole file as a range and hand the player a plain 200 again.
        val addWholeRange = requestMethod == HttpMethod.Get && call.request.headers[HttpHeaders.Range] == null
        val upstreamStart = TimeSource.Monotonic.markNow()

        // `request()` buffers the whole body before returning, which stalls the player until
        // the full track is downloaded. `execute {}` hands over the response as soon as headers
        // arrive so the body can be streamed.
        while (true) {
            var retryUrl: String? = null
            val failure = runCatching {
                httpClient.prepareRequest(currentUrl) {
                    method = requestMethod
                    call.forwardRequestHeaderIfPresent(headers, HttpHeaders.Range)
                    call.forwardRequestHeaderIfPresent(headers, HttpHeaders.IfRange)
                    call.forwardRequestHeaderIfPresent(headers, HttpHeaders.Accept)
                    call.forwardRequestHeaderIfPresent(headers, HttpHeaders.UserAgent)
                    if (addWholeRange) headers.append(HttpHeaders.Range, WHOLE_RANGE)
                }.execute { response ->
                    logger.i {
                        "[Req] upstream headers for $trackId after ${upstreamStart.elapsedNow()}: " +
                            "status=${response.status} version=${response.version} " +
                            "headers=${response.headers.entries().joinToString { "${it.key}=${it.value}" }}"
                    }
                    if (response.status.value < 400) {
                        respondWithUpstream(call, response, trackId, requestMethod, addWholeRange, allowCache)
                        return@execute
                    }

                    logger.w { "Upstream stream request failed for track $trackId with status ${response.status.value}" }
                    streamingUrlRepository.invalidateCachedStreamUrl(trackId, currentUrl)

                    if (!attemptedRefresh) {
                        val refreshedUrl =
                            streamingUrlRepository.resolveStreamInfo(trackId, forceRefresh = true)?.url
                        if (!refreshedUrl.isNullOrBlank() && refreshedUrl != currentUrl) {
                            retryUrl = refreshedUrl
                            return@execute
                        }
                    }
                    respondWithUpstream(call, response, trackId, requestMethod, allowCache = allowCache)
                }
            }.exceptionOrNull()

            if (failure != null) {
                if (failure is CancellationException) throw failure
                if (failure.isClientDisconnect()) {
                    logger.i(failure) { "[Req] player closed the connection while streaming track $trackId" }
                    return
                }
                // Name the failure in the message: Android drops the stack trace of an
                // UnknownHostException, which left a DNS failure with no reason at all.
                logger.w(failure) {
                    "Failed to fetch upstream stream for track $trackId after ${upstreamStart.elapsedNow()}: " +
                        "${failure::class.simpleName}: ${failure.message?.take(200)}"
                }
                // Throws if the response already started streaming; nothing more to send then
                runCatching { call.respond(HttpStatusCode.BadGateway, "Failed to fetch upstream stream") }
                return
            }

            val next = retryUrl ?: return
            logger.d { "Retrying stream for track $trackId using refreshed URL" }
            currentUrl = next
            attemptedRefresh = true
        }
    }

    private suspend fun respondWithUpstream(
        call: ApplicationCall,
        upstream: HttpResponse,
        trackId: String,
        requestMethod: HttpMethod,
        addedWholeRange: Boolean = false,
        allowCache: Boolean = false
    ) {
        if (upstream.status.value >= 400 && isCachingEnabled()) {
            val cachedEntry = cacheManager.findCachedEntry(trackId)
            if (cachedEntry != null) {
                val (filePath, entry) = cachedEntry
                logger.d { "Falling back to cache for track $trackId after upstream error" }
                if (requestMethod == HttpMethod.Get) {
                    cacheManager.serveCacheGet(call, filePath, entry)
                } else {
                    cacheManager.serveCacheHead(call, entry)
                }
                return
            }
        }

        // The 206 answering our own range is, to a player that asked for none, an ordinary 200.
        val answersWholeRange = addedWholeRange && upstream.status == HttpStatusCode.PartialContent &&
            coversWholeResource(upstream.headers[HttpHeaders.ContentRange])
        val downstreamStatus = if (answersWholeRange) HttpStatusCode.OK else upstream.status
        val downstreamHeaders = upstreamHeadersToForward(upstream).let { headers ->
            if (!answersWholeRange) headers else HeadersBuilder().apply {
                appendAll(headers)
                remove(HttpHeaders.ContentRange)
            }.build()
        }
        val downstreamContentLength = downstreamHeaders[HttpHeaders.ContentLength]?.toLongOrNull()
        logger.v {
            "Proxy response headers for $trackId status=${upstream.status.value} " +
                    "contentType=${downstreamHeaders[HttpHeaders.ContentType]} " +
                    "contentLength=${downstreamHeaders[HttpHeaders.ContentLength]} " +
                    "contentRange=${downstreamHeaders[HttpHeaders.ContentRange]} " +
                    "acceptRanges=${downstreamHeaders[HttpHeaders.AcceptRanges]}"
        }

        if (requestMethod == HttpMethod.Head) {
            call.respond(object : OutgoingContent.NoContent() {
                override val status: HttpStatusCode = downstreamStatus
                override val headers: Headers = downstreamHeaders.passthroughForPlayer()
                override val contentLength: Long? = downstreamContentLength
            })
            return
        }

        // Only a full 200 response can be cached; 206 partial responses must not be stored as the whole track
        val doCache = allowCache && requestMethod == HttpMethod.Get && isCachingEnabled() &&
                downstreamStatus == HttpStatusCode.OK
        val cacheTrack = if (doCache) getTrackMetadata(trackId) else null
        val cacheFilename = if (cacheTrack != null) {
            cacheManager.resolveCacheFilename(cacheTrack, downstreamHeaders[HttpHeaders.ContentType])
        } else {
            null
        }
        val trackIdForCache = trackId
        val expectedLength = downstreamContentLength
        val cacheContentType = downstreamHeaders[HttpHeaders.ContentType] ?: "application/octet-stream"

        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val status: HttpStatusCode = downstreamStatus
            override val headers: Headers = downstreamHeaders.passthroughForPlayer()
            override val contentLength: Long? = downstreamContentLength
            override val contentType: ContentType? =
                downstreamHeaders[HttpHeaders.ContentType]?.let(ContentType.Companion::parse)

            override suspend fun writeTo(channel: io.ktor.utils.io.ByteWriteChannel) {
                val writeStart = TimeSource.Monotonic.markNow()
                logger.i { "[Req] start writing body for $trackId status=${upstream.status} cache=${cacheFilename != null} expected=$expectedLength" }
                try {
                    writeBody(channel)
                    logger.i { "[Req] finished writing body for $trackId in ${writeStart.elapsedNow()}" }
                } catch (e: Throwable) {
                    logger.i(e) { "[Req] body write for $trackId aborted after ${writeStart.elapsedNow()}: ${e::class.simpleName}: ${e.message}" }
                    throw e
                }
            }

            private suspend fun writeBody(channel: io.ktor.utils.io.ByteWriteChannel) {
                if (cacheFilename != null) {
                    val cacheDir = cacheManager.resolveCacheDir()
                    val cacheFile = cacheDir / cacheFilename.toPath()
                    fileSystem.createDirectories(cacheDir)
                    var completed = false
                    var totalBytes = 0L
                    try {
                        val source = upstream.bodyAsChannel()
                        val buffer = ByteArray(64 * 1024)
                        fileSystem.sink(cacheFile).buffer().use { sink ->
                            while (true) {
                                val read = source.readAvailable(buffer, 0, buffer.size)
                                if (read < 0) break
                                if (read == 0) continue
                                channel.writeFully(buffer, 0, read)
                                channel.flush()
                                sink.write(buffer, 0, read)
                                totalBytes += read
                            }
                        }
                        completed = expectedLength == null || expectedLength == totalBytes
                        if (completed) {
                            cacheManager.commitCacheEntry(
                                trackId = trackIdForCache,
                                filename = cacheFilename,
                                sizeBytes = totalBytes,
                                contentType = cacheContentType)
                            scope.launch {
                                cacheManager.evictIfNeeded()
                            }
                        }
                    } finally {
                        // Player disconnected or stream was incomplete: drop the partial file
                        if (!completed && fileSystem.exists(cacheFile)) {
                            fileSystem.delete(cacheFile)
                        }
                    }
                } else {
                    upstream.bodyAsChannel().copyTo(channel)
                }
            }
        })
    }

    private fun rewriteHlsManifest(
        manifest: String,
        baseUrl: String,
        serverUrl: String,
        trackId: String
    ): String {
        val manifestBase = baseUrl.substringBeforeLast('/')
        val lines = manifest.lines()
        val rewritten = lines.map { line ->
            when {
                line.isBlank() || line.startsWith("#") -> line
                line.startsWith("http") -> {
                    "${serverUrl}/segment/${trackId}?url=${encodeUrl(line)}"
                }
                else -> {
                    val absoluteUrl = if (line.startsWith("/")) {
                        val urlBase =
                            baseUrl.substringBefore("://") + "://" + baseUrl.substringAfter("://")
                                .substringBefore('/')
                        "$urlBase$line"
                    } else {
                        "$manifestBase/$line"
                    }
                    "${serverUrl}/segment/${trackId}?url=${encodeUrl(absoluteUrl)}"
                }
            }
        }
        return rewritten.joinToString("\n")
    }

    private fun rewriteDashManifest(
        manifest: String,
        baseUrl: String,
        serverUrl: String,
        trackId: String
    ): String {
        val manifestBase = baseUrl.substringBeforeLast('/')
        var rewritten = manifest

        val urlRegex = Regex("""(BaseURL|SegmentURL|Location)>([^<]+)""")
        rewritten = urlRegex.replace(rewritten) { match ->
            val tag = match.groupValues[1]
            val url = match.groupValues[2]
            val absoluteUrl = if (url.startsWith("http")) url else "$manifestBase/$url"
            "$tag>${serverUrl}/segment/${trackId}?url=${encodeUrl(absoluteUrl)}"
        }

        val srcRegex = Regex("""src="([^"]+)"""")
        rewritten = srcRegex.replace(rewritten) { match ->
            val url = match.groupValues[1]
            val absoluteUrl = if (url.startsWith("http")) url else "$manifestBase/$url"
            """src="${serverUrl}/segment/${trackId}?url=${encodeUrl(absoluteUrl)}"""
        }

        return rewritten
    }

    /**
     * True when the failure came from writing to the player's socket (seek, skip or stop closes
     * it mid-stream), as opposed to a failure fetching from upstream.
     */
    private fun Throwable.isClientDisconnect(): Boolean =
        generateSequence(this) { it.cause }.take(10).any {
            it is ChannelWriteException || it is ClosedWriteChannelException ||
                (it is IOException && it.message?.contains("Broken pipe", ignoreCase = true) == true)
        }

    private fun encodeUrl(url: String): String = url.encodeURLParameter()
    private fun decodeUrl(encoded: String): String = encoded.decodeURLQueryComponent()

    private fun ApplicationCall.forwardRequestHeaderIfPresent(
        builder: HeadersBuilder,
        headerName: String
    ) {
        request.headers.getAll(headerName)?.forEach { value ->
            builder.append(headerName, value)
        }
    }

    private fun upstreamHeadersToForward(upstream: HttpResponse): Headers {
        return HeadersBuilder().apply {
            upstream.headers.forEach { name, values ->
                values.forEach { value -> append(name, value) }
            }
        }.build()
    }

}

private const val WHOLE_RANGE = "bytes=0-"

/** True for `bytes 0-{n-1}/{n}`: the partial response is in fact the entire resource. */
internal fun coversWholeResource(contentRange: String?): Boolean {
    val match = contentRange?.let { Regex("""bytes 0-(\d+)/(\d+)""").matchEntire(it.trim()) } ?: return false
    val (last, size) = match.destructured
    return last.toLong() + 1 == size.toLong()
}

private val PLAYER_PASSTHROUGH_HEADERS = listOf(
    HttpHeaders.ContentRange,
    HttpHeaders.AcceptRanges,
    HttpHeaders.ETag,
    HttpHeaders.LastModified,
)

/**
 * The upstream headers a player actually needs. Copying them all makes Ktor emit duplicate
 * Content-Length/Content-Type (also set through the OutgoingContent) and forwards hop-by-hop
 * headers like `Connection: keep-alive`, which VLC rejects by closing the socket right after
 * the response headers. Content-Range and Accept-Ranges must stay: without them the player
 * treats a 206 as invalid and the stream as unseekable, and e.g. WebM can't be demuxed.
 */
internal fun Headers.passthroughForPlayer(): Headers {
    val upstream = this
    return HeadersBuilder().apply {
        PLAYER_PASSTHROUGH_HEADERS.forEach { name ->
            upstream.getAll(name)?.forEach { value -> append(name, value) }
        }
    }.build()
}
