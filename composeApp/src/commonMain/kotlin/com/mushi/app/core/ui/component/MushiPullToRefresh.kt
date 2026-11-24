package com.mushi.app.core.ui.component

import androidx.compose.animation.core.animate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.MushiMotion
import com.mushi.app.core.ui.rememberHaptics

/** How far to pull before letting go refreshes, and the most the page moves. */
private val RefreshThreshold = 80.dp
private val MaxPull = 130.dp
private val IndicatorSize = 40.dp

/**
 * Pull to refresh with the Mushi logo: as the page is pulled down, the logo draws itself in
 * (arches, dot, then the waves) until it is complete at the refresh point, where a haptic tick
 * marks that letting go will refresh. Releasing past it calls [onRefresh]; either way the logo
 * and the page spring back. While [isRefreshing] the logo stays shown.
 */
@Composable
fun MushiPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { RefreshThreshold.toPx() }
    val maxPx = with(density) { MaxPull.toPx() }
    val haptics = rememberHaptics()
    val currentOnRefresh by rememberUpdatedState(onRefresh)

    var pull by remember { mutableFloatStateOf(0f) }
    var armed by remember { mutableStateOf(false) }

    // While refreshing, rest at the refresh point; afterwards, spring back up.
    LaunchedEffect(isRefreshing) {
        val target = if (isRefreshing) thresholdPx else 0f
        animate(pull, target, animationSpec = MushiMotion.spatialDefault()) { value, _ -> pull = value }
    }

    val connection = remember(thresholdPx, maxPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Scrolling back up while pulled shrinks the pull before the list scrolls.
                if (source == NestedScrollSource.UserInput && available.y < 0f && pull > 0f) {
                    val used = maxOf(available.y, -pull)
                    pull += used
                    armed = pull >= thresholdPx
                    return Offset(0f, used)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Only what the list couldn't scroll (it's at the top) becomes pull, with resistance.
                if (source == NestedScrollSource.UserInput && available.y > 0f) {
                    pull = (pull + available.y * 0.5f).coerceAtMost(maxPx)
                    val nowArmed = pull >= thresholdPx
                    if (nowArmed && !armed) haptics.tick()
                    armed = nowArmed
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pull <= 0f) return Velocity.Zero
                val refresh = pull >= thresholdPx
                armed = false
                if (refresh) currentOnRefresh()
                animate(pull, 0f, animationSpec = MushiMotion.spatialDefault()) { value, _ -> pull = value }
                return available
            }
        }
    }

    Box(modifier.nestedScroll(connection)) {
        Box(Modifier.fillMaxSize().graphicsLayer { translationY = pull * 0.6f }) {
            content()
        }
        Canvas(
            Modifier
                .align(Alignment.TopCenter)
                .size(IndicatorSize)
                .graphicsLayer {
                    val shown = (pull / thresholdPx).coerceIn(0f, 1f)
                    alpha = shown
                    // Centred in the gap the page leaves as it moves down.
                    translationY = pull * 0.6f / 2f - IndicatorSize.toPx() / 2f
                    val scale = 0.7f + 0.3f * shown
                    scaleX = scale
                    scaleY = scale
                }) {
            drawMushiMark((pull / thresholdPx).coerceIn(0f, 1f))
        }
    }
}
