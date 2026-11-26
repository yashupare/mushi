package com.mushi.app.core.newpipe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LeanStreamLookupTest {
    private val next = "https://www.youtube.com/youtubei/v1/next?prettyPrint=false"
    private val player = "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"

    @Test
    fun `stands in for the next page only during a lookup`() {
        assertNull(LeanStreamLookup.standInFor(next))
        LeanStreamLookup.run<Unit>(isUsable = { true }) {
            assertNotNull(LeanStreamLookup.standInFor(next))
            assertNull(LeanStreamLookup.standInFor(player))
        }
        assertNull(LeanStreamLookup.standInFor(next))
    }

    @Test
    fun `repeats in full when the lean lookup throws`() {
        val skipped = mutableListOf<Boolean>()
        val result = LeanStreamLookup.run(isUsable = { true }) {
            val lean = LeanStreamLookup.standInFor(next) != null
            skipped += lean
            if (lean) error("extractor needed the page")
            "full"
        }
        assertEquals("full", result)
        assertEquals(listOf(true, false), skipped)
    }

    @Test
    fun `repeats in full when the lean answer is unusable`() {
        var calls = 0
        val result = LeanStreamLookup.run<Int>(isUsable = { it > 1 }) { ++calls }
        assertEquals(2, result)
    }
}
