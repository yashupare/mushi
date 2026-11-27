package com.mushi.app.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkMonitorTest {

    private class Fake : NetworkMonitor(initiallyOnline = true) {
        fun set(online: Boolean) = onNetworkChanged(online)
    }

    @Test
    fun `counts only a return from offline`() {
        val monitor = Fake()

        monitor.set(true)
        monitor.set(false)
        monitor.set(false)
        assertEquals(0, monitor.reconnects.value)

        monitor.set(true)
        monitor.set(true)
        assertEquals(1, monitor.reconnects.value)
        assertTrue(monitor.online.value)
    }

    @Test
    fun `tells a network failure from other errors`() {
        assertTrue(isNetworkError("java.net.UnknownHostException: Unable to resolve host \"x\""))
        assertTrue(isNetworkError("Request timeout has expired"))
        assertFalse(isNetworkError("JsonDecodingException: unexpected token"))
        assertFalse(isNetworkError(null))
    }
}
