package com.mushi.app.modules.jam

import com.mushi.app.core.ui.component.MushiButton
import org.spotube.millennium.composables.MillenniumAlertDialog
import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.jam_destination_message
import com.mushi.app.generated.resources.jam_destination_play_here
import com.mushi.app.generated.resources.jam_destination_suggest
import com.mushi.app.generated.resources.jam_destination_title
import com.mushi.app.generated.resources.common_cancel

/**
 * Dialog shown when the user tries to play a collection while a jam session is active.
 * The user picks between playing locally on their device or suggesting it to the jam session.
 */
@Composable
fun PlayDestinationPicker(
    visible: Boolean,
    onDismiss: () -> Unit,
    onPlayLocally: () -> Unit,
    onSuggestToJam: () -> Unit) {
    if (!visible) return

    MillenniumAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.jam_destination_title)) },
        description = { Text(stringResource(Res.string.jam_destination_message)) }) {
        MillenniumButton(
            onClick = onDismiss,
            style = MillenniumButtonStyle.Quiet) {
            Text(stringResource(Res.string.common_cancel))
        }
        MillenniumButton(
            onClick = onPlayLocally,
            style = MillenniumButtonStyle.Secondary) {
            Text(stringResource(Res.string.jam_destination_play_here))
        }
        MushiButton(onClick = onSuggestToJam) {
            Text(stringResource(Res.string.jam_destination_suggest))
        }
    }
}
