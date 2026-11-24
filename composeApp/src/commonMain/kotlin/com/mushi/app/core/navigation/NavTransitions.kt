package com.mushi.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

private const val TRANSITION_DURATION_MILLIS = 300
private const val FADE_THROUGH_OUT_MILLIS = 90
private const val FADE_THROUGH_SCALE = 0.94f
private const val SLIDE_FRACTION = 0.25f
private const val BEHIND_SLIDE_FRACTION = 0.1f
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** How small the page gets at the end of a back swipe, and how far it drifts sideways. */
private const val PREDICTIVE_BACK_SCALE = 0.88f
private const val PREDICTIVE_BACK_SHIFT = 0.08f
/** The page behind starts this size and grows to full size as the swipe completes. */
private const val PREDICTIVE_BACK_BEHIND_SCALE = 0.96f
/** The page behind starts this visible, so its edges read as "the page you'll land on". */
private const val PREDICTIVE_BACK_BEHIND_ALPHA = 0.6f
/** The leaving page only starts fading after this much of the transition (70%). */
private const val PREDICTIVE_BACK_FADE_DELAY_MILLIS = 210

// NavEntry.contentKey defaults to key.toString()
private val topLevelContentKeys = TOP_LEVEL_ROUTES.map { it.toString() }.toSet()

/**
 * The tab a scene belongs to: the last top-level route in its stack. Switching tabs changes
 * this, while pushing/popping pages inside a tab doesn't.
 */
private fun Scene<NavKey>.tabRoot(): Any? =
    (previousEntries + entries).lastOrNull { it.contentKey in topLevelContentKeys }?.contentKey

private fun AnimatedContentTransitionScope<Scene<NavKey>>.isTabSwitch(): Boolean =
    initialState.tabRoot() != targetState.tabRoot()

/** Material fade-through: the old tab fades out, the new one fades in while scaling up slightly. */
private fun fadeThrough(): ContentTransform {
    val enter = fadeIn(
        tween(TRANSITION_DURATION_MILLIS - FADE_THROUGH_OUT_MILLIS, FADE_THROUGH_OUT_MILLIS, EmphasizedDecelerate)
    ) + scaleIn(
        tween(TRANSITION_DURATION_MILLIS - FADE_THROUGH_OUT_MILLIS, FADE_THROUGH_OUT_MILLIS, EmphasizedDecelerate),
        initialScale = FADE_THROUGH_SCALE
    )
    return enter togetherWith fadeOut(tween(FADE_THROUGH_OUT_MILLIS))
}

/**
 * Pushed/popped pages inside a tab: the page on top slides + fades while the page behind
 * drifts slightly the other way. On pop the leaving page must stay on top (negative target
 * z-index), otherwise the page being revealed is drawn over it and its exit is never seen.
 */
private fun AnimatedContentTransitionScope<Scene<NavKey>>.stackTransition(
    forward: Boolean
): ContentTransform {
    if (isTabSwitch()) return fadeThrough()

    val slide = tween<IntOffset>(TRANSITION_DURATION_MILLIS, easing = EmphasizedDecelerate)
    val enter: EnterTransition
    val exit: ExitTransition
    if (forward) {
        enter = slideInHorizontally(slide) { (it * SLIDE_FRACTION).toInt() } +
            fadeIn(tween(TRANSITION_DURATION_MILLIS * 2 / 3))
        exit = slideOutHorizontally(slide) { -(it * BEHIND_SLIDE_FRACTION).toInt() } +
            fadeOut(tween(TRANSITION_DURATION_MILLIS / 2))
    } else {
        enter = slideInHorizontally(slide) { -(it * BEHIND_SLIDE_FRACTION).toInt() } +
            fadeIn(tween(TRANSITION_DURATION_MILLIS * 2 / 3))
        exit = slideOutHorizontally(slide) { (it * SLIDE_FRACTION).toInt() } +
            fadeOut(tween(TRANSITION_DURATION_MILLIS * 2 / 3))
    }
    return ContentTransform(enter, exit, targetContentZIndex = if (forward) 0f else -1f)
}

val navPushTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    { stackTransition(forward = true) }

val navPopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    { stackTransition(forward = false) }

/**
 * Predictive back (Android's back swipe): while the finger drags, the page shrinks and slides
 * toward the side being swiped, and the page behind fades and settles in. The transition is
 * seeked by the gesture's progress, so linear easing makes it track the finger 1:1; releasing
 * finishes it and cancelling plays it back.
 *
 * [swipeEdge] is 0 for a swipe from the left edge and 1 for the right edge.
 */
private fun AnimatedContentTransitionScope<Scene<NavKey>>.predictiveBackTransition(
    swipeEdge: Int
): ContentTransform {
    if (isTabSwitch()) return fadeThrough()
    val towardRight = swipeEdge == 0
    val linear = tween<Float>(TRANSITION_DURATION_MILLIS, easing = LinearEasing)
    val linearOffset = tween<IntOffset>(TRANSITION_DURATION_MILLIS, easing = LinearEasing)
    val exit = scaleOut(
        animationSpec = linear,
        targetScale = PREDICTIVE_BACK_SCALE,
        transformOrigin = TransformOrigin(if (towardRight) 1f else 0f, 0.5f)
    ) + slideOutHorizontally(linearOffset) { width ->
        val shift = (width * PREDICTIVE_BACK_SHIFT).toInt()
        if (towardRight) shift else -shift
    } + fadeOut(
        // The leaving page stays solid while the finger drags (the page behind shows only around
        // its edges) and fades in the last stretch, after release, so it never looks see-through.
        tween(
            durationMillis = TRANSITION_DURATION_MILLIS - PREDICTIVE_BACK_FADE_DELAY_MILLIS,
            delayMillis = PREDICTIVE_BACK_FADE_DELAY_MILLIS,
            easing = LinearEasing))
    val enter = fadeIn(linear, initialAlpha = PREDICTIVE_BACK_BEHIND_ALPHA) +
        scaleIn(linear, initialScale = PREDICTIVE_BACK_BEHIND_SCALE)
    return ContentTransform(enter, exit, targetContentZIndex = -1f)
}

val navPredictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform =
    { swipeEdge -> predictiveBackTransition(swipeEdge) }
