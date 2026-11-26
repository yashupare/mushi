package com.mushi.app.modules.shell

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerMotionTest {

    @Test
    fun `cover slides forwards for later tracks and back for earlier ones`() {
        val slide = CoverSlideDirection()
        assertEquals(1, slide.onIndex(0, 5))
        assertEquals(1, slide.onIndex(1, 5))
        assertEquals(-1, slide.onIndex(0, 5))
    }

    @Test
    fun `wrapping from the last track to the first slides forwards`() {
        val slide = CoverSlideDirection()
        slide.onIndex(4, 5)
        assertEquals(1, slide.onIndex(0, 5))
    }

    @Test
    fun `recomposing on the same track keeps the direction`() {
        val slide = CoverSlideDirection()
        slide.onIndex(3, 5)
        slide.onIndex(2, 5)
        assertEquals(-1, slide.onIndex(2, 5))
        assertEquals(-1, slide.onIndex(-1, 0))
    }

    @Test
    fun `touches map onto the inset track`() {
        assertEquals(0f, sliderFraction(x = 0f, width = 116f, inset = 8f))
        assertEquals(0.5f, sliderFraction(x = 58f, width = 116f, inset = 8f))
        assertEquals(1f, sliderFraction(x = 200f, width = 116f, inset = 8f))
        assertEquals(0f, sliderFraction(x = 5f, width = 10f, inset = 8f))
    }
}
