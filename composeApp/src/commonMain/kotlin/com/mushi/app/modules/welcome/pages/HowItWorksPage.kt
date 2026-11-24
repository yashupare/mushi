package com.mushi.app.modules.welcome.pages

import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumListTile
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxDocumentDownload
import com.mushi.app.resources.iconsax.IconsaxMusic
import com.mushi.app.resources.iconsax.IconsaxSearch
import com.mushi.app.resources.iconsax.User
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.welcome_guide_lyrics_desc
import com.mushi.app.generated.resources.welcome_guide_lyrics_title
import com.mushi.app.generated.resources.welcome_guide_offline_desc
import com.mushi.app.generated.resources.welcome_guide_offline_title
import com.mushi.app.generated.resources.welcome_guide_search_desc
import com.mushi.app.generated.resources.welcome_guide_search_title
import com.mushi.app.generated.resources.welcome_guide_signin_desc
import com.mushi.app.generated.resources.welcome_guide_signin_title
import com.mushi.app.generated.resources.welcome_guide_subtitle
import com.mushi.app.generated.resources.welcome_guide_title

/**
 * The onboarding guide: a short, concrete answer to "how do I actually use this app?".
 * No marketing prose — just the four things a new user needs to do.
 */
@Composable
fun HowItWorksPage(modifier: Modifier = Modifier, contentPadding: Dp = 28.dp) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(Res.string.welcome_guide_title),
                style = AppTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif))
            Text(
                stringResource(Res.string.welcome_guide_subtitle),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.onSurfaceVariant)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WelcomeGuideStep(
                icon = com.mushi.app.core.ui.theming.MushiIcons.User,
                title = Res.string.welcome_guide_signin_title,
                description = Res.string.welcome_guide_signin_desc)
            WelcomeGuideStep(
                icon = com.mushi.app.core.ui.theming.MushiIcons.Search,
                title = Res.string.welcome_guide_search_title,
                description = Res.string.welcome_guide_search_desc)
            WelcomeGuideStep(
                icon = com.mushi.app.core.ui.theming.MushiIcons.Download,
                title = Res.string.welcome_guide_offline_title,
                description = Res.string.welcome_guide_offline_desc)
            WelcomeGuideStep(
                icon = com.mushi.app.core.ui.theming.MushiIcons.MusicNote,
                title = Res.string.welcome_guide_lyrics_title,
                description = Res.string.welcome_guide_lyrics_desc)
        }
    }
}

@Composable
private fun WelcomeGuideStep(
    icon: ImageVector,
    title: StringResource,
    description: StringResource,
) {
    MillenniumListTile(
        leading = {
            MillenniumIcon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppTheme.colors.primary)
        },
        title = { Text(stringResource(title)) },
        subtitle = { Text(stringResource(description)) })
}
