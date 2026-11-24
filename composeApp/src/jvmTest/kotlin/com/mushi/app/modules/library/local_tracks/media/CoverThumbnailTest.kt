package com.mushi.app.modules.library.local_tracks.media

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoverThumbnailTest {
    private fun png(width: Int, height: Int): ByteArray = Surface.makeRasterN32Premul(width, height).use { surface ->
        // Noise-free but non-uniform so the PNG isn't trivially small.
        for (x in 0 until width step 7) {
            surface.canvas.drawRect(Rect.makeXYWH(x.toFloat(), 0f, 3f, height.toFloat()), Paint().apply { color = (0xFF000000.toInt() or (x * 2654435761L).toInt()) })
        }
        surface.makeImageSnapshot().use { it.encodeToData(EncodedImageFormat.PNG)!!.bytes }
    }

    @Test
    fun largeCoverIsShrunkToASmallJpeg() {
        val original = png(1600, 1200)
        val thumbnail = assertNotNull(coverThumbnail(original))

        Image.makeFromEncoded(thumbnail).use { image ->
            assertEquals(160, minOf(image.width, image.height))
            assertEquals(213, image.width)
        }
        assertTrue(thumbnail.size < 32 * 1024, "thumbnail is ${thumbnail.size} bytes")
    }

    @Test
    fun unreadableCoverIsDropped() {
        assertNull(coverThumbnail(byteArrayOf(1, 2, 3, 4)))
    }
}
