package com.mushi.app.modules.settings.sections

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SwitchSettingCard
import com.mushi.app.modules.settings.components.TextInputSettingCard
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxEdit
import com.mushi.app.resources.iconsax.IconsaxMirroringScreen
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_allow_remote_control_subtitle
import com.mushi.app.generated.resources.settings_allow_remote_control_title
import com.mushi.app.generated.resources.settings_remote_device_name_default
import com.mushi.app.generated.resources.settings_remote_device_name_description
import com.mushi.app.generated.resources.settings_remote_device_name_placeholder
import com.mushi.app.generated.resources.settings_remote_device_name_subtitle
import com.mushi.app.generated.resources.settings_remote_device_name_title

/**
 * "Allow remote control" and "Device name" cards. Shared by the Playback settings section and the
 * Devices screen so both stay identical.
 */
internal fun remoteControlSettingCards(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    requestLocalNetworkPermission: () -> Unit
): List<@Composable () -> Unit> = listOf(
    {
        SwitchSettingCard(
            title = stringResource(Res.string.settings_allow_remote_control_title),
            subtitle = stringResource(Res.string.settings_allow_remote_control_subtitle),
            icon = {
                SettingsItemIcon(
                    com.mushi.app.core.ui.theming.MushiIcons.Cast,
                    stringResource(Res.string.settings_allow_remote_control_title)
                )
            },
            checked = settings.allowRemoteControl,
            onCheckedChange = { enabled ->
                settingsViewModel.updateSettings {
                    copy(allowRemoteControl = enabled)
                }
                // Request the local network permission when enabling remote control
                // so that DNS-SD registration can succeed on Android 16+
                if (enabled) {
                    requestLocalNetworkPermission()
                }
            }
        )
    },
    {
        TextInputSettingCard(
            title = stringResource(Res.string.settings_remote_device_name_title),
            subtitle = stringResource(
                Res.string.settings_remote_device_name_subtitle,
                settings.remoteControlDeviceName.ifBlank { stringResource(Res.string.settings_remote_device_name_default) }
            ),
            icon = {
                SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Edit, stringResource(Res.string.settings_remote_device_name_title))
            },
            value = settings.remoteControlDeviceName,
            dialogDescription = stringResource(Res.string.settings_remote_device_name_description),
            placeholder = stringResource(Res.string.settings_remote_device_name_placeholder),
            normalize = { it.trim() },
            validate = { _ -> null },
            onValueSaved = { value ->
                settingsViewModel.updateSettings {
                    copy(remoteControlDeviceName = value)
                }
            }
        )
    }
)
