package com.mushi.app.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class SwipeToQueueTest {

    @Test
    fun `the spring overshooting past zero never gives a negative width`() {
        assertEquals(0, pullPixels(-4.2f))
        assertEquals(0, pullPixels(0f))
        assertEquals(37, pullPixels(36.7f))
    }
}
