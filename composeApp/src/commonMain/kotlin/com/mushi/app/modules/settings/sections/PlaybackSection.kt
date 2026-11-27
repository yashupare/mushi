package com.mushi.app.modules.settings.sections


import org.spotube.millennium.composables.MillenniumIcon
import androidx.compose.foundation.lazy.LazyListScope

import com.mushi.app.core.ui.theming.AppTheme
import androidx.compose.runtime.Composable
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Cast
import compose.icons.feathericons.Radio
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Server
import compose.icons.feathericons.Sliders
import com.mushi.app.generated.resources.*
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SelectionSettingCard
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.modules.settings.components.SwitchSettingCard
import com.mushi.app.modules.settings.components.TextInputSettingCard
import com.mushi.app.resources.iconsax.CustomServer
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxForbidden
import com.mushi.app.resources.iconsax.IconsaxMusicPlay
import com.mushi.app.resources.iconsax.IconsaxRepeatArrow
import com.mushi.app.resources.iconsax.IconsaxVerticalSetting
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.playbackSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    navigatorCommands: NavigationCommands,
    requestLocalNetworkPermission: () -> Unit,
    includeBlacklist: Boolean = true,
    includeHeader: Boolean = true) {
    val streamingFormats = availableAudioFormats(settings.streamingMusicFormat, streamingFormatPresets)
    val streamingQualities = availableAudioQualities(
        format = settings.streamingMusicFormat,
        current = settings.streamingMusicQuality)

    val cards: List<@Composable () -> Unit> = buildList {
        add {
            SelectionSettingCard(
                title = stringResource(Res.string.settings_streaming_format_title),
                subtitle = stringResource(
                    Res.string.settings_streaming_format_subtitle_current,
                    settings.streamingMusicFormat.displayLabel()
                ),
                icon = {
                    SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.MusicNote, stringResource(Res.string.settings_streaming_format_title))
                },
                selectedOption = settings.streamingMusicFormat,
                options = streamingFormats,
                optionLabel = { it.displayLabel() },
                onOptionSelected = { format ->
                    settingsViewModel.updateSettings {
                        copy(
                            streamingMusicFormat = format,
                            streamingMusicQuality = format.resolveQuality(streamingMusicQuality))
                    }
                }
            )
        }
        add {
            SelectionSettingCard(
                title = stringResource(Res.string.settings_streaming_quality_title),
                subtitle = stringResource(
                    Res.string.settings_subtitle_current,
                    settings.streamingMusicQuality.displayLabel()
                ),
                icon = {
                    SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Tune, stringResource(Res.string.settings_streaming_quality_title))
                },
                selectedOption = settings.streamingMusicQuality,
                options = streamingQualities,
                optionLabel = { it.displayLabel() },
                onOptionSelected = { quality ->
                    settingsViewModel.updateSettings {
                        copy(streamingMusicQuality = quality)
                    }
                }
            )
        }
        add {
            SwitchSettingCard(
                title = stringResource(Res.string.settings_enable_endless_playback_title),
                subtitle = stringResource(Res.string.settings_enable_endless_playback_subtitle),
                icon = {
                    SettingsItemIcon(
                        com.mushi.app.core.ui.theming.MushiIcons.Repeat,
                        stringResource(Res.string.settings_enable_endless_playback_title)
                    )
                },
                checked = settings.enableEndlessPlayback,
                onCheckedChange = { enabled ->
                    settingsViewModel.updateSettings {
                        copy(enableEndlessPlayback = enabled)
                    }
                }
            )
        }
        if (com.mushi.app.getPlatform().type == com.mushi.app.PlatformType.Android) {
            addAll(soundSettingCards(settings, settingsViewModel))
        }
        addAll(remoteControlSettingCards(settings, settingsViewModel, requestLocalNetworkPermission))
        add {
            val error_whole_number = stringResource(Res.string.settings_error_whole_number)
            val error_port_range = stringResource(Res.string.settings_error_port_range)

            TextInputSettingCard(
                title = stringResource(Res.string.settings_playback_port_title),
                subtitle = stringResource(
                    Res.string.settings_playback_port_subtitle_current,
                    settings.playbackProxyServerPort
                ),
                icon = {
                    SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Server, stringResource(Res.string.settings_playback_port_title))
                },
                value = settings.playbackProxyServerPort.toString(),
                dialogDescription = stringResource(Res.string.settings_playback_port_description),
                placeholder = stringResource(Res.string.settings_playback_port_placeholder),
                normalize = { it.trim() },
                validate = { value ->
                    val port = value.toIntOrNull()
                    when {
                        port == null -> error_whole_number
                        port !in 1..65535 -> error_port_range
                        else -> null
                    }
                },
                onValueSaved = { value ->
                    settingsViewModel.updateSettings {
                        copy(playbackProxyServerPort = value.toInt())
                    }
                }
            )
        }
        if (includeBlacklist) {
            add {
                SettingCardItem(
                    title = stringResource(Res.string.settings_blacklist_title),
                    subtitle = stringResource(Res.string.settings_blacklist_subtitle),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Forbidden,
                            stringResource(Res.string.settings_blacklist_title))
                    },
                    trailingContent = {
                        MillenniumIcon(
                            imageVector = FeatherIcons.ChevronRight,
                            contentDescription = stringResource(Res.string.settings_blacklist_title),
                            tint = AppTheme.colors.onSurfaceVariant)
                    },
                    onClick = {
                        navigatorCommands.navigateTo(Routes.Blacklist)
                    })
            }
        }
    }

    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_playback)
    }
    settingsSectionCard(items = cards)
}
