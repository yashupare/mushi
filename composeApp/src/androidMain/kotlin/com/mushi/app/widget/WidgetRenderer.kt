package com.mushi.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** A star of the mini sky or of a constellation's shape, as fractions of its box. */
data class WidgetStar(val x: Float, val y: Float, val color: Int)

/** A saved constellation's row: its name, "3 stars" and its shape. */
data class WidgetPath(val name: String, val count: String, val stars: List<WidgetStar>)

internal val STAR_COLORS = intArrayOf(
    0xFFFF9A7A.toInt(), 0xFF5FE3C0.toInt(), 0xFFFF6FA8.toInt(),
    0xFF7FD0FF.toInt(), 0xFFFFD166.toInt(), 0xFFB49BFF.toInt())

private const val INK = 0xFF141119.toInt()
private const val CORNER = 24f

/**
 * Draws the widgets as pictures, so they can look like the app. Everything is in dp; the click
 * areas in the widget layouts are placed to match.
 */
internal class WidgetRenderer(context: Context) {
    private val density = min(context.resources.displayMetrics.density, 2.5f)

    private fun font(name: String, fallback: Int): Typeface = runCatching {
        Typeface.createFromAsset(assets, "composeResources/com.mushi.app.generated.resources/font/$name.ttf")
    }.getOrElse { Typeface.create(Typeface.DEFAULT, fallback) }

    private val assets = context.assets
    private val bold = font("plus_jakarta_sans_extrabold", Typeface.BOLD)
    private val medium = font("plus_jakarta_sans_semibold", Typeface.NORMAL)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun draw(width: Float, height: Float, content: Canvas.() -> Unit): Bitmap {
        val bitmap = Bitmap.createBitmap(
            (width * density).toInt().coerceAtLeast(1),
            (height * density).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            scale(this@WidgetRenderer.density, this@WidgetRenderer.density)
            clipPath(Path().apply {
                addRoundRect(RectF(0f, 0f, width, height), CORNER, CORNER, Path.Direction.CW)
            })
            content()
        }
        return bitmap
    }

    private fun Canvas.fill(color: Int, rect: RectF, radius: Float = 0f) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = color
        drawRoundRect(rect, radius, radius, paint)
    }

    private fun Canvas.gradient(width: Float, height: Float, vararg colors: Int) {
        paint.reset()
        paint.shader = LinearGradient(0f, 0f, width * 0.4f, height, colors, null, Shader.TileMode.CLAMP)
        drawRect(0f, 0f, width, height, paint)
        paint.shader = null
    }

    private fun Canvas.glow(x: Float, y: Float, radius: Float, color: Int, alpha: Int) {
        paint.reset()
        paint.isAntiAlias = true
        paint.shader = RadialGradient(x, y, radius, withAlpha(color, alpha), withAlpha(color, 0), Shader.TileMode.CLAMP)
        drawCircle(x, y, radius, paint)
        paint.shader = null
    }

    private fun Canvas.text(
        value: String, x: Float, baseline: Float, maxWidth: Float, size: Float,
        color: Int = Color.WHITE, face: Typeface = bold, centred: Boolean = false) {
        val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = face
            textSize = size
            this.color = color
        }
        val shown = TextUtils.ellipsize(value, text, maxWidth, TextUtils.TruncateAt.END).toString()
        drawText(shown, if (centred) x - text.measureText(shown) / 2 else x, baseline, text)
    }

    private fun Canvas.cover(art: Bitmap?, rect: RectF, radius: Float, color: Int) {
        save()
        clipPath(Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) })
        if (art != null) {
            paint.reset()
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            drawBitmap(art, null, rect, paint)
        } else {
            fill(color, rect)
        }
        restore()
    }

    private fun Canvas.sparkle(x: Float, y: Float, r: Float, color: Int, alpha: Int = 255) {
        val k = r * 0.18f
        val path = Path().apply {
            moveTo(x, y - r)
            quadTo(x + k, y - k, x + r, y)
            quadTo(x + k, y + k, x, y + r)
            quadTo(x - k, y + k, x - r, y)
            quadTo(x - k, y - k, x, y - r)
            close()
        }
        paint.reset()
        paint.isAntiAlias = true
        paint.color = withAlpha(color, alpha)
        drawPath(path, paint)
    }

    private fun Canvas.line(points: List<Pair<Float, Float>>, color: Int, width: Float) {
        if (points.size < 2) return
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = color
        points.zipWithNext { a, b -> drawLine(a.first, a.second, b.first, b.second, paint) }
    }

    private fun Canvas.playPause(cx: Float, cy: Float, size: Float, playing: Boolean, color: Int) {
        if (playing) {
            fill(color, RectF(cx - size * 0.38f, cy - size * 0.45f, cx - size * 0.1f, cy + size * 0.45f), 1.5f)
            fill(color, RectF(cx + size * 0.1f, cy - size * 0.45f, cx + size * 0.38f, cy + size * 0.45f), 1.5f)
        } else {
            triangle(cx - size * 0.3f, cy, size, color)
        }
    }

    private fun Canvas.triangle(left: Float, cy: Float, size: Float, color: Int) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = color
        drawPath(Path().apply {
            moveTo(left, cy - size * 0.45f)
            lineTo(left + size * 0.72f, cy)
            lineTo(left, cy + size * 0.45f)
            close()
        }, paint)
    }

    private fun Canvas.next(cx: Float, cy: Float, size: Float) {
        triangle(cx - size * 0.45f, cy, size * 0.9f, Color.WHITE)
        fill(Color.WHITE, RectF(cx + size * 0.25f, cy - size * 0.4f, cx + size * 0.4f, cy + size * 0.4f), 1f)
    }

    private fun Canvas.heart(cx: Float, cy: Float, size: Float, filled: Boolean) {
        val s = size / 2
        val path = Path().apply {
            moveTo(cx, cy + s * 0.85f)
            cubicTo(cx - s * 1.5f, cy - s * 0.05f, cx - s * 0.8f, cy - s * 1.2f, cx, cy - s * 0.4f)
            cubicTo(cx + s * 0.8f, cy - s * 1.2f, cx + s * 1.5f, cy - s * 0.05f, cx, cy + s * 0.85f)
            close()
        }
        paint.reset()
        paint.isAntiAlias = true
        paint.color = if (filled) 0xFFFF4D6D.toInt() else Color.WHITE
        paint.style = if (filled) Paint.Style.FILL else Paint.Style.STROKE
        paint.strokeWidth = 1.8f
        drawPath(path, paint)
    }

    fun nowPlaying(
        width: Float, height: Float, art: Bitmap?, color: Int, title: String, artist: String,
        playing: Boolean, liked: Boolean, progress: Float): Bitmap = draw(width, height) {
        gradient(width, height, INK, INK)
        glow(height * 0.4f, height * 0.4f, width * 0.75f, color, 190)
        val coverSize = min(height - 24f, width * 0.34f)
        val top = (height - coverSize) / 2
        // The record peeks out from behind the cover
        val discX = 12f + coverSize * 0.78f
        val discR = coverSize * 0.44f
        fill(0xFF0B0A0D.toInt(), RectF(discX - discR, height / 2 - discR, discX + discR, height / 2 + discR), discR)
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = 0x22FFFFFF
        for (ring in 3..8) drawCircle(discX, height / 2, discR * ring / 9, paint)
        cover(art, RectF(12f, top, 12f + coverSize, top + coverSize), 16f, color)

        val textX = 12f + coverSize * 1.25f + 10f
        val textWidth = width - textX - 12f
        val titleY = maxOf(30f, top + 22f)
        text(title, textX, titleY, textWidth, 15f)
        text(artist, textX, titleY + 16f, textWidth, 11f, 0xB8FFFFFF.toInt(), medium)

        // A still waveform showing how far the song is
        val waveY = titleY + 36f
        if (waveY + 10f < height - 56f) {
            val bars = 24
            val step = textWidth / bars
            for (i in 0 until bars) {
                val h = 5f + abs(sin(i * 1.7f) * cos(i * 0.45f)) * 14f
                val x = textX + i * step
                fill(
                    if (i.toFloat() / bars <= progress) color else 0x3DFFFFFF,
                    RectF(x, waveY - h / 2, x + step * 0.55f, waveY + h / 2), 1.5f)
            }
        }

        // Three 42dp targets, 10dp from the right and bottom edges
        val cy = height - 10f - 21f
        val first = width - 10f - 42f * 3 + 21f
        heart(first, cy, 16f, liked)
        fill(Color.WHITE, RectF(first + 42f - 19f, cy - 19f, first + 42f + 19f, cy + 19f), 13f)
        playPause(first + 42f, cy, 15f, playing, INK)
        next(first + 84f, cy, 16f)
    }

    fun slim(
        width: Float, height: Float, art: Bitmap?, color: Int, title: String, artist: String,
        playing: Boolean): Bitmap = draw(width, height) {
        gradient(width, height, INK, INK)
        glow(0f, height / 2, width * 0.6f, color, 170)
        val size = min(height - 12f, 52f)
        val top = (height - size) / 2
        cover(art, RectF(8f, top, 8f + size, top + size), size / 2, color)
        val textX = size + 18f
        val textWidth = width - textX - 100f
        text(title, textX, height / 2 - 2f, textWidth, 14f)
        text(artist, textX, height / 2 + 14f, textWidth, 11f, 0xB8FFFFFF.toInt(), medium)
        // Two 44dp targets, 8dp from the right edge
        val cy = height / 2
        val playX = width - 8f - 66f
        fill(Color.WHITE, RectF(playX - 18f, cy - 18f, playX + 18f, cy + 18f), 13f)
        playPause(playX, cy, 14f, playing, INK)
        next(width - 8f - 22f, cy, 16f)
    }

    fun miniSky(
        width: Float, height: Float, stars: List<WidgetStar>, kept: Boolean,
        headline: String, caption: String): Bitmap = draw(width, height) {
        gradient(width, height, 0xFF070B24.toInt(), 0xFF1D1C4C.toInt())
        glow(width * 0.2f, height * 0.18f, width * 0.6f, 0xFF7A52C0.toInt(), 110)
        for (i in 0 until 30) {
            fill(
                withAlpha(Color.WHITE, 60 + (i % 5) * 25),
                RectF((i * 0.377f % 1f) * width, (i * 0.533f % 1f) * height,
                    (i * 0.377f % 1f) * width + 1.2f, (i * 0.533f % 1f) * height + 1.2f), 1f)
        }
        val points = stars.map { (it.x * width) to (10f + it.y * (height - 70f)) }
        if (kept) line(points.take(5), 0x66FFFFFF, 0.8f)
        stars.forEachIndexed { i, star ->
            val (x, y) = points[i]
            if (kept) glow(x, y, 14f, star.color, 120)
            sparkle(x, y, if (kept) 7f else 5f, star.color, if (kept) 255 else 110)
        }
        if (kept) {
            text(headline, 14f, height - 26f, width - 70f, 26f)
            text(caption, 14f, height - 12f, width - 70f, 11f, 0xC0FFFFFF.toInt(), medium)
            // The latest-moment button: a 44dp target, 8dp from the right and bottom
            val c = 8f + 22f
            fill(Color.WHITE, RectF(width - c - 18f, height - c - 18f, width - c + 18f, height - c + 18f), 13f)
            playPause(width - c, height - c, 14f, false, INK)
        } else {
            text(headline, 14f, height - 28f, width - 28f, 12f)
            text(caption, 14f, height - 12f, width - 28f, 12f)
        }
    }

    fun resume(
        width: Float, height: Float, art: Bitmap?, color: Int, label: String, title: String,
        timeLeft: String, progress: Float): Bitmap = draw(width, height) {
        gradient(width, height, INK, INK)
        glow(width, 0f, width, color, 210)
        val size = min(width, height) * 0.4f
        val ring = RectF(14f, 14f, 14f + size, 14f + size)
        cover(art, RectF(ring.left + 4f, ring.top + 4f, ring.right - 4f, ring.bottom - 4f), size / 2, color)
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = 0x30FFFFFF
        drawArc(ring, 0f, 360f, false, paint)
        paint.color = Color.WHITE
        drawArc(ring, -90f, 360f * progress, false, paint)
        fill(Color.WHITE, RectF(width - 50f, 20f, width - 14f, 56f), 13f)
        playPause(width - 32f, 38f, 14f, false, INK)
        text(label, 14f, height - 44f, width - 28f, 11f, 0xC0FFFFFF.toInt(), medium)
        text(title, 14f, height - 27f, width - 28f, 14f)
        text(timeLeft, 14f, height - 12f, width - 28f, 11f, 0xC0FFFFFF.toInt(), medium)
    }

    /** Three rows of equal height inside 6dp of padding, matching the three click areas. */
    fun constellations(width: Float, height: Float, paths: List<WidgetPath>): Bitmap = draw(width, height) {
        gradient(width, height, 0xFF141A42.toInt(), 0xFF0C0B15.toInt())
        glow(width * 0.9f, 0f, width * 0.5f, 0xFF5A3F9C.toInt(), 110)
        val row = (height - 12f) / 3
        paths.take(3).forEachIndexed { index, path ->
            val top = 6f + index * row
            val cy = top + row / 2
            val boxW = 54f
            val boxH = row * 0.62f
            val points = path.stars.map { (14f + it.x * boxW) to (cy - boxH / 2 + it.y * boxH) }
            line(points, 0x99FFFFFF.toInt(), 1f)
            path.stars.forEachIndexed { i, star -> sparkle(points[i].first, points[i].second, 5f, star.color) }
            text(path.name, 82f, cy - 1f, width - 82f - 56f, 13f)
            text(path.count, 82f, cy + 13f, width - 82f - 56f, 10.5f, 0x99FFFFFF.toInt(), medium)
            val size = min(row - 6f, 32f)
            fill(0x1FFFFFFF, RectF(width - 14f - size, cy - size / 2, width - 14f, cy + size / 2), 11f)
            playPause(width - 14f - size / 2, cy, 11f, false, Color.WHITE)
        }
    }

    /** A plain message on the night colours, for a widget with nothing to show yet. */
    fun message(width: Float, height: Float, first: String, second: String = ""): Bitmap =
        draw(width, height) {
            gradient(width, height, 0xFF141A42.toInt(), 0xFF0C0B15.toInt())
            val y = height / 2 + if (second.isEmpty()) 4f else -4f
            text(first, width / 2, y, width - 24f, 13f, centred = true)
            if (second.isNotEmpty()) {
                text(second, width / 2, y + 18f, width - 24f, 11f, 0xB8FFFFFF.toInt(), medium, centred = true)
            }
        }

    companion object {
        fun withAlpha(color: Int, alpha: Int): Int =
            Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

        /** A vivid version of a cover's average colour. */
        fun tintOf(art: Bitmap?): Int {
            if (art == null) return 0xFFFF4D6D.toInt()
            val pixel = Bitmap.createScaledBitmap(art, 1, 1, true).getPixel(0, 0)
            val hsv = FloatArray(3)
            Color.colorToHSV(pixel, hsv)
            hsv[1] = (hsv[1] * 1.4f).coerceIn(0.35f, 0.85f)
            hsv[2] = hsv[2].coerceIn(0.65f, 0.95f)
            return Color.HSVToColor(hsv)
        }
    }
}
