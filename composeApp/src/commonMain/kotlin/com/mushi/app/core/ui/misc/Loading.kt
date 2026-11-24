package com.mushi.app.core.ui.misc

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.common_loading

/*
 * How loading looks in Mushi
 * --------------------------
 * - A screen or list whose shape is known shows a skeleton of that shape, with a soft shimmer,
 *   and cross-fades into the real content (never a spinner or a blank page).
 * - A skeleton only appears after a short delay, so a fast load never flashes a placeholder.
 * - A small, short wait with no shape (inside a dialog, a button, a pane) uses [AppSpinner].
 * - Known progress (downloads) stays a determinate bar; it isn't handled here.
 */

/**
 * A modern indeterminate spinner: a rounded arc that grows and shrinks while it turns, over a
 * faint full ring. Use it only for short, shapeless waits; prefer a skeleton for anything with
 * a layout.
 */
@Composable
fun AppSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    strokeWidth: Dp = 3.dp,
    color: Color = AppTheme.colors.primary
) {
    val transition = rememberInfiniteTransition(label = "app_spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "spinner_rotation")
    val sweep by transition.animateFloat(
        initialValue = 36f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse),
        label = "spinner_sweep")
    val description = stringResource(Res.string.common_loading)

    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = description }
    ) {
        val stroke = strokeWidth.toPx()
        val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
        val topLeft = Offset(stroke / 2f, stroke / 2f)
        drawArc(
            color = color.copy(alpha = 0.14f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke))
        rotate(rotation) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

/** A soft placeholder bar. Must sit inside a [SkeletonTree] so it shimmers. */
@Composable
fun SkeletonBar(
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    Box(modifier.height(height).clip(shape).shimmerApply())
}

private val RowWidths = listOf(0.62f, 0.48f, 0.7f, 0.55f, 0.66f)

/** Placeholder with the shape of a track row: artwork, title, artists, duration. */
@Composable
fun TrackRowSkeleton(index: Int = 0, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).shimmerApply())
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBar(Modifier.fillMaxWidth(RowWidths[index % RowWidths.size]), height = 14.dp)
            SkeletonBar(Modifier.fillMaxWidth(RowWidths[(index + 2) % RowWidths.size] * 0.6f), height = 11.dp)
        }
        Spacer(Modifier.width(16.dp))
        SkeletonBar(Modifier.width(28.dp), height = 11.dp)
    }
}

/** A list of track-row placeholders. */
@Composable
fun TrackListSkeleton(modifier: Modifier = Modifier, count: Int = 9) {
    SkeletonTree(isLoading = true, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            repeat(count) { TrackRowSkeleton(it) }
        }
    }
}

/** A grid of card placeholders: square artwork, title, subtitle. */
@Composable
fun CardGridSkeleton(
    modifier: Modifier = Modifier,
    minCellSize: Dp = 160.dp,
    count: Int = 12
) {
    SkeletonTree(isLoading = true, modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellSize),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items((0 until count).toList()) { index ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerApply())
                    SkeletonBar(Modifier.fillMaxWidth(RowWidths[index % RowWidths.size]), height = 14.dp)
                    SkeletonBar(Modifier.fillMaxWidth(0.4f), height = 11.dp)
                }
            }
        }
    }
}

/** Placeholder for synced lyrics: large left-aligned lines of varying length. */
@Composable
fun LyricsSkeleton(modifier: Modifier = Modifier) {
    val widths = listOf(0.78f, 0.55f, 0.7f, 0.4f, 0.74f, 0.6f, 0.82f, 0.5f)
    SkeletonTree(isLoading = true, modifier = modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            widths.forEach { SkeletonBar(Modifier.fillMaxWidth(it), height = 26.dp, shape = RoundedCornerShape(10.dp)) }
        }
    }
}

/** Placeholder for a label/value table such as track details. */
@Composable
fun DetailRowsSkeleton(modifier: Modifier = Modifier, rows: Int = 6) {
    SkeletonTree(isLoading = true, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(rows) { index ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SkeletonBar(Modifier.fillMaxWidth(0.22f), height = 13.dp)
                    SkeletonBar(Modifier.fillMaxWidth(RowWidths[index % RowWidths.size] * 0.5f), height = 13.dp)
                }
            }
        }
    }
}

private enum class LoadPhase { Pending, Skeleton, Content }

/**
 * Shows [skeleton] while [isLoading] and cross-fades to [content] when it finishes. The skeleton
 * only appears after [delayMillis], so a fast load shows nothing in between instead of
 * flashing a placeholder.
 */
@Composable
fun LoadingCrossfade(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Long = 150,
    skeleton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var pastDelay by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        pastDelay = false
        if (isLoading) {
            delay(delayMillis)
            pastDelay = true
        }
    }
    val phase = when {
        !isLoading -> LoadPhase.Content
        pastDelay -> LoadPhase.Skeleton
        else -> LoadPhase.Pending
    }
    Crossfade(
        targetState = phase,
        modifier = modifier,
        animationSpec = tween(240),
        label = "loading_crossfade"
    ) { current ->
        when (current) {
            LoadPhase.Content -> content()
            LoadPhase.Skeleton -> skeleton()
            LoadPhase.Pending -> Box(Modifier.fillMaxSize())
        }
    }
}
