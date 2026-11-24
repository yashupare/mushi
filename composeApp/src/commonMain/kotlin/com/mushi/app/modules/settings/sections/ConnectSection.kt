package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.graphics.Color
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumIcon
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxMirrorScreenRegular
import com.mushi.app.resources.iconsax.IconsaxSoundTwotone
import com.mushi.app.generated.resources.*

/**
 * Shortcuts to Devices and Group Jam. They used to sit in Home's top bar; the top bar now holds
 * only the profile button, so they live at the top of Settings, the screen it opens.
 */
internal fun LazyListScope.connectSection(
    navigatorCommands: NavigationCommands
) {
    settingsSectionHeader(Res.string.nav_group_connect)
    settingsSectionCard(
        items = listOf(
            {
                SettingCardItem(
                    title = stringResource(Res.string.nav_devices),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Cast,
                            stringResource(Res.string.nav_devices),
                            Color(0xFF8E8E93)
                        )
                    },
                    trailingContent = {
                        MillenniumIcon(
                            imageVector = FeatherIcons.ChevronRight,
                            contentDescription = stringResource(Res.string.nav_devices),
                            tint = AppTheme.colors.onSurfaceVariant
                        )
                    },
                    onClick = { navigatorCommands.navigateTo(Routes.Devices) }
                )
            },
            {
                SettingCardItem(
                    title = stringResource(Res.string.jam_screen_title),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Waveform,
                            stringResource(Res.string.jam_screen_title),
                            Color(0xFF8E8E93)
                        )
                    },
                    trailingContent = {
                        MillenniumIcon(
                            imageVector = FeatherIcons.ChevronRight,
                            contentDescription = stringResource(Res.string.jam_screen_title),
                            tint = AppTheme.colors.onSurfaceVariant
                        )
                    },
                    onClick = { navigatorCommands.navigateTo(Routes.Jam) }
                )
            }
        )
    )
}
