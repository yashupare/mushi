package com.mushi.app.core.ui.theming

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Side length of the thumbnail the tint is read from; colour only needs a rough sample. */
private const val TINT_SAMPLE_SIZE = 48

/**
 * Reads a [TINT_SAMPLE_SIZE] x [TINT_SAMPLE_SIZE] thumbnail of the artwork in [model] as ARGB
 * pixels, or `null` when it can't be loaded.
 */
private suspend fun sampleArtworkPixels(context: coil3.PlatformContext, model: Any): IntArray? {
    val request = ImageRequest.Builder(context)
        .data(model)
        .size(TINT_SAMPLE_SIZE, TINT_SAMPLE_SIZE)
        // Hardware bitmaps can't be drawn into the software canvas used to read pixels.
        .softwareBitmapsOnly()
        .build()
    val result = SingletonImageLoader.get(context).execute(request) as? SuccessResult ?: return null
    val painter = result.image.asPainter(context)
    return withContext(Dispatchers.Default) {
        val side = TINT_SAMPLE_SIZE
        val bitmap = ImageBitmap(side, side)
        val size = Size(side.toFloat(), side.toFloat())
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), size) {
            with(painter) { draw(size) }
        }
        IntArray(side * side).also { bitmap.readPixels(it, 0, 0, side, side) }
    }
}

/**
 * A representative colour of the artwork in [model] (anything Coil can load), or `null`
 * while it loads or when it can't be read. The previous colour stays until the next one is
 * ready, so a track change fades from one tint to the next instead of flashing.
 */
@Composable
fun rememberArtworkTint(model: Any?): Color? {
    val context = LocalPlatformContext.current
    var tint by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(model) {
        if (model == null) {
            tint = null
            return@LaunchedEffect
        }
        tint = runCatching {
            sampleArtworkPixels(context, model)?.let(::dominantColor)
        }.getOrNull() ?: tint
    }
    return tint
}

/**
 * Three colours from different parts of the artwork (top left, top right, bottom), each the
 * dominant colour of that region with the glare cap applied, or `null` while loading. Used for
 * the moving colour background on the player. Like [rememberArtworkTint], the previous colours
 * stay until the next cover is read.
 */
@Composable
fun rememberArtworkPalette(model: Any?): List<Color>? {
    val context = LocalPlatformContext.current
    var palette by remember { mutableStateOf<List<Color>?>(null) }

    LaunchedEffect(model) {
        if (model == null) {
            palette = null
            return@LaunchedEffect
        }
        palette = runCatching {
            val pixels = sampleArtworkPixels(context, model) ?: return@runCatching null
            val whole = dominantColor(pixels) ?: return@runCatching null
            val third = TINT_SAMPLE_SIZE / 3
            listOf(
                region(pixels, 0, 0, 2 * third, 2 * third),
                region(pixels, third, 0, TINT_SAMPLE_SIZE, 2 * third),
                region(pixels, third / 2, third, TINT_SAMPLE_SIZE - third / 2, TINT_SAMPLE_SIZE),
            ).map { (it ?: whole).forBackdrop() }
        }.getOrNull() ?: palette
    }
    return palette
}

/** [dominantColor] of the pixels in columns [x0, x1) and rows [y0, y1) of the sample. */
private fun region(pixels: IntArray, x0: Int, y0: Int, x1: Int, y1: Int): Color? {
    val out = IntArray((x1 - x0) * (y1 - y0))
    var i = 0
    for (y in y0 until y1) for (x in x0 until x1) out[i++] = pixels[y * TINT_SAMPLE_SIZE + x]
    return dominantColor(out)
}

/**
 * Weighted average of [argbPixels] that favours saturated, mid-tone pixels, so a mostly white
 * or black cover with one coloured shape still gives that colour rather than grey. Returns
 * `null` for a fully transparent or empty image.
 */
internal fun dominantColor(argbPixels: IntArray): Color? {
    var red = 0.0
    var green = 0.0
    var blue = 0.0
    var total = 0.0
    for (argb in argbPixels) {
        val alpha = (argb ushr 24) and 0xFF
        if (alpha < 128) continue
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val lightness = (max + min) / 2.0
        val saturation = if (max == 0.0) 0.0 else (max - min) / max
        // Almost-black and almost-white pixels carry little colour; weight them down.
        val tonal = 1.0 - kotlin.math.abs(lightness - 0.5) * 1.6
        val weight = 0.05 + saturation * saturation * tonal.coerceAtLeast(0.0)
        red += r * weight
        green += g * weight
        blue += b * weight
        total += weight
    }
    if (total <= 0.0) return null
    return capGlare(
        red = red / total,
        green = green / total,
        blue = blue / total)
}

/** Brightest a tint may be, as HSL lightness; brighter covers are toned down to this. */
private const val MAX_TINT_LIGHTNESS = 0.55
/** Most saturated a tint may be; neon covers are calmed to this. */
private const val MAX_TINT_SATURATION = 0.80

/**
 * Caps a tint's lightness and saturation so a white or neon cover can't flood a dark screen with
 * glare (the complaint Apple Music's adaptive colours drew in dark mode). Hue is kept, so the
 * tint still matches the cover.
 */
internal fun capGlare(red: Double, green: Double, blue: Double): Color {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val lightness = (max + min) / 2.0
    val delta = max - min
    val saturation = if (delta == 0.0) 0.0 else delta / (1.0 - kotlin.math.abs(2.0 * lightness - 1.0))
    val hue = when {
        delta == 0.0 -> 0.0
        max == red -> 60.0 * (((green - blue) / delta).mod(6.0))
        max == green -> 60.0 * ((blue - red) / delta + 2.0)
        else -> 60.0 * ((red - green) / delta + 4.0)
    }
    return Color.hsl(
        hue = hue.toFloat().coerceIn(0f, 359.99f),
        saturation = saturation.coerceIn(0.0, MAX_TINT_SATURATION).toFloat(),
        lightness = lightness.coerceIn(0.0, MAX_TINT_LIGHTNESS).toFloat())
}

/** Darkest a backdrop colour may be, as HSL lightness, so dark covers still show colour. */
private const val MIN_BACKDROP_LIGHTNESS = 0.32f
private const val MAX_BACKDROP_LIGHTNESS = 0.55f

/**
 * A palette colour made to read as colour behind the player: lifted to at least
 * [MIN_BACKDROP_LIGHTNESS] (a dark cover would otherwise give near-black blobs) and slightly more
 * saturated, still within the glare cap. Near-greys stay grey rather than gaining a false hue.
 */
private fun Color.forBackdrop(): Color {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val lightness = (max + min) / 2f
    val delta = max - min
    val saturation = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * lightness - 1f))
    val hue = when {
        delta == 0f -> 0f
        max == red -> 60f * (((green - blue) / delta).mod(6f))
        max == green -> 60f * ((blue - red) / delta + 2f)
        else -> 60f * ((red - green) / delta + 4f)
    }
    val boosted = if (saturation < 0.08f) saturation else (saturation * 1.2f).coerceAtMost(MAX_TINT_SATURATION.toFloat())
    return Color.hsl(
        hue = hue.coerceIn(0f, 359.99f),
        saturation = boosted.coerceIn(0f, 1f),
        lightness = lightness.coerceIn(MIN_BACKDROP_LIGHTNESS, MAX_BACKDROP_LIGHTNESS))
}
