package com.mushi.app.modules.settings.sections


import org.spotube.millennium.composables.MillenniumIcon
import androidx.compose.foundation.lazy.LazyListScope

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Package
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxBoxAdd
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.pluginsSection(
    navigatorCommands: NavigationCommands
) {
    settingsSectionHeader(Res.string.settings_section_plugins)
    settingsSectionCard(
        items = listOf {
            SettingCardItem(
                title = stringResource(Res.string.settings_plugins_manage_title),
                subtitle = stringResource(Res.string.settings_plugins_manage_subtitle),
                icon = {
                    SettingsItemIcon(
                        com.mushi.app.core.ui.theming.MushiIcons.AddSquare,
                        stringResource(Res.string.settings_section_plugins),
                        Color(0xFFFF9800)
                    )
                },
                trailingContent = {
                    MillenniumIcon(
                        imageVector = FeatherIcons.ChevronRight,
                        contentDescription = stringResource(Res.string.settings_plugins_manage_title),
                        tint = AppTheme.colors.onSurfaceVariant
                    )
                },
                onClick = {
                    navigatorCommands.navigateTo(Routes.Plugins)
                }
            )
        }
    )
}
