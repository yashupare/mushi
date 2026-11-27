package com.mushi.app.modules.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.ui.component.MushiButton
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumAlertDialog
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumProgress

/**
 * Prompts the user that a newer Mushi release is available, and follows the update once asked.
 * [onUpdate] starts it (or opens the downloads page where the app can't install); [onIgnore]
 * persistently silences this release tag; [onWebsite] is the way out when installing fails.
 */
@Composable
fun AppUpdateDialog(
    update: AvailableAppUpdate,
    progress: UpdateProgress,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit,
    onUpdate: () -> Unit,
    onInstall: () -> Unit,
    onWebsite: () -> Unit) {
    MillenniumAlertDialog(
        visible = true,
        // The app is about to close; there is nothing left to dismiss
        onDismissRequest = { if (progress !is UpdateProgress.Installing) onDismiss() },
        title = { Text(stringResource(Res.string.app_update_dialog_title, update.tag)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (progress) {
                    UpdateProgress.Idle -> Notes(update)
                    UpdateProgress.NeedsPermission -> Message(Res.string.app_update_permission)
                    is UpdateProgress.Downloading -> {
                        Message(Res.string.app_update_downloading)
                        MillenniumProgress(
                            progress = progress.fraction,
                            modifier = Modifier.fillMaxWidth().height(4.dp))
                    }

                    UpdateProgress.Ready -> Message(Res.string.app_update_ready)
                    UpdateProgress.Installing -> Message(Res.string.app_update_installing)
                    UpdateProgress.Failed -> Message(Res.string.app_update_failed)
                }
            }
        }) {
        when (progress) {
            UpdateProgress.Idle -> {
                Secondary(Res.string.update_action_ignore, onIgnore)
                Primary(Res.string.update_action_update, onUpdate)
            }

            UpdateProgress.NeedsPermission -> {
                Secondary(Res.string.common_cancel, onDismiss)
                Primary(Res.string.update_action_update, onUpdate)
            }

            is UpdateProgress.Downloading -> Secondary(Res.string.common_cancel, onDismiss)
            UpdateProgress.Ready -> {
                Secondary(Res.string.app_update_later, onDismiss)
                Primary(Res.string.app_update_install_now, onInstall)
            }

            UpdateProgress.Installing -> Unit
            UpdateProgress.Failed -> {
                Secondary(Res.string.app_update_website, onWebsite)
                Primary(Res.string.common_retry, onUpdate)
            }
        }
    }
}

@Composable
private fun Notes(update: AvailableAppUpdate) {
    val lines = releaseNoteLines(update.releaseNotesMarkdown)
    if (lines.isEmpty()) {
        Message(Res.string.app_update_dialog_no_notes)
        return
    }
    Column(
        // Long notes scroll, so the buttons and the title stay on screen
        Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lines.forEach { line ->
            Text(
                line.text,
                style = if (line.heading) AppTheme.typography.titleSmall else AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurface)
        }
    }
}

@Composable
private fun Message(text: org.jetbrains.compose.resources.StringResource) {
    Text(stringResource(text), style = AppTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
}

@Composable
private fun Primary(label: org.jetbrains.compose.resources.StringResource, onClick: () -> Unit) {
    MushiButton(onClick = onClick) {
        Text(stringResource(label))
    }
}

@Composable
private fun Secondary(label: org.jetbrains.compose.resources.StringResource, onClick: () -> Unit) {
    MillenniumButton(onClick = onClick, style = MillenniumButtonStyle.Secondary) {
        Text(stringResource(label))
    }
}
