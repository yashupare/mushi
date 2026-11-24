package com.mushi.app.core.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.LocalReduceMotion
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.common_loading
import org.jetbrains.compose.resources.stringResource

/** Phase offsets of the four bars, so they move like a level meter rather than in step. */
private val BarOffsetsMs = intArrayOf(300, 600, 100, 450)

/**
 * The app's loading indicator: four equalizer bars in the accent colour, echoing the logo's sound
 * waves. Replaces spinners for anything that loads in a few seconds. Under reduced motion the bars
 * hold still at mixed heights.
 */
@Composable
fun MushiLoader(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    color: Color = AppTheme.colors.primary,
) {
    val label = stringResource(Res.string.common_loading)
    val reduceMotion = LocalReduceMotion.current
    val heights = if (reduceMotion) {
        listOf(0.45f, 0.9f, 0.6f, 0.75f)
    } else {
        val transition = rememberInfiniteTransition(label = "mushi_loader")
        BarOffsetsMs.map { offset ->
            transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(offset)),
                label = "bar_$offset").value
        }
    }
    Canvas(modifier.size(size).semantics { contentDescription = label }) {
        val bars = heights.size
        val gap = this.size.width * 0.12f
        val barWidth = (this.size.width - gap * (bars - 1)) / bars
        heights.forEachIndexed { i, h ->
            val barHeight = this.size.height * h
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barWidth + gap), this.size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f))
        }
    }
}
