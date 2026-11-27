package com.mushi.app.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/** Whether the device has a network, for telling "you're offline" apart from other failures. */
abstract class NetworkMonitor(initiallyOnline: Boolean) {
    private val _online = MutableStateFlow(initiallyOnline)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    private val _reconnects = MutableStateFlow(0)
    /** Goes up each time the network comes back, so a failed screen knows to load again. */
    val reconnects: StateFlow<Int> = _reconnects.asStateFlow()

    protected fun onNetworkChanged(isOnline: Boolean) {
        val wasOnline = _online.getAndUpdate { isOnline }
        if (isOnline && !wasOnline) _reconnects.update { it + 1 }
    }
}

/** For platforms that don't report the network: failures are told apart by their cause alone. */
class AlwaysOnlineMonitor : NetworkMonitor(initiallyOnline = true)

private val NETWORK_FAILURES = listOf(
    "UnknownHost", "Unable to resolve host", "No address associated with hostname",
    "ConnectException", "Failed to connect", "SocketTimeout", "timed out", "timeout",
    "Network is unreachable", "Software caused connection abort", "Connection reset", "SSLException")

/** True when an error's text says the request never got through. */
fun isNetworkError(error: String?): Boolean =
    error != null && NETWORK_FAILURES.any { error.contains(it, ignoreCase = true) }
