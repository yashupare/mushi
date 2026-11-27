package com.mushi.app.modules.settings


import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.PlatformType
import com.mushi.app.getPlatform
import com.mushi.app.core.AppEdition
import com.mushi.app.core.discovery.rememberLocalNetworkPermissionRequester
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.ui.component.ApplicationMainBar
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.settings.sections.appearanceSection
import com.mushi.app.modules.settings.sections.backupSection
import com.mushi.app.modules.settings.sections.cacheSection
import com.mushi.app.modules.settings.sections.desktopSection
import com.mushi.app.modules.settings.sections.downloadsSection
import com.mushi.app.modules.settings.sections.jamSection
import com.mushi.app.modules.settings.sections.languageRegionSection
import com.mushi.app.modules.settings.sections.playbackSection
import com.mushi.app.modules.settings.sections.pluginsSection
import com.mushi.app.modules.settings.sections.connectSection
import com.mushi.app.modules.settings.sections.supportSection
import com.mushi.app.modules.settings.sections.updatesSection
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Minimum width of the settings content area required to show the settings as an
 * overlay dialog instead of a full-screen destination.
 */
private val SettingsDialogMinWidth = 720.dp

/**
 * On large screens settings is presented as a modal overlay so it does not sit next to
 * the main navigation sidebar. On smaller screens it stays a full-screen destination.
 */
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val navigatorCommands: NavigationCommands = koinInject()

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentWindowWidthBreakpoint() isAtLeast WindowBreakpoints.Expanded) {
            SettingsDialog(
                settingsViewModel = settingsViewModel,
                onDismiss = { navigatorCommands.pop(Routes.Settings) })
        } else {
            CompactSettingsScreen(settingsViewModel = settingsViewModel)
        }
    }
}

@Composable
private fun CompactSettingsScreen(settingsViewModel: SettingsViewModel) {
    val navigatorCommands = koinInject<NavigationCommands>()
    val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()
    val shellBottomInset = LocalAppShellBottomInset.current
    val requestLocalNetworkPermission = rememberLocalNetworkPermissionRequester()
    val contentPadding = remember(shellBottomInset) {
        PaddingValues(top = 16.dp, bottom = 16.dp + shellBottomInset)
    }

    val titleState = com.mushi.app.core.ui.component.rememberLargeTitleState()

    Column(modifier = Modifier.fillMaxSize().nestedScroll(titleState.connection)) {
        ApplicationMainBar(
            title = {
                // The bar's own title appears once the large title has scrolled away.
                Box(Modifier.graphicsLayer { alpha = titleState.fraction }) {
                    Text(stringResource(Res.string.settings_screen_title))
                }
            },
            backButton = false
        )
        com.mushi.app.core.ui.component.LargeTitle(
            text = stringResource(Res.string.settings_screen_title),
            state = titleState)
        CompactSettingsContent(
            settings = settingsState,
            settingsViewModel = settingsViewModel,
            navigatorCommands = navigatorCommands,
            requestLocalNetworkPermission = requestLocalNetworkPermission,
            contentPadding = contentPadding,
            modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CompactSettingsContent(
    settings: UserSettings?,
    settingsViewModel: SettingsViewModel,
    navigatorCommands: NavigationCommands,
    requestLocalNetworkPermission: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier) {
    val platformType = remember { getPlatform().type }
    val isDesktopPlatform = platformType == PlatformType.Windows ||
            platformType == PlatformType.Linux ||
            platformType == PlatformType.MacOS

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 1280.dp)
                .align(Alignment.TopCenter),
            contentPadding = contentPadding) {
            connectSection(navigatorCommands = navigatorCommands)
            if (AppEdition.PLUGIN_MANAGEMENT_ENABLED) {
                pluginsSection(
                    navigatorCommands = navigatorCommands)
            }
            if (settings != null) {
                languageRegionSection(
                    settings = settings,
                    settingsViewModel = settingsViewModel)
                appearanceSection(
                    settings = settings,
                    settingsViewModel = settingsViewModel)
                playbackSection(
                    settings = settings,
                    settingsViewModel = settingsViewModel,
                    navigatorCommands = navigatorCommands,
                    requestLocalNetworkPermission = requestLocalNetworkPermission)
                jamSection(
                    settings = settings,
                    navigationCommands = navigatorCommands)
                cacheSection(
                    settings = settings,
                    settingsViewModel = settingsViewModel)
                downloadsSection(
                    settings = settings,
                    settingsViewModel = settingsViewModel)
                backupSection()
                if (isDesktopPlatform) {
                    desktopSection(
                        settings = settings,
                        settingsViewModel = settingsViewModel)
                }
                if (AppEdition.UPSTREAM_LINKS_ENABLED) {
                    supportSection(
                        settings = settings,
                        settingsViewModel = settingsViewModel)
                }
                if (AppEdition.APP_UPDATES_ENABLED) {
                    updatesSection(
                        settings = settings,
                        settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}
