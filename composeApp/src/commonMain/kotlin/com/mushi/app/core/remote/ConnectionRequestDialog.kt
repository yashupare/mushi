package com.mushi.app.core.remote

import com.mushi.app.core.ui.component.MushiButton
import org.spotube.millennium.composables.MillenniumAlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.remote_request_allow
import com.mushi.app.generated.resources.remote_request_allow_always
import com.mushi.app.generated.resources.remote_request_deny
import com.mushi.app.generated.resources.remote_request_message
import com.mushi.app.generated.resources.remote_request_title

@Composable
fun ConnectionRequestDialogHost() {
    val handler: RemoteControlHandler = koinInject()
    var pendingRequest by remember { mutableStateOf<ConnectionRequest?>(null) }

    LaunchedEffect(handler) {
        handler.incomingConnectionRequests.collect { request ->
            pendingRequest = request
        }
    }

    pendingRequest?.let { request ->
        MillenniumAlertDialog(
            visible = true,
            onDismissRequest = {
                handler.resolveConnectionRequest(request.sessionId, ConnectionRequestResponse.Deny)
                pendingRequest = null
            },
            title = { Text(stringResource(Res.string.remote_request_title)) },
            description = {
                Text(stringResource(Res.string.remote_request_message, request.deviceName))
            },
            ) {
                MillenniumButton(
                    onClick = {
                        handler.resolveConnectionRequest(request.sessionId, ConnectionRequestResponse.AllowAlways)
                        pendingRequest = null
                    },
                    style = MillenniumButtonStyle.Quiet) {
                    Text(stringResource(Res.string.remote_request_allow_always))
                }
                MillenniumButton(
                    onClick = {
                        handler.resolveConnectionRequest(request.sessionId, ConnectionRequestResponse.Deny)
                        pendingRequest = null
                    },
                    style = MillenniumButtonStyle.Secondary) {
                    Text(stringResource(Res.string.remote_request_deny))
                }
                MushiButton(
                    onClick = {
                        handler.resolveConnectionRequest(request.sessionId, ConnectionRequestResponse.Allow)
                        pendingRequest = null
                    }) {
                    Text(stringResource(Res.string.remote_request_allow))
                }
            }
    }
}
