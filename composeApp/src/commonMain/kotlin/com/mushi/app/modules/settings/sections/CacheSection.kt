package com.mushi.app.modules.settings.sections


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import androidx.compose.foundation.lazy.LazyListScope

import androidx.compose.runtime.Composable
import compose.icons.FeatherIcons
import compose.icons.feathericons.Folder
import compose.icons.feathericons.HardDrive
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.modules.settings.components.SwitchSettingCard
import com.mushi.app.modules.settings.components.TextInputSettingCard
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxDocumentDownload
import com.mushi.app.resources.iconsax.IconsaxFolder
import com.mushi.app.resources.iconsax.IconsaxFolderAdd
import com.mushi.app.resources.iconsax.IconsaxStopCircle
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.cacheSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    includeHeader: Boolean = true) {
    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_caching)
    }
    settingsSectionCard(
        items = listOf(
            {
                SwitchSettingCard(
                    title = stringResource(Res.string.settings_enable_music_caching_title),
                    subtitle = stringResource(Res.string.settings_enable_music_caching_subtitle),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Download,
                            stringResource(Res.string.settings_enable_music_caching_title)
                        )
                    },
                    checked = settings.enableMusicCaching,
                    onCheckedChange = { enabled ->
                        settingsViewModel.updateSettings {
                            copy(enableMusicCaching = enabled)
                        }
                    }
                )
            },
            {
                CacheFolderSettingCard(
                    enabled = settings.enableMusicCaching,
                    folder = settings.cacheFolder,
                    onFolderSelected = { folder ->
                        settingsViewModel.updateSettings {
                            copy(cacheFolder = folder)
                        }
                    }
                )
            },
            {
                val error_whole_number = stringResource(Res.string.settings_error_whole_number)
                val error_cache_range = stringResource(Res.string.settings_error_cache_size_range)

                TextInputSettingCard(
                    enabled = settings.enableMusicCaching,
                    title = stringResource(Res.string.settings_cache_size_limit_title),
                    subtitle = when {
                        settings.cacheSizeLimitMB <= 0L -> stringResource(Res.string.settings_cache_size_limit_unlimited)
                        else -> stringResource(
                            Res.string.settings_cache_size_limit_current,
                            settings.cacheSizeLimitMB
                        )
                    },
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Stop,
                            stringResource(Res.string.settings_cache_size_limit_title)
                        )
                    },
                    value = when {
                        settings.cacheSizeLimitMB <= 0L -> ""
                        else -> settings.cacheSizeLimitMB.toString()
                    },
                    dialogDescription = stringResource(Res.string.settings_cache_size_limit_description),
                    placeholder = stringResource(Res.string.settings_cache_size_limit_placeholder),
                    normalize = { it.trim() },
                    validate = { value ->
                        if (value.isBlank()) null
                        else {
                            val mb = value.toLongOrNull()
                            when {
                                mb == null -> error_whole_number
                                mb < 0L -> error_cache_range
                                else -> null
                            }
                        }
                    },
                    onValueSaved = { value ->
                        settingsViewModel.updateSettings {
                            copy(cacheSizeLimitMB = value.toLongOrNull()?.coerceAtLeast(0L) ?: 0L)
                        }
                    }
                )
            })
    )
}

@Composable
private fun CacheFolderSettingCard(
    enabled: Boolean,
    folder: String?,
    onFolderSelected: (String?) -> Unit) {
    val pickerLauncher = rememberDirectoryPickerLauncher { directory ->
        if (directory != null) {
            val normalized = normalizePath(directory.path)
            if (normalized.isNotEmpty()) {
                onFolderSelected(normalized)
            }
        }
    }

    SettingCardItem(
        title = stringResource(Res.string.settings_cache_folder_title),
        subtitle = folder?.let {
            stringResource(Res.string.settings_cache_folder_current, it)
        } ?: stringResource(Res.string.settings_cache_folder_default),
        icon = {
            SettingsItemIcon(
                com.mushi.app.core.ui.theming.MushiIcons.Folder,
                stringResource(Res.string.settings_cache_folder_title)
            )
        },
        trailingContent = {
            MillenniumIconButton(
                icon = com.mushi.app.core.ui.theming.MushiIcons.FolderAdd,
                contentDescription = stringResource(Res.string.settings_cache_folder_title),
                onClick = { pickerLauncher.launch() },
                enabled = enabled,
                style = MillenniumIconButtonStyle.Quiet)
        },
        onClick = { pickerLauncher.launch() },
        enabled = enabled)
}
