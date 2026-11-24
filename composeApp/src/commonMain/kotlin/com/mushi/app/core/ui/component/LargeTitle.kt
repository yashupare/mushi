package com.mushi.app.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.ui.theming.AppTheme
import kotlin.math.roundToInt

/** Height of the large title row. */
val LargeTitleHeight = 52.dp

/**
 * Collapse state for a large page title.
 *
 * Attach [connection] with `Modifier.nestedScroll` to a parent of the scrolling content. As the
 * content scrolls up the title shrinks away first, then the content scrolls; scrolling back down
 * to the top brings the title back. [fraction] runs from 0 (full title) to 1 (collapsed) and
 * drives the small title in the top bar.
 */
@Stable
class LargeTitleState internal constructor(private val maxCollapsePx: Float) {
    /** How far the title has collapsed, in pixels: 0 when full, [maxCollapsePx] when gone. */
    internal var offset by mutableFloatStateOf(0f)

    val fraction: Float
        get() = if (maxCollapsePx <= 0f) 1f else (offset / maxCollapsePx).coerceIn(0f, 1f)

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        // Scrolling up: collapse the title before the content moves.
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            if (dy < 0f && offset < maxCollapsePx) {
                val next = (offset - dy).coerceAtMost(maxCollapsePx)
                val used = next - offset
                offset = next
                return Offset(0f, -used)
            }
            return Offset.Zero
        }

        // Scrolling down: once the content is at the top and has nothing left to scroll, the
        // leftover distance brings the title back.
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val dy = available.y
            if (dy > 0f && offset > 0f) {
                val next = (offset - dy).coerceAtLeast(0f)
                val used = offset - next
                offset = next
                return Offset(0f, used)
            }
            return Offset.Zero
        }
    }
}

@Composable
fun rememberLargeTitleState(height: Dp = LargeTitleHeight): LargeTitleState {
    val maxPx = with(LocalDensity.current) { height.toPx() }
    return remember(maxPx) { LargeTitleState(maxPx) }
}

/** The big bold title under the top bar. It slides up and fades as [state] collapses. */
@Composable
fun LargeTitle(
    text: String,
    state: LargeTitleState,
    modifier: Modifier = Modifier,
    height: Dp = LargeTitleHeight
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val collapsed = state.offset.roundToInt().coerceIn(0, placeable.height)
                layout(placeable.width, placeable.height - collapsed) {
                    placeable.placeRelative(0, -collapsed)
                }
            }
            .graphicsLayer { alpha = 1f - state.fraction }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = AppTheme.typography.headlineLarge,
            color = AppTheme.colors.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(vertical = (height - 41.dp).coerceAtLeast(0.dp) / 2)
        )
    }
}
