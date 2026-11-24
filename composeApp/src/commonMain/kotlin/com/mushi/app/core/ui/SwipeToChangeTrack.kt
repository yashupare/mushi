package com.mushi.app.core.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** How far, in dp, a horizontal drag has to travel before it counts as a swipe. */
private const val SwipeThresholdDp = 64

/**
 * Swipe left for the next track and right for the previous one, like Apple Music's mini player
 * and artwork. It only reacts once the drag passes a threshold, so taps and small slips do
 * nothing. Always pair it with visible skip buttons somewhere on screen.
 */
fun Modifier.swipeToChangeTrack(
    enabled: Boolean = true,
    onNext: () -> Unit,
    onPrevious: () -> Unit
): Modifier = composed {
    if (!enabled) return@composed this
    val threshold = with(LocalDensity.current) { SwipeThresholdDp.dp.toPx() }
    val currentNext by rememberUpdatedState(onNext)
    val currentPrevious by rememberUpdatedState(onPrevious)
    val haptics = rememberHaptics()

    pointerInput(threshold) {
        var travelled = 0f
        detectHorizontalDragGestures(
            onDragStart = { travelled = 0f },
            onDragCancel = { travelled = 0f },
            onDragEnd = {
                when {
                    travelled <= -threshold -> { haptics.tick(); currentNext() }
                    travelled >= threshold -> { haptics.tick(); currentPrevious() }
                }
                travelled = 0f
            },
            onHorizontalDrag = { _, delta -> travelled += delta }
        )
    }
}
