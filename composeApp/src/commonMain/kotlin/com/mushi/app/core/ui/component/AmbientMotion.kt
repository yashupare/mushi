package com.mushi.app.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.LocalReduceMotion
import kotlin.math.PI
import kotlin.math.sin

/**
 * Soft rings that pulse out from the element and fade, like the sound waves in the logo. For
 * empty and error states, which otherwise sit completely still. Nothing is drawn under reduced
 * motion. The rings are read while drawing, so the loop never recomposes.
 */
fun Modifier.pulseRings(color: Color, rings: Int = 2, periodMillis: Int = 2600): Modifier = composed {
    if (LocalReduceMotion.current) return@composed this
    val progress = rememberInfiniteTransition(label = "pulse_rings").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing)),
        label = "pulse_rings_progress")
    this.drawBehind {
        val base = size.minDimension / 2f
        for (i in 0 until rings) {
            val p = (progress.value + i.toFloat() / rings) % 1f
            drawCircle(
                color = color.copy(alpha = color.alpha * (1f - p) * 0.45f),
                radius = base * (1f + 0.7f * p),
                style = Stroke(width = 2.dp.toPx()))
        }
    }
}

/** A slow up-and-down float of [amplitude], for an illustration that should feel alive. */
fun Modifier.gentleFloat(amplitude: Dp = 3.dp, periodMillis: Int = 3400): Modifier = composed {
    if (LocalReduceMotion.current) return@composed this
    val phase = rememberInfiniteTransition(label = "gentle_float").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "gentle_float_phase")
    this.graphicsLayer {
        translationY = sin(phase.value * 2f * PI.toFloat()) * amplitude.toPx()
    }
}
