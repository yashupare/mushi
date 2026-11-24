package com.mushi.app.modules.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import com.mushi.app.core.remote.ConnectionState
import com.mushi.app.core.remote.RemoteControlClient

/** Set while this app is remote-controlling another device. */
data class RemoteConnectStatus(val deviceName: String?)

/** Set while this app is in a Group Jam room. */
data class JamConnectStatus(val isHost: Boolean, val listenerCount: Int)

data class ConnectStatusUiState(
    val remote: RemoteConnectStatus? = null,
    val jam: JamConnectStatus? = null)

/** Live remote-control / Group Jam status shown by the Home header and the sidebar. */
class ConnectStatusViewModel(
    remoteControlClient: RemoteControlClient,
    jamRoomService: JamRoomService
) : ViewModel() {
    val uiState: StateFlow<ConnectStatusUiState> = combine(
        remoteControlClient.connectionState,
        jamRoomService.role,
        jamRoomService.participants
    ) { connection, role, participants ->
        ConnectStatusUiState(
            remote = (connection as? ConnectionState.Connected)?.let {
                RemoteConnectStatus(it.deviceName.ifBlank { null })
            },
            jam = role?.let { JamConnectStatus(isHost = it == JamRole.Host, listenerCount = participants.size) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectStatusUiState())
}
