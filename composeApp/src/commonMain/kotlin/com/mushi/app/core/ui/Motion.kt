package com.mushi.app.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * The app's motion tokens. Every animation picks one of these instead of choosing its own
 * timing, so the whole app moves with one feel.
 *
 * - Spatial specs move things (position, size, scale) and may overshoot a little.
 * - Effects specs change colour and opacity and never overshoot.
 *
 * Values follow Material 3 Expressive's motion scheme (fast / default / slow), tuned on a phone.
 */
object MushiMotion {
    /** Press, toggles, small icons, the like pop. */
    fun <T> spatialFast(): SpringSpec<T> = spring(dampingRatio = 0.6f, stiffness = 800f)

    /** Cards, sheets, the tab indicator, list reordering. */
    fun <T> spatialDefault(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = 380f)

    /** Full-screen moves, the launch hand-off, lyrics scrolling. */
    fun <T> spatialSlow(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = 200f)

    /** Colour and opacity on taps. */
    fun <T> effectsFast(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 3800f)

    /** Fades and crossfades, skeleton to content. */
    fun <T> effectsDefault(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 1600f)

    /** Slow fades for large surfaces. */
    fun <T> effectsSlow(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 800f)

    /** Delay between consecutive items rising into view. */
    const val STAGGER_STEP_MS = 45L

    /** Items after this many rise together, so a long list never waits on its tail. */
    const val STAGGER_MAX_ITEMS = 8

    /** Only items composed this soon after a list's first item join the entrance. */
    const val STAGGER_WINDOW_MS = 600L
}

/**
 * Whether the system asks for reduced motion ("Remove animations" on Android, "Reduce Motion" on
 * iOS). Provided at the app root by the theme. Looping animations stop, entrances appear in place,
 * and the launch animation is skipped.
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

/** Reads the platform's reduced-motion setting. */
@Composable
expect fun rememberSystemReduceMotion(): Boolean

/** [spec], or an instant snap when reduced motion is on. */
@Composable
fun <T> motionOrSnap(spec: FiniteAnimationSpec<T>): FiniteAnimationSpec<T> =
    if (LocalReduceMotion.current) snap() else spec

/** The spring every press and select animation shares (an alias of [MushiMotion.spatialFast]). */
fun <T> pressSpring() = MushiMotion.spatialFast<T>()

/**
 * Shrinks the element a little while a finger is down and springs back on release. It watches
 * touches without consuming them, so taps, swipes and drags on the same element still work.
 */
fun Modifier.pressScale(pressedScale: Float = 0.96f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = pressSpring(),
        label = "press_scale")
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                pressed = true
                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                pressed = false
            }
        }
}

/**
 * Tracks when a list's content first appeared, so only the items that arrive together (the first
 * screenful after loading) rise in one after another. Items scrolled into view later just appear.
 * Pass a [key] that changes when the content is replaced (a new tab, a new search) to replay it.
 */
@Stable
class StaggerState internal constructor() {
    internal var start: TimeMark? = null
}

@Composable
fun rememberStaggerState(key: Any? = Unit): StaggerState = remember(key) { StaggerState() }

/**
 * Fades the item in and lifts it 16 dp into place, delayed by its [index] so a freshly loaded
 * list builds from the top down. Does nothing under reduced motion or outside the entrance window.
 */
fun Modifier.staggeredEntrance(state: StaggerState, index: Int): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    val animate = remember {
        val start = state.start ?: TimeSource.Monotonic.markNow().also { state.start = it }
        !reduceMotion && start.elapsedNow() < MushiMotion.STAGGER_WINDOW_MS.milliseconds
    }
    if (!animate) return@composed this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index.coerceIn(0, MushiMotion.STAGGER_MAX_ITEMS) * MushiMotion.STAGGER_STEP_MS)
        progress.animateTo(1f, MushiMotion.spatialDefault())
    }
    this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * 16.dp.toPx()
    }
}

/** Standard list-row motion: fade in and out, and glide to a new place when the list reorders. */
@Composable
fun LazyItemScope.listItemMotion(): Modifier =
    if (LocalReduceMotion.current) Modifier
    else Modifier.animateItem(
        fadeInSpec = MushiMotion.effectsDefault(),
        placementSpec = MushiMotion.spatialDefault<IntOffset>(),
        fadeOutSpec = MushiMotion.effectsFast())

/** [listItemMotion] for grids. */
@Composable
fun LazyGridItemScope.gridItemMotion(): Modifier =
    if (LocalReduceMotion.current) Modifier
    else Modifier.animateItem(
        fadeInSpec = MushiMotion.effectsDefault(),
        placementSpec = MushiMotion.spatialDefault<IntOffset>(),
        fadeOutSpec = MushiMotion.effectsFast())
