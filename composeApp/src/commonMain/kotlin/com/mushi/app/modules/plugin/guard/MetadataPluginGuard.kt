package com.mushi.app.modules.plugin.guard

import com.mushi.app.core.ui.component.MushiButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import com.mushi.app.core.ui.component.ApplicationMainBar
import com.mushi.app.core.ui.component.LargeTitle
import com.mushi.app.core.ui.component.LoginRequiredDisplay
import com.mushi.app.core.ui.component.rememberLargeTitleState
import com.mushi.app.core.ui.component.ProfileButton
import com.mushi.app.modules.plugin.MetadataAuthViewModel
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAdd
import com.mushi.app.resources.iconsax.IconsaxBoxAdd
import com.mushi.app.resources.iconsax.IconsaxCheckCircle
import com.mushi.app.resources.iconsax.IconsaxMusicLibrary
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumCard
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.theme.MillenniumTheme
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.no_metadata_plugin_action_install
import com.mushi.app.generated.resources.no_metadata_plugin_action_local
import com.mushi.app.generated.resources.no_metadata_plugin_local_hint
import com.mushi.app.generated.resources.no_metadata_plugin_action_select
import com.mushi.app.generated.resources.no_metadata_plugin_description_install
import com.mushi.app.generated.resources.no_metadata_plugin_description_select
import com.mushi.app.generated.resources.no_metadata_plugin_step_discover
import com.mushi.app.generated.resources.no_metadata_plugin_step_install
import com.mushi.app.generated.resources.no_metadata_plugin_step_select
import com.mushi.app.generated.resources.no_metadata_plugin_title

/**
 * Renders [content] only while a metadata plugin is selected. Otherwise shows a guided
 * "no plugin" state with a call to action leading to the plugin manager, instead of letting
 * the screen sit on an endless loading skeleton.
 *
 * @param backButton whether the placeholder's top bar shows a back button (for pushed screens).
 * @param requireLogin also swap [content] for a friendly sign-in prompt while the plugin needs an
 * account and the user isn't signed in (for screens that only make sense when logged in).
 * @param title large title shown above the sign-in prompt, so a signed-out main tab looks like
 * its signed-in self (Home "Browse", Search "Search"). Leave null for screens without one.
 * @param showTopBar false when the host screen already draws its own app bar (e.g. Library tabs).
 */
@Composable
fun MetadataPluginGuard(
    backButton: Boolean = false,
    showTopBar: Boolean = true,
    requireLogin: Boolean = false,
    title: String? = null,
    viewModel: MetadataPluginGuardViewModel = koinViewModel(),
    authViewModel: MetadataAuthViewModel = koinViewModel(),
    content: @Composable () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        is MetadataPluginGuardState.Available ->
            if (requireLogin && authState.loginRequired) {
                Column(Modifier.fillMaxSize().background(MillenniumTheme.colors.background)) {
                    if (showTopBar) ApplicationMainBar(
                        backButton = backButton,
                        // Main tab screens keep the profile button so Settings stays reachable.
                        actions = { if (!backButton) ProfileButton() })
                    if (showTopBar && title != null) LargeTitle(
                        text = title,
                        state = rememberLargeTitleState())
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = LocalAppShellBottomInset.current),
                        contentAlignment = Alignment.Center) {
                        LoginRequiredDisplay(
                            pluginName = authState.pluginName,
                            onLogin = authViewModel::login,
                            isLoggingIn = authState.isLoggingIn,
                            modifier = Modifier.widthIn(max = 420.dp))
                    }
                }
            } else {
                content()
            }
        is MetadataPluginGuardState.Loading -> Box(
            Modifier.fillMaxSize().background(MillenniumTheme.colors.background))
        is MetadataPluginGuardState.Missing -> NoMetadataPluginScreen(
            hasInstalledPlugins = s.hasInstalledPlugins,
            backButton = backButton,
            showTopBar = showTopBar,
            onOpenPlugins = viewModel::openPlugins,
            onOpenLocalTracks = viewModel::openLocalTracks)
    }
}

@Composable
private fun NoMetadataPluginScreen(
    hasInstalledPlugins: Boolean,
    backButton: Boolean,
    showTopBar: Boolean,
    onOpenPlugins: () -> Unit,
    onOpenLocalTracks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MillenniumTheme.colors.background)) {
        if (showTopBar) ApplicationMainBar(
            backButton = backButton,
            actions = { if (!backButton) ProfileButton() })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = LocalAppShellBottomInset.current),
            contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HeroBadge()

                Text(
                    text = stringResource(Res.string.no_metadata_plugin_title),
                    style = AppTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.onSurface,
                    textAlign = TextAlign.Center)

                Text(
                    text = stringResource(
                        if (hasInstalledPlugins) Res.string.no_metadata_plugin_description_select
                        else Res.string.no_metadata_plugin_description_install),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center)

                if (!hasInstalledPlugins) {
                    MillenniumCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Step(1, Res.string.no_metadata_plugin_step_discover)
                            Step(2, Res.string.no_metadata_plugin_step_install)
                            Step(3, Res.string.no_metadata_plugin_step_select)
                        }
                    }
                }

                MushiButton(
                    onClick = onOpenPlugins,
                    style = MillenniumButtonStyle.Primary) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MillenniumIcon(
                            if (hasInstalledPlugins) Iconsax.IconsaxCheckCircle else com.mushi.app.core.ui.theming.MushiIcons.Plus,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Text(
                            stringResource(
                                if (hasInstalledPlugins) Res.string.no_metadata_plugin_action_select
                                else Res.string.no_metadata_plugin_action_install))
                    }
                }

                MillenniumButton(
                    onClick = onOpenLocalTracks,
                    style = MillenniumButtonStyle.Secondary) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MillenniumIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Playlist,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Text(stringResource(Res.string.no_metadata_plugin_action_local))
                    }
                }

                Text(
                    text = stringResource(Res.string.no_metadata_plugin_local_hint),
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun HeroBadge() {
    val primary = AppTheme.colors.primary
    Box(
        modifier = Modifier
            .size(104.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(primary.copy(alpha = 0.22f), primary.copy(alpha = 0f)))),
        contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            MillenniumIcon(
                com.mushi.app.core.ui.theming.MushiIcons.AddSquare,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = primary)
        }
    }
}

@Composable
private fun Step(index: Int, label: StringResource) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center) {
            Text(
                text = index.toString(),
                style = AppTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.primary)
        }
        Text(
            text = stringResource(label),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurface)
    }
}
