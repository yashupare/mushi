package com.mushi.app.modules.update

import com.mushi.app.core.ui.component.MushiButton
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import com.composeunstyled.Text
import com.mikepenz.markdown.m3.Markdown
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumAlertDialog
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.app_update_dialog_no_notes
import com.mushi.app.generated.resources.app_update_dialog_title
import com.mushi.app.generated.resources.update_action_ignore
import com.mushi.app.generated.resources.update_action_update

/**
 * Prompts the user that a newer Mushi release is available on GitHub. [onUpdate]
 * should take the user to the downloads page; [onIgnore] persistently silences this
 * specific release tag so the check moves on to the next newer one.
 */
@Composable
fun AppUpdateDialog(
    update: AvailableAppUpdate,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit,
    onUpdate: () -> Unit) {
    MillenniumAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.app_update_dialog_title, update.tag)) },
        content = {
            if (update.releaseNotesMarkdown.isNotBlank()) {
                SelectionContainer {
                    Markdown(content = update.releaseNotesMarkdown)
                }
            } else {
                Text(stringResource(Res.string.app_update_dialog_no_notes))
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
