package com.mushi.app.modules.settings


import com.mushi.app.core.ui.misc.AppSpinner
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumSeparator
import org.spotube.millennium.composables.MillenniumSidebarItem
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.PlatformType
import com.mushi.app.getPlatform
import com.mushi.app.core.AppEdition
import com.mushi.app.core.discovery.rememberLocalNetworkPermissionRequester
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.modules.blacklist.BlacklistContent
import com.mushi.app.modules.plugin.PluginScreenContent
import com.mushi.app.modules.settings.sections.appearanceSection
import com.mushi.app.modules.settings.sections.backupSection
import com.mushi.app.modules.settings.sections.cacheSection
import com.mushi.app.modules.settings.sections.desktopSection
import com.mushi.app.modules.settings.sections.downloadsSection
import com.mushi.app.modules.settings.sections.languageRegionSection
import com.mushi.app.modules.settings.sections.playbackSection
import com.mushi.app.modules.settings.sections.supportSection
import com.mushi.app.modules.settings.sections.SettingsPageTitle
import com.mushi.app.modules.settings.sections.updatesSection
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxBoxAdd
import com.mushi.app.resources.iconsax.IconsaxCloseSquare
import com.mushi.app.resources.iconsax.IconsaxColorSwatch
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.resources.iconsax.IconsaxDocumentDownload
import com.mushi.app.resources.iconsax.IconsaxForbidden
import com.mushi.app.resources.iconsax.IconsaxHeart2
import com.mushi.app.resources.iconsax.IconsaxImportArrow2
import com.mushi.app.resources.iconsax.IconsaxInformation
import com.mushi.app.resources.iconsax.IconsaxLanguageSquare
import com.mushi.app.resources.iconsax.IconsaxMusicPlay
import com.mushi.app.resources.iconsax.IconsaxSoundTwotone
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.common_close
import com.mushi.app.generated.resources.settings_blacklist_title
import com.mushi.app.generated.resources.settings_screen_title
import com.mushi.app.generated.resources.settings_section_about
import com.mushi.app.generated.resources.settings_section_appearance
import com.mushi.app.generated.resources.settings_section_caching
import com.mushi.app.generated.resources.settings_section_desktop
import com.mushi.app.generated.resources.settings_section_downloads
import com.mushi.app.generated.resources.settings_section_jam
import com.mushi.app.generated.resources.settings_section_language_region
import com.mushi.app.generated.resources.settings_section_playback
import com.mushi.app.generated.resources.settings_section_plugins
import com.mushi.app.generated.resources.settings_section_support

private val SettingsSidebarWidth = 236.dp

/**
 * The sections reachable from the large-screen settings sidebar, in display order.
 *
 * Each entry maps to either a list-based settings section or a standalone
 * screen (Plugins, Blacklist, Group Jam) whose content is embedded in the tab.
 */
internal enum class SettingsTab(
    val titleRes: StringResource,
    val icon: ImageVector) {
    Plugins(Res.string.settings_section_plugins, com.mushi.app.core.ui.theming.MushiIcons.AddSquare),
    LanguageRegion(Res.string.settings_section_language_region, Iconsax.IconsaxLanguageSquare),
    Appearance(Res.string.settings_section_appearance, com.mushi.app.core.ui.theming.MushiIcons.Palette),
    Playback(Res.string.settings_section_playback, com.mushi.app.core.ui.theming.MushiIcons.MusicNote),
    Blacklist(Res.string.settings_blacklist_title, com.mushi.app.core.ui.theming.MushiIcons.Forbidden),
    GroupJam(Res.string.settings_section_jam, com.mushi.app.core.ui.theming.MushiIcons.Waveform),
    Caching(Res.string.settings_section_caching, com.mushi.app.core.ui.theming.MushiIcons.Download),
    Downloads(Res.string.settings_section_downloads, com.mushi.app.core.ui.theming.MushiIcons.Download),
    Desktop(Res.string.settings_section_desktop, com.mushi.app.core.ui.theming.MushiIcons.Monitor),
    Support(Res.string.settings_section_support, Iconsax.IconsaxHeart2),
    About(Res.string.settings_section_about, com.mushi.app.core.ui.theming.MushiIcons.Info),
}

/**
 * Large-screen settings, presented as a modal overlay (dialog) over the whole app so it
 * never sits next to the main navigation sidebar.
 */
@Composable
internal fun SettingsDialog(
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // The dialog is its own surface; the player/bottom bars behind it should not
        // reserve any inset inside the dialog content.
        CompositionLocalProvider(LocalAppShellBottomInset provides 0.dp) {
            SettingsPane(
                settingsViewModel = settingsViewModel,
                onClose = onDismiss)
        }
    }
}

@Composable
private fun SettingsPane(
    settingsViewModel: SettingsViewModel,
    onClose: () -> Unit) {
    val navigatorCommands: NavigationCommands = koinInject()
    val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()
    val requestLocalNetworkPermission = rememberLocalNetworkPermissionRequester()
    val contentPadding = remember { PaddingValues(top = 16.dp, bottom = 24.dp) }

    val platformType = remember { getPlatform().type }
    val isDesktopPlatform = platformType == PlatformType.Windows ||
            platformType == PlatformType.Linux ||
            platformType == PlatformType.MacOS

    val tabs = remember(isDesktopPlatform) {
        SettingsTab.entries.filter {
            (it != SettingsTab.Desktop || isDesktopPlatform) &&
                (it != SettingsTab.Plugins || AppEdition.PLUGIN_MANAGEMENT_ENABLED) &&
                (it != SettingsTab.Support || AppEdition.UPSTREAM_LINKS_ENABLED)
        }
    }
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val selectedTab = tabs.getOrElse(selectedIndex) { tabs.first() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .heightIn(max = 860.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.surfaceContainerHigh)) {
            SettingsSidebar(
                tabs = tabs,
                selectedTab = selectedTab,
                onSelect = { selectedIndex = tabs.indexOf(it) })
            MillenniumSeparator(
                modifier = Modifier.fillMaxHeight(),
                vertical = true)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        SettingsPageTitle(selectedTab.titleRes)
                    }
                    MillenniumIconButton(
                        onClick = onClose,
                        modifier = Modifier.padding(end = 8.dp),
                        style = MillenniumIconButtonStyle.Quiet) {
                        MillenniumIcon(
                            imageVector = com.mushi.app.core.ui.theming.MushiIcons.Close,
                            contentDescription = stringResource(Res.string.common_close))
                    }
                }
                MillenniumSeparator()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()) {
                    SettingsTabContent(
                        tab = selectedTab,
                        settings = settingsState,
                        settingsViewModel = settingsViewModel,
                        navigatorCommands = navigatorCommands,
                        requestLocalNetworkPermission = requestLocalNetworkPermission,
                        contentPadding = contentPadding)
                }
            }
        }
    }
}

@Composable
private fun SettingsSidebar(
    tabs: List<SettingsTab>,
    selectedTab: SettingsTab,
    onSelect: (SettingsTab) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .width(SettingsSidebarWidth)
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        item {
            Text(
                stringResource(Res.string.settings_screen_title),
                style = AppTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 16.dp)
            )
        }
        items(tabs, key = { it.name }) { tab ->
            MillenniumSidebarItem(
                label = stringResource(tab.titleRes),
                onClick = { onSelect(tab) },
                selected = tab == selectedTab,
                contentDescription = stringResource(tab.titleRes),
                icon = tab.icon)
        }
    }
}

@Composable
private fun SettingsTabContent(
    tab: SettingsTab,
    settings: UserSettings?,
    settingsViewModel: SettingsViewModel,
    navigatorCommands: NavigationCommands,
    requestLocalNetworkPermission: () -> Unit,
    contentPadding: PaddingValues) {
    when (tab) {
        SettingsTab.Plugins -> {
            PluginScreenContent(viewModel = koinViewModel())
        }

        SettingsTab.Blacklist -> {
            BlacklistContent(viewModel = koinViewModel())
        }

        SettingsTab.GroupJam -> {
            JamSettingsContent(viewModel = koinViewModel())
        }

        else -> {
            if (settings == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    AppSpinner(size = 32.dp)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 1280.dp)
                            .align(Alignment.TopCenter),
                        contentPadding = contentPadding) {
                        when (tab) {
                            SettingsTab.LanguageRegion ->
                                languageRegionSection(settings, settingsViewModel, includeHeader = false)

                            SettingsTab.Appearance ->
                                appearanceSection(settings, settingsViewModel, includeHeader = false)

                            SettingsTab.Playback ->
                                playbackSection(
                                    settings = settings,
                                    settingsViewModel = settingsViewModel,
                                    navigatorCommands = navigatorCommands,
                                    requestLocalNetworkPermission = requestLocalNetworkPermission,
                                    includeBlacklist = false,
                                    includeHeader = false)

                            SettingsTab.Caching ->
                                cacheSection(settings, settingsViewModel, includeHeader = false)

                            SettingsTab.Downloads -> {
                                downloadsSection(settings, settingsViewModel, includeHeader = false)
                                backupSection()
                            }

                            SettingsTab.Desktop ->
                                desktopSection(settings, settingsViewModel, includeHeader = false)

                            SettingsTab.Support ->
                                supportSection(
                                    settings = settings,
                                    settingsViewModel = settingsViewModel,
                                    includeHeader = false)

                            SettingsTab.About ->
                                updatesSection(settings, settingsViewModel, includeHeader = false)
                        }
                    }
                }
            }
        }
    }
}
