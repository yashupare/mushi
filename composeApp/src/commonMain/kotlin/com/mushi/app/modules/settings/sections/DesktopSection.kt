package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.lazy.LazyListScope
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SwitchSettingCard
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxImportArrow2
import com.mushi.app.resources.iconsax.LineDiscord
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.desktopSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    includeHeader: Boolean = true) {
    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_desktop)
    }
    settingsSectionCard(
        items = listOf(
            {
                SwitchSettingCard(
                    title = stringResource(Res.string.settings_desktop_minimize_title),
                    subtitle = stringResource(Res.string.settings_desktop_minimize_subtitle),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Monitor,
                            stringResource(Res.string.settings_desktop_minimize_title)
                        )
                    },
                    checked = settings.minimizeToTray,
                    onCheckedChange = { enabled ->
                        settingsViewModel.updateSettings {
                            copy(minimizeToTray = enabled)
                        }
                    }
                )
            },
            {
                SwitchSettingCard(
                    title = stringResource(Res.string.settings_desktop_discord_title),
                    subtitle = stringResource(Res.string.settings_desktop_discord_subtitle),
                    icon = {
                        SettingsItemIcon(
                            Iconsax.LineDiscord,
                            stringResource(Res.string.settings_desktop_discord_title)
                        )
                    },
                    checked = settings.discordRichPresence,
                    onCheckedChange = { enabled ->
                        settingsViewModel.updateSettings {
                            copy(discordRichPresence = enabled)
                        }
                    }
                )
            })
    )
}

