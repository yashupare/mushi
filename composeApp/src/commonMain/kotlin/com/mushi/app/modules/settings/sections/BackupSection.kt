package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.settings.BackupStatus
import com.mushi.app.modules.settings.BackupViewModel
import com.mushi.app.modules.settings.components.SettingCardItem
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

internal fun LazyListScope.backupSection(includeHeader: Boolean = true) {
    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_backup)
    }
    settingsSectionCard(items = listOf({ SaveBackupCard() }, { RestoreBackupCard() }))
}

@Composable
private fun SaveBackupCard() {
    val viewModel = koinViewModel<BackupViewModel>()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val saver = rememberFileSaverLauncher(dialogSettings = FileKitDialogSettings.createDefault()) { file ->
        file?.let(viewModel::saveTo)
    }
    val title = stringResource(Res.string.backup_save_title)

    SettingCardItem(
        title = title,
        subtitle = when (status) {
            BackupStatus.Saved -> stringResource(Res.string.backup_saved)
            BackupStatus.SaveFailed -> stringResource(Res.string.backup_save_failed)
            else -> stringResource(Res.string.backup_save_subtitle)
        },
        icon = { SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Download, title) },
        onClick = { saver.launch("mushi-backup", "json") })
}

@Composable
private fun RestoreBackupCard() {
    val viewModel = koinViewModel<BackupViewModel>()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val picker = rememberFilePickerLauncher(type = FileKitType.File("json")) { file ->
        file?.let(viewModel::restoreFrom)
    }
    val title = stringResource(Res.string.backup_restore_title)

    SettingCardItem(
        title = title,
        subtitle = when (status) {
            BackupStatus.Restored -> stringResource(Res.string.backup_restored)
            BackupStatus.NotABackup -> stringResource(Res.string.backup_not_a_backup)
            BackupStatus.RestoreFailed -> stringResource(Res.string.backup_restore_failed)
            else -> stringResource(Res.string.backup_restore_subtitle)
        },
        icon = { SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Refresh, title) },
        onClick = { picker.launch() })
}
