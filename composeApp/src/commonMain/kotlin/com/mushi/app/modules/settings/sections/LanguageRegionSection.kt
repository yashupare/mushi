package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.mushi.app.modules.settings.CountryCode
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.SupportedLanguages
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SelectionSettingCard
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxGlobe
import com.mushi.app.resources.iconsax.IconsaxLanguageSquare
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_country_subtitle_current
import com.mushi.app.generated.resources.settings_country_title
import com.mushi.app.generated.resources.settings_language_subtitle_current
import com.mushi.app.generated.resources.settings_language_system_default
import com.mushi.app.generated.resources.settings_language_title
import com.mushi.app.generated.resources.settings_option_name_and_code
import com.mushi.app.generated.resources.settings_section_language_region

internal fun LazyListScope.languageRegionSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    includeHeader: Boolean = true) {
    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_language_region)
    }
    settingsSectionCard(
        items = listOf(
            {
                SelectionSettingCard(
                    title = stringResource(Res.string.settings_language_title),
                    subtitle = stringResource(
                        Res.string.settings_language_subtitle_current,
                        languageLabel(settings.language)
                    ),
                    icon = {
                        SettingsItemIcon(
                            Iconsax.IconsaxLanguageSquare,
                            stringResource(Res.string.settings_language_title)
                        )
                    },
                    selectedOption = settings.language,
                    options = SupportedLanguages.entries,
                    optionLabel = { languageLabel(it) },
                    onOptionSelected = { value ->
                        settingsViewModel.updateSettings {
                            copy(language = value)
                        }
                    },
                    filter = { item, query -> item.label.contains(query, ignoreCase = true) })
            },
            {
                SelectionSettingCard(
                    title = stringResource(Res.string.settings_country_title),
                    subtitle = stringResource(
                        Res.string.settings_country_subtitle_current,
                        settings.country.displayName
                    ),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Globe,
                            stringResource(Res.string.settings_country_title)
                        )
                    },
                    selectedOption = settings.country,
                    options = CountryCode.entries,
                    optionLabel = {
                        stringResource(Res.string.settings_option_name_and_code, it.displayName, it.code)
                    },
                    onOptionSelected = { value ->
                        settingsViewModel.updateSettings {
                            copy(country = value)
                        }
                    },
                    filter = { item, query -> item.label.contains(query, ignoreCase = true) })
            })
    )
}

@Composable
private fun languageLabel(language: SupportedLanguages): String =
    if (language == SupportedLanguages.SYSTEM) {
        stringResource(Res.string.settings_language_system_default)
    } else {
        stringResource(Res.string.settings_option_name_and_code, language.displayName, language.languageTag!!)
    }

