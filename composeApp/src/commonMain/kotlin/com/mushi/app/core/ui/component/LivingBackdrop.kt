package com.mushi.app.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.mushi.app.core.ui.LocalReduceMotion
import kotlin.math.cos
import kotlin.math.sin

/** How each colour blob drifts: its resting spot (fractions of width / tint height) and speeds. */
private class Blob(val x: Float, val y: Float, val dx: Float, val dy: Float, val speed: Float, val phase: Float)

private val Blobs = listOf(
    Blob(x = 0.20f, y = 0.18f, dx = 0.16f, dy = 0.10f, speed = 0.21f, phase = 0.0f),
    Blob(x = 0.85f, y = 0.30f, dx = 0.14f, dy = 0.12f, speed = 0.17f, phase = 2.1f),
    Blob(x = 0.45f, y = 0.62f, dx = 0.20f, dy = 0.08f, speed = 0.13f, phase = 4.2f),
)

/**
 * The player's moving colour background: three soft blobs in the cover's own colours that drift
 * slowly while music plays, over [base], fading into [base] towards the bottom so the controls
 * stay legible. [strength] is how far the blobs lean from [base] towards the cover colours.
 *
 * Plain radial gradients, so it runs the same on every platform and Android version. It stops
 * moving when paused and under reduced motion; colours cross-fade when the track changes.
 */
fun Modifier.livingBackdrop(
    colors: List<Color>?,
    base: Color,
    strength: Float,
    time: State<Float>,
): Modifier = this.drawBehind {
    drawRect(base)
    val palette = colors ?: return@drawBehind
    val t = time.value
    val tintHeight = size.height * 0.7f
    val radius = size.width * 0.75f
    palette.take(Blobs.size).forEachIndexed { i, color ->
        val blob = Blobs[i]
        val center = Offset(
            x = size.width * (blob.x + blob.dx * sin(t * blob.speed + blob.phase)),
            y = tintHeight * (blob.y + blob.dy * cos(t * blob.speed * 0.8f + blob.phase)))
        val tint = lerp(base, color, strength)
        drawCircle(
            brush = Brush.radialGradient(
                0f to tint.copy(alpha = 0.95f),
                0.55f to tint.copy(alpha = 0.45f),
                1f to tint.copy(alpha = 0f),
                center = center,
                radius = radius),
            radius = radius,
            center = center)
    }
    // Settle into the page colour lower down, where the controls and progress bar sit.
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.45f to Color.Transparent,
            0.9f to base,
            1f to base))
}

/**
 * Seconds of "living" time for [livingBackdrop]: advances with the frame clock only while
 * [moving] (music playing and reduced motion off), so pausing freezes the background in place.
 */
@Composable
fun rememberLivingTime(moving: Boolean): State<Float> {
    val reduceMotion = LocalReduceMotion.current
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(moving, reduceMotion) {
        if (!moving || reduceMotion) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) time.floatValue += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }
    return time
}

/** Cross-fades each palette colour when the cover changes, so a new track blends in. */
@Composable
fun animatedPalette(colors: List<Color>?): List<Color>? {
    if (colors == null) return null
    return colors.mapIndexed { i, color ->
        val animated by animateColorAsState(color, tween(900), label = "palette_$i")
        animated
    }
}

/**
 * A gentle breathing scale (1.0 to 1.02 and back, about every 5 s) for the cover while music
 * plays; 1 when [active] is false or reduced motion is on. Read it in a graphics layer so the
 * breathing only redraws, never recomposes.
 */
@Composable
fun rememberBreathing(active: Boolean): State<Float> {
    val reduceMotion = LocalReduceMotion.current
    if (!active || reduceMotion) return remember { mutableFloatStateOf(1f) }
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "cover_breath")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(2600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse),
        label = "cover_breath")
}
