package com.mushi.app.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.core.ui.theming.MushiIcons
import kotlinx.coroutines.launch
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.theme.MillenniumTheme
import kotlin.math.roundToInt

/** How far, in dp, a row has to be pulled before letting go adds it. */
private const val QueueThresholdDp = 72
private const val MaxPullDp = 112

/**
 * Pull [content] to the right to add it to the queue. The queue icon is revealed behind the row,
 * a vibration marks the point where letting go will add it, and the row springs back. Offered as
 * a shortcut only: the row's menu has the same action.
 */
@Composable
fun SwipeToQueue(
    enabled: Boolean,
    onQueue: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val threshold = with(density) { QueueThresholdDp.dp.toPx() }
    val maxPull = with(density) { MaxPullDp.dp.toPx() }
    val pull = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val currentOnQueue by rememberUpdatedState(onQueue)
    val drag = rememberDraggableState { delta ->
        val before = pull.value
        val after = (before + delta).coerceIn(0f, maxPull)
        if (before < threshold && after >= threshold) haptics.tap()
        scope.launch { pull.snapTo(after) }
    }

    Box(
        modifier.draggable(
            state = drag,
            orientation = Orientation.Horizontal,
            enabled = enabled,
            onDragStopped = {
                if (pull.value >= threshold) currentOnQueue()
                pull.animateTo(0f, pressSpring())
            })
    ) {
        // The strip uncovered to the left of the row, exactly as wide as the pull.
        Box(
            Modifier
                .matchParentSize()
                .layout { measurable, constraints ->
                    val width = pullPixels(pull.value)
                    val placeable = measurable.measure(Constraints.fixed(width, constraints.maxHeight))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, 0) }
                }
                .background(AppTheme.colors.primary),
            contentAlignment = Alignment.Center
        ) {
            MillenniumIcon(
                imageVector = MushiIcons.Playlist,
                contentDescription = null,
                tint = MillenniumTheme.colors.onAccent,
                modifier = Modifier.size(24.dp).graphicsLayer {
                    val reached = (pull.value / threshold).coerceIn(0f, 1f)
                    alpha = reached
                    scaleX = 0.6f + 0.4f * reached
                    scaleY = scaleX
                })
        }
        Box(Modifier.offset { IntOffset(pullPixels(pull.value), 0) }) { content() }
    }
}

/** The pull in whole pixels. The spring back swings a little past zero, which must not show. */
internal fun pullPixels(pull: Float): Int = pull.roundToInt().coerceAtLeast(0)
