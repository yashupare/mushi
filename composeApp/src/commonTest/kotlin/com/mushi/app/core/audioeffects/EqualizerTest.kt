package com.mushi.app.core.audioeffects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EqualizerTest {

    @Test
    fun `knows a preset from its gains`() {
        assertEquals(EqualizerPreset.BassBoost, presetFor(listOf(6, 4, 0, 0, 0)))
        assertNull(presetFor(listOf(1, 0, 0, 0, 0)))
    }

    @Test
    fun `stored gains are made to fit the bands`() {
        assertEquals(listOf(8, -8, 0, 0, 0), listOf(20, -20).normalizedGains())
        assertEquals(5, listOf(1, 2, 3, 4, 5, 6).normalizedGains().size)
    }

    @Test
    fun `boosts are made room for`() {
        assertEquals(-6f, preampDb(listOf(6, 4, 0, -2, 0)))
        assertEquals(0f, preampDb(listOf(-3, -1, 0, 0, 0)))
    }
}
