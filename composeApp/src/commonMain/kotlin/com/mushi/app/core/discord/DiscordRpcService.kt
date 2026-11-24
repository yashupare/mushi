package com.mushi.app.core.discord

import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.di.injectLogger
import com.mushi.app.modules.settings.SettingsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import uniffi.compose_app.DiscordRpcClient
import kotlin.time.Clock

class DiscordRpcService(
    private val audioPlayer: AudioPlayerInterface,
    private val settingsProvider: SettingsProvider) : KoinComponent, AutoCloseable {

    private val logger by injectLogger<DiscordRpcService>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var observerJob: Job? = null

    private val clientId = "1176718791388975124"
    private var rpcClient: DiscordRpcClient? = null
    private var lastConnectionAttempt: Long = 0
    private var connectionFailed: Boolean = false
    private val connectionCooldownMs = 30_000L // 30 seconds cooldown between connection attempts

    init {
        start()
    }

    private fun start() {
        logger.d { "Starting Discord RPC service" }
        observerJob?.cancel()
        observerJob = scope.launch {
            combine(
                settingsProvider.settingsState,
                audioPlayer.playerStateFlow,
                audioPlayer.currentMediaItemFlow,
                audioPlayer.positionFlow,
                audioPlayer.durationFlow) { settings, playerState, mediaItem, position, duration ->
                DiscordRpcState(settings?.discordRichPresence == true, playerState, mediaItem, position, duration)
            }.collect { state ->
                handleStateChange(state)
            }
        }
    }

    private fun handleStateChange(state: DiscordRpcState) {
        if (!state.enabled) {
            disconnectClient()
            return
        }

        when (state.playerState) {
            PlayerState.PLAYING -> {
                val mediaItem = state.mediaItem ?: return
                ensureConnected()
                updatePresence(
                    title = mediaItem.title,
                    artist = mediaItem.artist,
                    album = mediaItem.album,
                    coverUrl = mediaItem.coverURL,
                    positionMs = state.position.inWholeMilliseconds,
                    durationMs = state.duration.inWholeMilliseconds)
            }
            PlayerState.PAUSED -> {
                val mediaItem = state.mediaItem ?: return
                ensureConnected()
                updatePresence(
                    title = mediaItem.title,
                    artist = mediaItem.artist,
                    album = mediaItem.album,
                    coverUrl = mediaItem.coverURL,
                    positionMs = state.position.inWholeMilliseconds,
                    durationMs = state.duration.inWholeMilliseconds)
            }
            PlayerState.IDLE, PlayerState.COMPLETED -> {
                clearPresence()
            }
            PlayerState.BUFFERING, PlayerState.READY -> {
            }
        }
    }

    private fun ensureConnected() {
        val client = rpcClient
        if (client != null) {
            try {
                if (client.isConnected()) return
                logger.w { "Discord RPC connection lost, will retry after cooldown" }
                client.disconnect()
                rpcClient = null
                connectionFailed = true
                lastConnectionAttempt = currentTimeMillis()
                return
            } catch (e: Exception) {
                logger.w { "Discord RPC connection check failed, will retry after cooldown" }
                rpcClient = null
                connectionFailed = true
                lastConnectionAttempt = currentTimeMillis()
                return
            }
        }

        // Check cooldown period
        val now = currentTimeMillis()
        if (connectionFailed && (now - lastConnectionAttempt) < connectionCooldownMs) {
            return
        }

        lastConnectionAttempt = now
        try {
            val newClient = DiscordRpcClient(clientId)
            newClient.connect()
            rpcClient = newClient
            connectionFailed = false
            logger.i { "Connected to Discord RPC" }
        } catch (e: Exception) {
            connectionFailed = true
            logger.d { "Discord not available, will retry in ${connectionCooldownMs / 1000}s" }
        }
    }

    private fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    private fun updatePresence(
        title: String,
        artist: String,
        album: String,
        coverUrl: String,
        positionMs: Long,
        durationMs: Long) {
        val client = rpcClient ?: return
        try {
            client.updatePresence(
                title = title,
                artist = artist,
                album = album,
                coverUrl = coverUrl,
                positionMs = positionMs,
                durationMs = durationMs)
        } catch (e: Exception) {
            rpcClient = null
            connectionFailed = true
            lastConnectionAttempt = currentTimeMillis()
            logger.d { "Discord RPC update failed, will retry after cooldown" }
        }
    }

    private fun clearPresence() {
        val client = rpcClient ?: return
        try {
            client.clearPresence()
        } catch (e: Exception) {
            rpcClient = null
            connectionFailed = true
            lastConnectionAttempt = currentTimeMillis()
            logger.d { "Discord RPC clear failed, will retry after cooldown" }
        }
    }

    private fun disconnectClient() {
        try {
            rpcClient?.disconnect()
            rpcClient = null
            logger.d { "Disconnected from Discord RPC" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to disconnect from Discord RPC" }
        }
    }

    override fun close() {
        observerJob?.cancel()
        disconnectClient()
    }

    private data class DiscordRpcState(
        val enabled: Boolean,
        val playerState: PlayerState,
        val mediaItem: com.mushi.app.core.audioplayer.MediaItem?,
        val position: kotlin.time.Duration,
        val duration: kotlin.time.Duration)
}
