package com.mushi.app.modules.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class StreamingFormatDefaultsTest {

    @Test
    fun `streaming defaults to opus in webm`() {
        val format = defaultStreamingMusicFormat()
        assertEquals("opus" to "webm", format.codec to format.container)
    }

    @Test
    fun `user settings default to the streaming default`() {
        assertEquals(defaultStreamingMusicFormat(), UserSettings().streamingMusicFormat)
    }
}
