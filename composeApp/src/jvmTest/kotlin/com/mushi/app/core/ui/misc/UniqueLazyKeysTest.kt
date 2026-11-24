package com.mushi.app.core.ui.misc

import kotlin.test.Test
import kotlin.test.assertEquals

class UniqueLazyKeysTest {
    @Test
    fun blankAndRepeatedIdsGetDistinctKeys() {
        val ids = listOf("a", "", "b", "a", "", "a")
        val keys = uniqueLazyKeys(ids) { it }

        assertEquals(ids.size, keys.toSet().size)
        // First occurrences keep their id so scroll position stays stable.
        assertEquals("a", keys[0])
        assertEquals("b", keys[2])
    }

    @Test
    fun suffixesCannotCollideWithRealIds() {
        val ids = listOf("a", "a", "a#1", "a1", "a_1")
        assertEquals(ids.size, uniqueLazyKeys(ids) { it }.toSet().size)
    }
}
