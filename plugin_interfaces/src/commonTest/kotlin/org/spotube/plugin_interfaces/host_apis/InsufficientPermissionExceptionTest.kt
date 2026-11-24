package org.spotube.plugin_interfaces.host_apis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class InsufficientPermissionExceptionTest {
    @Test
    fun recoversFromBridgedStackTraceString() {
        val original = InsufficientPermissionException(
            PluginPermission.WEBVIEW_COOKIES, "accounts.example.com", "not declared")
        // Zipline hands the plugin a ZiplineException whose message is the host's stack trace.
        val bridged = Exception(original.stackTraceToString())
        val recovered = assertNotNull(bridged.asInsufficientPermission())
        assertEquals(PluginPermission.WEBVIEW_COOKIES, recovered.permission)
        assertEquals("accounts.example.com", recovered.resource)
    }

    @Test
    fun returnsSameInstanceAndFollowsCauses() {
        val original = InsufficientPermissionException(PluginPermission.WEBVIEW_COOKIES, "x.com")
        assertSame(original, original.asInsufficientPermission())
        assertSame(original, RuntimeException("wrapped", original).asInsufficientPermission())
    }

    @Test
    fun resourceCannotBreakTheFormat() {
        val e = InsufficientPermissionException(PluginPermission.WEBVIEW_COOKIES, "evil'\ninjected")
        assertEquals("evilinjected", Exception(e.message).asInsufficientPermission()?.resource)
    }

    @Test
    fun ignoresUnrelatedErrors() {
        assertNull(IllegalStateException("boom").asInsufficientPermission())
        assertNull(Exception("Insufficient permission 'unknown.perm' for 'x'").asInsufficientPermission())
    }
}
