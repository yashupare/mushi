package com.mushi.app.modules.settings.sections


import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import androidx.compose.foundation.lazy.LazyListScope

import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.LocalMediaFoldersViewModel
import com.mushi.app.modules.settings.components.LocalMediaFoldersDialogHost
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import com.mushi.app.modules.settings.components.SelectionSettingCard
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.resources.iconsax.IconsaxFolder
import com.mushi.app.resources.iconsax.IconsaxFolderAdd
import com.mushi.app.resources.iconsax.IconsaxMusicPlay
import com.mushi.app.resources.iconsax.IconsaxVerticalSetting
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_download_folder_current
import com.mushi.app.generated.resources.settings_download_folder_default_path
import com.mushi.app.generated.resources.settings_download_folder_title
import com.mushi.app.generated.resources.settings_download_format_subtitle_current
import com.mushi.app.generated.resources.settings_download_format_title
import com.mushi.app.generated.resources.settings_download_quality_title
import com.mushi.app.generated.resources.settings_local_media_folders_manage
import com.mushi.app.generated.resources.settings_local_media_folders_subtitle_current
import com.mushi.app.generated.resources.settings_local_media_folders_subtitle_empty
import com.mushi.app.generated.resources.settings_local_media_folders_title
import com.mushi.app.generated.resources.settings_section_downloads
import com.mushi.app.generated.resources.settings_subtitle_current

internal fun LazyListScope.downloadsSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    includeHeader: Boolean = true
) {
    val downloadFormats = availableAudioFormats(settings.downloadMusicFormat, downloadFormatPresets)
    val downloadQualities = availableAudioQualities(
        format = settings.downloadMusicFormat,
        current = settings.downloadMusicQuality
    )

    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_downloads)
    }
    settingsSectionCard(
        items = listOf(
            {
                DownloadFolderSettingCard(
                    folder = settings.overloadedDownloadFolder,
                    defaultFolder = settingsViewModel.defaultDownloadFolder,
                    onFolderSelected = { folder ->
                        settingsViewModel.updateSettings {
                            copy(overloadedDownloadFolder = folder)
                        }
                    }
                )
            },
            {
                LocalMediaFoldersSettingCard()
            },
            {
                SelectionSettingCard(
                    title = stringResource(Res.string.settings_download_format_title),
                    subtitle = stringResource(
                        Res.string.settings_download_format_subtitle_current,
                        settings.downloadMusicFormat.displayLabel()
                    ),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.MusicNote,
                            stringResource(Res.string.settings_download_format_title)
                        )
                    },
                    selectedOption = settings.downloadMusicFormat,
                    options = downloadFormats,
                    optionLabel = { it.displayLabel() },
                    onOptionSelected = { format ->
                        settingsViewModel.updateSettings {
                            copy(
                                downloadMusicFormat = format,
                                downloadMusicQuality = format.resolveQuality(downloadMusicQuality)
                            )
                        }
                    }
                )
            },
            {
                SelectionSettingCard(
                    title = stringResource(Res.string.settings_download_quality_title),
                    subtitle = stringResource(
                        Res.string.settings_subtitle_current,
                        settings.downloadMusicQuality.displayLabel()
                    ),
                    icon = {
                        SettingsItemIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Tune,
                            stringResource(Res.string.settings_download_quality_title)
                        )
                    },
                    selectedOption = settings.downloadMusicQuality,
                    options = downloadQualities,
                    optionLabel = { it.displayLabel() },
                    onOptionSelected = { quality ->
                        settingsViewModel.updateSettings {
                            copy(downloadMusicQuality = quality)
                        }
                    }
                )
            })
    )
}

@Composable
private fun DownloadFolderSettingCard(
    folder: String?,
    defaultFolder: String,
    onFolderSelected: (String?) -> Unit
) {
    val pickerLauncher = rememberDirectoryPickerLauncher { directory ->
        if (directory != null) {
            val normalized = normalizePath(directory.path)
            if (normalized.isNotEmpty()) {
                onFolderSelected(normalized)
            }
        }
    }

    SettingCardItem(
        title = stringResource(Res.string.settings_download_folder_title),
        subtitle = folder?.let {
            stringResource(Res.string.settings_download_folder_current, it)
        } ?: stringResource(Res.string.settings_download_folder_default_path, defaultFolder),
        icon = {
            SettingsItemIcon(
                com.mushi.app.core.ui.theming.MushiIcons.Download,
                stringResource(Res.string.settings_download_folder_title)
            )
        },
        trailingContent = {
            MillenniumIconButton(
                icon = com.mushi.app.core.ui.theming.MushiIcons.FolderAdd,
                contentDescription = stringResource(Res.string.settings_download_folder_title),
                onClick = { pickerLauncher.launch() },
                style = MillenniumIconButtonStyle.Quiet
            )
        },
        onClick = {
            pickerLauncher.launch()
        }
    )
}

@Composable
private fun LocalMediaFoldersSettingCard(
    viewModel: LocalMediaFoldersViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val subtitlePreview = state.singleConfiguredPath ?: if (state.configuredCount == 0) {
        stringResource(Res.string.settings_local_media_folders_subtitle_empty)
    } else stringResource(
        Res.string.settings_local_media_folders_subtitle_current,
        state.configuredCount
    )

    SettingCardItem(
        title = stringResource(Res.string.settings_local_media_folders_title),
        subtitle = subtitlePreview,
        icon = {
            SettingsItemIcon(
                com.mushi.app.core.ui.theming.MushiIcons.Folder,
                stringResource(Res.string.settings_local_media_folders_title)
            )
        },
        trailingContent = {
            MillenniumButton(
                onClick = viewModel::open,
                style = MillenniumButtonStyle.Secondary
            ) {
                Text(stringResource(Res.string.settings_local_media_folders_manage))
            }
        },
        onClick = viewModel::open
    )

    LocalMediaFoldersDialogHost(viewModel)
}
