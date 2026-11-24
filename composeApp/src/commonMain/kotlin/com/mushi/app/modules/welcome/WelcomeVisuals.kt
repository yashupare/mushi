package com.mushi.app.modules.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import com.composeunstyled.Text
import com.mushi.app.core.ui.theming.AppTheme
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.theme.MillenniumTheme
import com.mushi.app.generated.resources.*

/**
 * Reapplies the Millennium theme for the onboarding pages using the app's active configuration,
 * so onboarding matches the palette and appearance used by the rest of the app.
 */
@Composable
fun WelcomeTheme(content: @Composable () -> Unit) {
    val dark = AppTheme.colors.surface.luminance() < 0.5f
    MillenniumTheme(
        dark = dark,
        config = MillenniumTheme.config,
        content = content)
}

@Composable
fun WelcomeBackground(modifier: Modifier = Modifier) {
    val scheme = AppTheme.colors
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(scheme.surfaceContainer, scheme.surface)))
    )
}

@Composable
fun WelcomeWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = AppTheme.typography.displaySmall.fontSize
) {
    Text(
        stringResource(Res.string.welcome_app_name),
        modifier = modifier,
        fontFamily = FontFamily(Font(Res.font.cookie_regular)),
        fontSize = fontSize,
        color = AppTheme.colors.onSurface)
}
