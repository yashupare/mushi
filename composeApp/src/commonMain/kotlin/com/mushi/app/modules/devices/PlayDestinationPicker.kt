package com.mushi.app.modules.devices


import org.spotube.millennium.composables.MillenniumDialogHeader
import org.spotube.millennium.composables.MillenniumDialogTitle
import com.mushi.app.core.ui.component.AppDialogClose
import org.spotube.millennium.composables.MillenniumDialogBody
import org.spotube.millennium.composables.MillenniumDialogFooter
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumDialog
import org.spotube.millennium.composables.MillenniumListTile
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.core.remote.ConnectionState
import com.mushi.app.core.remote.PlaybackDestinationAction
import com.mushi.app.core.remote.RemoteControlClient
import com.mushi.app.core.remote.RemotePlaybackController
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxCd
import com.mushi.app.resources.iconsax.IconsaxMirroringScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.devices_destination_action_add_to_queue
import com.mushi.app.generated.resources.devices_destination_action_play
import com.mushi.app.generated.resources.devices_destination_action_play_next
import com.mushi.app.generated.resources.common_cancel
import com.mushi.app.generated.resources.devices_destination_prompt
import com.mushi.app.generated.resources.devices_destination_remote_device
import com.mushi.app.generated.resources.devices_destination_remote_device_host
import com.mushi.app.generated.resources.devices_destination_remote_subtitle
import com.mushi.app.generated.resources.devices_destination_this_device
import com.mushi.app.generated.resources.devices_destination_this_device_subtitle
import com.mushi.app.generated.resources.devices_destination_title

/**
 * Globally hosted dialog shown when the user tries to play / add to queue /
 * play next and there is more than one place it could go (a connected remote
 * device and/or an active jam session). Lets the user choose the destination.
 */
@Composable
fun PlayDestinationPickerHost() {
    val controller = koinInject<RemotePlaybackController>()
    val remoteControlClient = koinInject<RemoteControlClient>()
    val request by controller.pendingRequest.collectAsStateWithLifecycle()
    val connectionState by remoteControlClient.connectionState.collectAsStateWithLifecycle()

    val pendingRequest = request ?: return

    val remoteDeviceName = when (val state = connectionState) {
        is ConnectionState.Connected -> stringResource(Res.string.devices_destination_remote_device_host, state.host)
        else -> stringResource(Res.string.devices_destination_remote_device)
    }

    val actionLabel = when (pendingRequest.action) {
        PlaybackDestinationAction.Play -> stringResource(Res.string.devices_destination_action_play)
        PlaybackDestinationAction.AddToQueue -> stringResource(Res.string.devices_destination_action_add_to_queue)
        PlaybackDestinationAction.PlayNext -> stringResource(Res.string.devices_destination_action_play_next)
    }

    MillenniumDialog(visible = true, onDismissRequest = controller::dismissPicker) {
        MillenniumDialogHeader {
            MillenniumDialogTitle(stringResource(Res.string.devices_destination_title))
            AppDialogClose(controller::dismissPicker)
        }
        MillenniumDialogBody {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(Res.string.devices_destination_prompt, actionLabel, pendingRequest.title),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant)

            MillenniumListTile(
                onClick = controller::playLocally,
                modifier = Modifier.fillMaxWidth(),
                leading = {
                    MillenniumIcon(
                        imageVector = com.mushi.app.core.ui.theming.MushiIcons.Album,
                        contentDescription = null,
                        tint = AppTheme.colors.primary)
                },
                title = {
                    Text(
                        text = stringResource(Res.string.devices_destination_this_device),
                        style = AppTheme.typography.bodyLarge)
                },
                subtitle = {
                    Text(
                        text = stringResource(Res.string.devices_destination_this_device_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant)
                })

            if (connectionState is ConnectionState.Connected) {
                MillenniumListTile(
                    onClick = controller::playOnRemote,
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        MillenniumIcon(
                            imageVector = com.mushi.app.core.ui.theming.MushiIcons.Cast,
                            contentDescription = null,
                            tint = AppTheme.colors.primary)
                    },
                    title = {
                        Text(
                            text = remoteDeviceName,
                            style = AppTheme.typography.bodyLarge)
                    },
                    subtitle = {
                        Text(
                            text = stringResource(Res.string.devices_destination_remote_subtitle),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant)
                    })
            }

        }
        }
        MillenniumDialogFooter {
            MillenniumButton(
                onClick = controller::dismissPicker,
                style = MillenniumButtonStyle.Quiet) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    }
}
