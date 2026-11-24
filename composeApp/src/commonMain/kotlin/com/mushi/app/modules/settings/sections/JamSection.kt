package com.mushi.app.modules.settings.sections


import org.spotube.millennium.composables.MillenniumIcon
import androidx.compose.foundation.lazy.LazyListScope

import com.mushi.app.core.ui.theming.AppTheme
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.resources.iconsax.CustomServer
import com.mushi.app.resources.iconsax.Iconsax
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_jam_server_title
import com.mushi.app.generated.resources.settings_section_jam

internal fun LazyListScope.jamSection(
    settings: UserSettings,
    navigationCommands: NavigationCommands) {
    settingsSectionHeader(Res.string.settings_section_jam)
    settingsSectionCard(
        items = listOf(
            {
                SettingCardItem(
                    title = stringResource(Res.string.settings_jam_server_title),
                    subtitle = settings.jamServerUrl,
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Server,
                            stringResource(Res.string.settings_jam_server_title))
                    },
                    trailingContent = {
                        MillenniumIcon(
                            imageVector = FeatherIcons.ChevronRight,
                            contentDescription = stringResource(Res.string.settings_jam_server_title),
                            tint = AppTheme.colors.onSurfaceVariant)
                    },
                    onClick = { navigationCommands.navigateTo(Routes.JamSettings) })
            })
    )
}
