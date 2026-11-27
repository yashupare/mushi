package com.mushi.app.modules.update

import kotlin.test.Test
import kotlin.test.assertEquals

class ReleaseNotesTest {

    @Test
    fun `shows headings and bullets without the markup`() {
        val notes = "**Install on Android:** download `x.apk` below.\r\n\r\n### What's new\r\n" +
            "- **Search:** keeps `every` letter. See [here](https://example.com).\r\n"

        assertEquals(
            listOf(
                NoteLine("What's new", heading = true),
                NoteLine("• Search: keeps every letter. See here.")),
            releaseNoteLines(notes))
    }
}
