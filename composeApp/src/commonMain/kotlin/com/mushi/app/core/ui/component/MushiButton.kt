package com.mushi.app.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.composeunstyled.LocalContentColor
import com.composeunstyled.LocalTextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.theme.MillenniumColorSchemes
import org.spotube.millennium.theme.MillenniumTheme

/**
 * [MillenniumButton] whose primary style labels are readable on every palette.
 *
 * Millennium's text always defaults to the page text colour, ignoring the button's label colour,
 * so a primary button would draw white text on its accent fill. That fails contrast on light
 * accents (Aurora's violet, Tide's teal). This wrapper re-themes only the button's content so
 * text and icons use the palette's `onAccent` colour. Other styles are passed through unchanged.
 */
@Composable
fun MushiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    style: MillenniumButtonStyle = MillenniumButtonStyle.Primary,
    content: @Composable () -> Unit,
) {
    MillenniumButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        style = style,
    ) {
        if (style == MillenniumButtonStyle.Primary) OnAccentContent(content) else content()
    }
}

/** Provides a Millennium theme whose text colour is the accent's label colour, for content on an accent fill. */
@Composable
fun OnAccentContent(content: @Composable () -> Unit) {
    val colors = MillenniumTheme.colors
    val config = MillenniumTheme.config
    // The button's label style (weight, size) arrives through LocalTextStyle; the nested theme
    // resets it, so it's captured here and provided again, with the label colour.
    val buttonTextStyle = LocalTextStyle.current
    val onAccentConfig = remember(colors, config) {
        val onAccent = colors.copy(text = colors.onAccent, muted = colors.onAccent.copy(alpha = 0.8f))
        config.copy(
            colorSchemes = MillenniumColorSchemes(
                light = onAccent,
                dark = onAccent,
                orangeLight = onAccent,
                orangeDark = onAccent,
                limeLight = onAccent,
                limeDark = onAccent),
            // No cross-fade: the label must have the right colour from its first frame.
            themeTransitionMillis = 0)
    }
    MillenniumTheme(dark = colors.background.luminance() < 0.5f, config = onAccentConfig) {
        CompositionLocalProvider(
            LocalTextStyle provides buttonTextStyle.copy(color = colors.onAccent),
            LocalContentColor provides colors.onAccent,
            content = content)
    }
}
