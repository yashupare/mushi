package com.mushi.app.modules.shell

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.theming.AppTheme
import kotlin.math.PI
import kotlin.math.sin

private val ThumbRadius = 8.dp
private val WaveLength = 32.dp
private val WaveHeight = 3.dp

/** Where a touch at [x] falls on a track that is inset by [inset] at both ends, 0 to 1. */
internal fun sliderFraction(x: Float, width: Float, inset: Float): Float {
    val length = width - 2 * inset
    return if (length > 0f) ((x - inset) / length).coerceIn(0f, 1f) else 0f
}

/**
 * A seek slider whose played part ripples while music plays and lies flat when it stops. [wave]
 * is a running clock in seconds and is only read while drawing.
 */
@Composable
internal fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    enabled: Boolean,
    label: String,
    wave: State<Float>,
    waving: Boolean,
    modifier: Modifier = Modifier
) {
    val change by rememberUpdatedState(onValueChange)
    val finished by rememberUpdatedState(onValueChangeFinished)
    val fill = AppTheme.colors.onSurface
    val rest = fill.copy(alpha = 0.2f)
    val height by animateDpAsState(if (waving) WaveHeight else 0.dp, tween(400), label = "wave_height")

    Canvas(
        modifier
            .height(32.dp)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                if (enabled) setProgress {
                    change(it.coerceIn(0f, 1f))
                    finished()
                    true
                }
            }
            .pointerInput(enabled) {
                if (enabled) detectTapGestures { offset ->
                    change(sliderFraction(offset.x, size.width.toFloat(), ThumbRadius.toPx()))
                    finished()
                }
            }
            .pointerInput(enabled) {
                if (enabled) detectHorizontalDragGestures(
                    onDragStart = { change(sliderFraction(it.x, size.width.toFloat(), ThumbRadius.toPx())) },
                    onDragEnd = { finished() },
                    onDragCancel = { finished() }
                ) { event, _ ->
                    change(sliderFraction(event.position.x, size.width.toFloat(), ThumbRadius.toPx()))
                }
            }
    ) {
        val inset = ThumbRadius.toPx()
        val stroke = 4.dp.toPx()
        val middle = size.height / 2f
        val thumb = inset + (size.width - 2 * inset) * value.coerceIn(0f, 1f)
        val end = size.width - inset

        drawLine(rest, Offset(thumb, middle), Offset(end, middle), stroke, StrokeCap.Round)

        val amplitude = height.toPx()
        if (amplitude <= 0f) {
            drawLine(fill, Offset(inset, middle), Offset(thumb, middle), stroke, StrokeCap.Round)
        } else {
            val length = WaveLength.toPx()
            val phase = wave.value * 4f
            fun yAt(x: Float) = middle + amplitude * sin((x / length) * 2f * PI.toFloat() - phase)
            val step = 2.dp.toPx()
            val path = Path().apply {
                moveTo(inset, yAt(inset))
                var x = inset + step
                while (x < thumb) {
                    lineTo(x, yAt(x))
                    x += step
                }
                lineTo(thumb, yAt(thumb))
            }
            drawPath(path, fill, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
        drawCircle(fill, radius = inset, center = Offset(thumb, middle))
    }
}
