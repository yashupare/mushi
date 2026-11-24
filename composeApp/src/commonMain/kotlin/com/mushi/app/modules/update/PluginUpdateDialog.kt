package com.mushi.app.modules.update

import com.mushi.app.core.ui.component.MushiButton
import org.spotube.millennium.composables.MillenniumAlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.selection.SelectionContainer
import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.plugin_update_dialog_current_version
import com.mushi.app.generated.resources.plugin_update_dialog_no_notes
import com.mushi.app.generated.resources.plugin_update_dialog_title
import com.mushi.app.generated.resources.update_action_ignore
import com.mushi.app.generated.resources.update_action_update

/**
 * Prompts the user that a newer version of one of their installed plugins is
 * available. [onUpdate] should download and install the update; [onIgnore]
 * persistently silences this specific plugin version so the check moves on to the
 * next newer one (or another plugin).
 */
@Composable
fun PluginUpdateDialog(
    update: AvailablePluginUpdate,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit,
    onUpdate: () -> Unit) {
    MillenniumAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        title = {
            Text(
                stringResource(
                    Res.string.plugin_update_dialog_title,
                    update.pluginName,
                    update.info.latestVersion))
        },
        description = {
            Text(stringResource(Res.string.plugin_update_dialog_current_version, update.currentVersion))
        },
        content = {
            if (update.info.releaseNotes.isNotBlank()) {
                SelectionContainer {
                    Markdown(content = update.info.releaseNotes)
                }
            } else {
                Text(stringResource(Res.string.plugin_update_dialog_no_notes))
            }
        }) {
        MillenniumButton(
            onClick = onIgnore,
            style = MillenniumButtonStyle.Secondary) {
            Text(stringResource(Res.string.update_action_ignore))
        }
        MushiButton(onClick = onUpdate) {
            Text(stringResource(Res.string.update_action_update))
        }
    }
}
