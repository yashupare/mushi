package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.AppBuildInfo
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.modules.update.UpdateCheckStatus
import com.mushi.app.modules.update.UpdateCheckViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.mushi.app.generated.resources.settings_updates_check_title
import com.mushi.app.generated.resources.settings_updates_current_version
import com.mushi.app.generated.resources.settings_updates_checking
import com.mushi.app.generated.resources.settings_updates_up_to_date
import com.mushi.app.generated.resources.settings_updates_check_failed
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
        items = listOf({
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
        }, { CheckForUpdatesCard() })
    )
}

@Composable
private fun CheckForUpdatesCard() {
    val viewModel = koinViewModel<UpdateCheckViewModel>()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val title = stringResource(Res.string.settings_updates_check_title)
    SettingCardItem(
        title = title,
        subtitle = when (status) {
            UpdateCheckStatus.Idle -> stringResource(Res.string.settings_updates_current_version, AppBuildInfo.VERSION)
            UpdateCheckStatus.Checking -> stringResource(Res.string.settings_updates_checking)
            UpdateCheckStatus.UpToDate -> stringResource(Res.string.settings_updates_up_to_date, AppBuildInfo.VERSION)
            UpdateCheckStatus.Failed -> stringResource(Res.string.settings_updates_check_failed)
        },
        icon = { SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Download, title) },
        onClick = viewModel::check)
}

