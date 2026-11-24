package com.mushi.app.core.discovery

import com.appstractive.dnssd.DiscoveryEvent
import com.appstractive.dnssd.NetService
import com.appstractive.dnssd.createNetService
import com.appstractive.dnssd.discoverServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.text.decodeToString

data class DiscoveredDevice(
    val name: String,
    val type: String,
    val host: String,
    val port: Int,
    val deviceId: String) {
    val key: String get() = "$name$type".replace(".", "")
}

sealed interface DiscoveryState {
    /** [resolve] can block for seconds on desktop (JmDNS), so it's a suspend call that runs off the main thread. */
    data class Discovered(val device: DiscoveredDevice, val resolve: suspend () -> Unit) : DiscoveryState
    data class Resolved(val device: DiscoveredDevice) : DiscoveryState
    data class Removed(val device: DiscoveredDevice) : DiscoveryState
}

class DeviceDiscoveryService {
    companion object {
        const val SERVICE_TYPE = "_mushi-ctrl._tcp"
        const val TXT_DEVICE_ID = "deviceId"
    }

    /**
     * Runs on [Dispatchers.IO]: on desktop, cancelling the underlying flow calls the blocking
     * `JmDNS.close()` (it waits up to 5s for its shutdown), which froze the UI whenever the
     * collector was cancelled on the main thread.
     */
    fun discover(): Flow<DiscoveryState> = discoverServices(SERVICE_TYPE).map { event ->
        when (event) {
            is DiscoveryEvent.Discovered -> {
                val device = DiscoveredDevice(
                    name = event.service.name,
                    type = event.service.type,
                    host = event.service.host,
                    port = event.service.port,
                    deviceId = event.service.txt[TXT_DEVICE_ID]?.decodeToString().orEmpty())
                DiscoveryState.Discovered(
                    device = device,
                    resolve = { withContext(Dispatchers.IO) { event.resolve() } })
            }

            is DiscoveryEvent.Resolved -> {
                val device = DiscoveredDevice(
                    name = event.service.name,
                    type = event.service.type,
                    host = event.service.host,
                    port = event.service.port,
                    deviceId = event.service.txt[TXT_DEVICE_ID]?.decodeToString().orEmpty())
                DiscoveryState.Resolved(device = device)
            }

            is DiscoveryEvent.Removed -> {
                val device = DiscoveredDevice(
                    name = event.service.name,
                    type = event.service.type,
                    host = event.service.host,
                    port = event.service.port,
                    deviceId = "")
                DiscoveryState.Removed(device = device)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun advertise(
        name: String,
        port: Int,
        deviceId: String,
        registerTimeoutMs: Long = 5_000): NetService {
        val service = createService(name, port, deviceId)
        service.register(timeoutInMs = registerTimeoutMs)
        return service
    }

    fun createService(
        name: String,
        port: Int,
        deviceId: String): NetService = createNetService(
        type = SERVICE_TYPE,
        name = name,
        port = port,
        addresses = preferredAdvertiseAddress()?.let { listOf(it) },
        txt = mapOf(TXT_DEVICE_ID to deviceId))
}