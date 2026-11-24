package com.mushi.app.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mushi.app.PlatformType
import com.mushi.app.getPlatform

/** The launch colour: the same near-black as the Android splash and the iOS launch screen. */
private val LaunchBackground = Color(0xFF0A0A0C)

/** The logo's rose gradient and wave colour (brand colours, the same on every palette). */
private val RoseGradient = listOf(Color(0xFFFF8A5B), Color(0xFFFF4D6D), Color(0xFFD6336C))
private val WaveColor = Color(0xFFFF4D6D)

/** Logo A geometry, in the 100-unit grid shared with the icon generator and Android vectors. */
private const val MARK_CENTER_X = 50f
private const val MARK_CENTER_Y = 44.65f
private const val MARK_HEIGHT = 62.7f
private const val DRAW_MS = 800f

/** Played at most once per process, so returning to the app or recomposing never replays it. */
private object StartupIntroState {
    var played = false
}

/**
 * The launch animation on platforms without an animated system splash.
 *
 * Android is skipped: its SplashScreen API plays the same animation (MainActivity). On iOS the
 * static launch screen already shows the finished logo, so the intro starts from that frame and
 * goes straight to the hand-off. On desktop the logo draws in first.
 */
@Composable
fun StartupIntro() {
    val platform = remember { getPlatform().type }
    if (platform == PlatformType.Android) return
    // Reduced motion: no intro at all; the app appears straight away.
    val reduceMotion = com.mushi.app.core.ui.LocalReduceMotion.current
    var visible by remember { mutableStateOf(!StartupIntroState.played && !reduceMotion) }
    if (!visible) return
    LaunchIntro(
        drawIn = platform != PlatformType.IOS,
        onFinished = {
            StartupIntroState.played = true
            visible = false
        })
}

@Composable
private fun LaunchIntro(drawIn: Boolean, onFinished: () -> Unit) {
    val draw = remember { Animatable(if (drawIn) 0f else 1f) }
    val exit = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (drawIn) {
            draw.animateTo(1f, tween(DRAW_MS.toInt(), easing = LinearEasing))
        } else {
            kotlinx.coroutines.delay(150)
        }
        // Hand-off: the logo grows and fades while the background fades a little later.
        exit.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        onFinished()
    }

    val e = exit.value
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 1f - ((e * 450f - 100f) / 350f).coerceIn(0f, 1f) }
            .background(LaunchBackground)
            // Swallows touches so nothing underneath reacts while the intro is on screen.
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } },
        contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = 1f + 0.6f * e
                    scaleY = 1f + 0.6f * e
                    alpha = 1f - e
                }) {
            drawMushiMark(draw.value)
        }
    }
}

/**
 * Draws logo A at [progress] (0 = nothing, 1 = finished) through the launch timeline:
 * arches draw in (0-450 ms, the second 90 ms later), the dot pops (320-700 ms), then the
 * small and large waves ripple out (480-760 ms, 580-800 ms).
 */
internal fun DrawScope.drawMushiMark(progress: Float) {
    val ms = progress * DRAW_MS
    fun phase(start: Float, duration: Float) = ((ms - start) / duration).coerceIn(0f, 1f)

    // The mark fills 90% of the canvas height, the same as the iOS launch image.
    val unit = 0.9f * size.height / MARK_HEIGHT
    val origin = Offset(size.width / 2f - MARK_CENTER_X * unit, size.height / 2f - MARK_CENTER_Y * unit)
    fun p(x: Float, y: Float) = Offset(origin.x + x * unit, origin.y + y * unit)
    fun arcRect(cx: Float, cy: Float, r: Float) = Rect(p(cx - r, cy - r), p(cx + r, cy + r))

    val brush = Brush.linearGradient(
        colorStops = arrayOf(0f to RoseGradient[0], 0.55f to RoseGradient[1], 1f to RoseGradient[2]),
        start = p(25f, 13f),
        end = p(75f, 76f))
    val archStroke = Stroke(width = 8f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val archA = Path().apply {
        moveTo(p(29f, 72f).x, p(29f, 72f).y)
        lineTo(p(29f, 52f).x, p(29f, 52f).y)
        arcTo(arcRect(39.5f, 52f, 10.5f), 180f, 180f, false)
        lineTo(p(50f, 72f).x, p(50f, 72f).y)
    }
    val archB = Path().apply {
        moveTo(p(50f, 52f).x, p(50f, 52f).y)
        arcTo(arcRect(60.5f, 52f, 10.5f), 180f, 180f, false)
        lineTo(p(71f, 72f).x, p(71f, 72f).y)
    }
    drawPath(trimmed(archA, easeOut(phase(0f, 450f))), brush, style = archStroke)
    drawPath(trimmed(archB, easeOut(phase(90f, 450f))), brush, style = archStroke)

    val dotScale = backOut(phase(320f, 380f))
    if (dotScale > 0f) drawCircle(brush, radius = 5f * unit * dotScale, center = p(50f, 29f))

    val waveStroke = Stroke(width = 3f * unit, cap = StrokeCap.Round)
    fun wave(centerY: Float, radius: Float, startAngle: Float, sweep: Float, t: Float) {
        if (t <= 0f) return
        val scale = 0.4f + 0.6f * easeOut(t)
        val pivot = p(50f, 29f)
        withTransform({ scale(scale, scale, pivot) }) {
            drawArc(
                color = WaveColor.copy(alpha = 0.55f * t),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = p(50f - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(2 * radius * unit, 2 * radius * unit),
                style = waveStroke)
        }
    }
    wave(centerY = 35.2f, radius = 15f, startAngle = 222.8f, sweep = 94.4f, t = phase(480f, 280f))
    wave(centerY = 40.8f, radius = 26f, startAngle = 226.2f, sweep = 87.6f, t = phase(580f, 220f))
}

private fun trimmed(path: Path, fraction: Float): Path {
    if (fraction >= 1f) return path
    val out = Path()
    if (fraction <= 0f) return out
    val measure = PathMeasure().apply { setPath(path, false) }
    measure.getSegment(0f, measure.length * fraction, out, true)
    return out
}

private fun easeOut(t: Float): Float = 1f - (1f - t) * (1f - t) * (1f - t)

/** Ease-out with a small overshoot, like Android's overshoot interpolator. */
private fun backOut(t: Float): Float {
    if (t <= 0f) return 0f
    val s = 1.70158f
    val u = t - 1f
    return 1f + (s + 1f) * u * u * u + s * u * u
}
