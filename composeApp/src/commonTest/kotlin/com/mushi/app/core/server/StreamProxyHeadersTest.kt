package com.mushi.app.core.server

import io.ktor.http.HeadersBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StreamProxyHeadersTest {

    // Lowercase names and values as googlevideo sends them
    private val upstream = HeadersBuilder().apply {
        append("accept-ranges", "bytes")
        append("alt-svc", "h3=\":443\"")
        append("cache-control", "private, max-age=21298")
        append("connection", "keep-alive")
        append("content-length", "3523553")
        append("content-range", "bytes 0-3523552/3523553")
        append("content-type", "audio/webm")
        append("last-modified", "Tue, 18 Feb 2025 22:42:11 GMT")
    }.build()

    @Test
    fun `keeps the headers a player needs to seek`() {
        val forwarded = upstream.passthroughForPlayer()
        assertEquals("bytes 0-3523552/3523553", forwarded["Content-Range"])
        assertEquals("bytes", forwarded["Accept-Ranges"])
        assertEquals("Tue, 18 Feb 2025 22:42:11 GMT", forwarded["Last-Modified"])
    }

    @Test
    fun `drops headers that are duplicated or hop-by-hop`() {
        val forwarded = upstream.passthroughForPlayer()
        // Length and type are set on the OutgoingContent; forwarding them too emits them twice
        assertNull(forwarded["Content-Length"])
        assertNull(forwarded["Content-Type"])
        assertNull(forwarded["Connection"])
        assertNull(forwarded["Alt-Svc"])
        assertNull(forwarded["Cache-Control"])
    }
}
