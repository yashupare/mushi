package com.mushi.app.modules.home

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals

class SkyLightTest {

    @Test
    fun `the light passes a star that doesn't play on its own`() {
        assertEquals(listOf("a", "b", "c"), lightRoute(listOf("a", "b", "c"), fromId = "a", nextId = "c"))
        assertEquals(listOf("c"), lightRoute(listOf("a", "b", "c"), fromId = "c", nextId = null))
    }

    @Test
    fun `the light follows the line, not a straight cut`() {
        val line = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(10f, 30f))

        assertEquals(Offset(10f, 0f), pointAlong(line, 0.25f))
        assertEquals(Offset(10f, 10f), pointAlong(line, 0.5f))
        assertEquals(Offset(10f, 30f), pointAlong(line, 1f))
    }
}
