package com.mushi.app.core.ui.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.TextAutoSize
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumBevelBrush

val LocalSkeletonLoading = compositionLocalOf { false }

/**
 * How visible the skeleton tint is, 0 to 1. It fades in after a short delay (so a fast load
 * never flashes a placeholder) and fades out when loading ends, instead of snapping.
 */
val LocalSkeletonAlpha = compositionLocalOf { 0f }

@Composable
fun SkeletonTree(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Intercept clicks and touches globally when loading
    val interactionModifier = if (isLoading) {
        modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    event.changes.forEach { it.consume() }
                }
            }
        }
    } else {
        modifier
    }

    val skeletonAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isLoading) 1f else 0f,
        animationSpec = if (isLoading) {
            androidx.compose.animation.core.tween(durationMillis = 220, delayMillis = 180)
        } else {
            androidx.compose.animation.core.tween(durationMillis = 260)
        },
        label = "skeleton_alpha")

    CompositionLocalProvider(
        LocalSkeletonLoading provides isLoading,
        LocalSkeletonAlpha provides skeletonAlpha
    ) {
        // Under reduced motion the placeholders stay as a still tint, with no sweeping shimmer.
        if (isLoading && !com.mushi.app.core.ui.LocalReduceMotion.current) {
            // Valentinilk's synchronized hardware-accelerated shimmer loop
            // The library dims what it covers to 25% between sweeps, which hides a faint
            // placeholder on dark pages. Keep the base at 70% so the shapes always read.
            val shimmerTheme = com.valentinilk.shimmer.defaultShimmerTheme.copy(
                shaderColors = listOf(
                    Color.Unspecified.copy(alpha = 0.7f),
                    Color.Unspecified.copy(alpha = 1f),
                    Color.Unspecified.copy(alpha = 0.7f)))
            Box(modifier = interactionModifier.shimmer(
                rememberShimmer(shimmerBounds = ShimmerBounds.View, theme = shimmerTheme))) {
                content()
            }
        } else {
            Box(modifier = interactionModifier) {
                content()
            }
        }
    }
}

fun Modifier.shimmerApply(
    color: Color? = null // defaults to Millennium's recessed skeleton fill
): Modifier = composed {
    val isTreeLoading = LocalSkeletonLoading.current
    val fade = LocalSkeletonAlpha.current
    // A quiet flat tint (the shimmer sweep supplies the movement), not the library's glossy bevel.
    val skeletonBrush = androidx.compose.ui.graphics.SolidColor(
        MillenniumTheme.colors.text.copy(
            alpha = (if (MillenniumTheme.colors.background.luminance() < 0.5f) 0.16f else 0.1f) * fade))

    when {
        !isTreeLoading && fade <= 0.001f -> this
        color != null -> this.background(color.copy(alpha = color.alpha * fade))
        else -> this.background(skeletonBrush)
    }
}

@Composable
fun TextWithShimmer(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current) {
    // While a skeleton tree is loading, the placeholder glyphs must not be readable:
    // only the shimmer bars (from `shimmerApply`) should show through.
    val isLoadingSkeleton = LocalSkeletonLoading.current
    val fade = LocalSkeletonAlpha.current
    // Once loading ends the real text fades in as the placeholder bar fades out.
    val fadedColor = if (fade <= 0.001f) color else {
        val base = when {
            color != Color.Unspecified -> color
            style.color != Color.Unspecified -> style.color
            else -> MillenniumTheme.colors.text
        }
        base.copy(alpha = base.alpha * (1f - fade))
    }
    Text(
        text = text,
        color = if (isLoadingSkeleton) Color.Transparent else fadedColor,
        modifier = Modifier.shimmerApply().then(modifier),
        autoSize = autoSize,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign ?: TextAlign.Start,
        lineHeight = lineHeight,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}