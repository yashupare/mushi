package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.lazy.LazyListScope
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SwitchSettingCard
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxRefreshArrow2
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_section_updates
import com.mushi.app.generated.resources.settings_updates_auto_check_subtitle
import com.mushi.app.generated.resources.settings_updates_auto_check_title

internal fun LazyListScope.updatesSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    includeHeader: Boolean = true) {
    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_updates)
    }
    settingsSectionCard(
        items = listOf {
            SwitchSettingCard(
                title = stringResource(Res.string.settings_updates_auto_check_title),
                subtitle = stringResource(Res.string.settings_updates_auto_check_subtitle),
                icon = {
                    SettingsItemIcon(
                        com.mushi.app.core.ui.theming.MushiIcons.Refresh,
                        stringResource(Res.string.settings_updates_auto_check_title)
                    )
                },
                checked = settings.autoCheckForUpdates,
                onCheckedChange = { enabled ->
                    settingsViewModel.updateSettings {
                        copy(autoCheckForUpdates = enabled)
                    }
                }
            )
        }
    )
}

