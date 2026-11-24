package com.mushi.app.core.jam

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JamNamesTest {
    @Test
    fun sameSeedSameName() = runTest {
        assertEquals(JamNames.generate("device-a"), JamNames.generate("device-a"))
    }

    @Test
    fun namesAreTwoFriendlyWordsAndVaried() = runTest {
        val names = List(60) { JamNames.generate("device-$it") }
        assertTrue(names.all { Regex("^[A-Z][a-z]+ [A-Z][a-z]+$").matches(it) }, names.toString())
        assertTrue(names.all { it.length <= JamNames.MAX_LENGTH })
        assertTrue(names.toSet().size >= 50, "only ${names.toSet().size} distinct names")
    }

    @Test
    fun typedNamesAreCleaned() {
        assertEquals("Kingkor Roy", JamNames.normalize("  Kingkor \u0000 \t Roy \n"))
        assertEquals("", JamNames.normalize("   "))
    }
}
