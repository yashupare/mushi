package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.lazy.LazyListScope
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.modules.settings.components.SwitchSettingCard
import com.mushi.app.modules.support.donationOptions
import com.mushi.app.modules.support.openDonation
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxHeart2
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_section_support
import com.mushi.app.generated.resources.settings_support_reminders_subtitle
import com.mushi.app.generated.resources.settings_support_reminders_title

/** Support settings: the periodic reminder toggle plus the donation destinations. */
internal fun LazyListScope.supportSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    includeHeader: Boolean = true) {
    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_support)
    }
    settingsSectionCard(
        items = buildList {
            add {
                SwitchSettingCard(
                    title = stringResource(Res.string.settings_support_reminders_title),
                    subtitle = stringResource(Res.string.settings_support_reminders_subtitle),
                    icon = {
                        SettingsItemIcon(
                            imageVector = Iconsax.IconsaxHeart2,
                            contentDescription = stringResource(
                                Res.string.settings_support_reminders_title))
                    },
                    checked = settings.supportRemindersEnabled,
                    onCheckedChange = { enabled ->
                        settingsViewModel.updateSettings {
                            copy(supportRemindersEnabled = enabled)
                        }
                    })
            }
            donationOptions.forEach { option ->
                add {
                    SettingCardItem(
                        title = stringResource(option.title),
                        subtitle = stringResource(option.subtitle),
                        icon = {
                            SettingsItemIcon(
                                imageVector = option.icon,
                                contentDescription = stringResource(option.title))
                        },
                        onClick = { openDonation(option) })
                }
            }
        })
}
