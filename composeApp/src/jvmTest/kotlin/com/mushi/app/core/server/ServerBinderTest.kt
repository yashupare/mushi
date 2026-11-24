package com.mushi.app.core.server

import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ServerBinderTest {
    private val host = "127.0.0.1"

    private fun health(port: Int) =
        URI("http://$host:$port/health").toURL().readText()

    @Test
    fun usesPreferredPortWhenFree() = runBlocking {
        val free = ServerSocket(0, 1, InetAddress.getByName(host)).use { it.localPort }
        val bound = startServerWithPortFallback(host, free) {
            routing { get("/health") { call.respondText("ok") } }
        }
        try {
            assertEquals(free, bound.port)
            assertTrue(!bound.usedFallback)
            assertEquals("ok", health(bound.port))
        } finally {
            bound.engine.stop(0, 500)
        }
    }

    @Test
    fun fallsBackToAnotherPortWhenPreferredIsTaken() = runBlocking {
        ServerSocket(0, 1, InetAddress.getByName(host)).use { blocker ->
            val taken = blocker.localPort
            val failures = mutableListOf<Int>()
            val bound = startServerWithPortFallback(host, taken, { port, _ -> failures += port }) {
                routing { get("/health") { call.respondText("ok") } }
            }
            try {
                assertNotEquals(taken, bound.port)
                assertTrue(bound.usedFallback)
                assertEquals(listOf(taken), failures)
                assertEquals("ok", health(bound.port))
            } finally {
                bound.engine.stop(0, 500)
            }
        }
    }
}
