package com.mushi.app.media

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.i18n.getString
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.media_control_like
import com.mushi.app.generated.resources.media_control_shuffle_off
import com.mushi.app.generated.resources.media_control_shuffle_on
import com.mushi.app.generated.resources.media_control_unlike
import com.mushi.app.modules.saved_tracks.SavedTracksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The extra buttons on the lock screen and notification player: like (for streaming tracks
 * while signed in) and shuffle. Android draws that player itself; apps can only choose its
 * buttons, artwork and text.
 */
@OptIn(UnstableApi::class)
internal class LockScreenControls(
    private val session: MediaSession,
    private val audioPlayer: AudioPlayerInterface,
    private val audioPlayerQueue: AudioPlayerQueue,
    private val savedTracks: SavedTracksRepository,
    private val scope: CoroutineScope,
) {
    private val currentTrackId = audioPlayerQueue.currentQueueEntryFlow
        .map { (it as? QueueEntry.StreamingTrack)?.track?.id }
        .distinctUntilChanged()

    fun start() {
        // Looks up whether each new track is liked; the answer lands in savedTracksIdsFlow.
        scope.launch {
            currentTrackId.collectLatest { id ->
                if (id != null) {
                    runCatching { savedTracks.isSavedTracks(listOf(id)) }
                        .onFailure { Log.d(TAG, "Liked state unavailable", it) }
                }
            }
        }
        scope.launch {
            combine(
                currentTrackId,
                savedTracks.savedTracksIdsFlow,
                audioPlayer.shuffleModeFlow
            ) { id, savedIds, shuffle -> Triple(id, id != null && id in savedIds, shuffle) }
                .distinctUntilChanged()
                .collectLatest { (id, liked, shuffle) -> publish(canLike = id != null, liked, shuffle) }
        }
    }

    private suspend fun publish(canLike: Boolean, liked: Boolean, shuffle: Boolean) {
        val buttons = buildList {
            if (canLike) {
                add(
                    CommandButton.Builder(
                        if (liked) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED)
                        .setDisplayName(getString(if (liked) Res.string.media_control_unlike else Res.string.media_control_like))
                        .setSessionCommand(LIKE)
                        .setSlots(CommandButton.SLOT_OVERFLOW)
                        .build())
            }
            add(
                CommandButton.Builder(
                    if (shuffle) CommandButton.ICON_SHUFFLE_ON else CommandButton.ICON_SHUFFLE_OFF)
                    .setDisplayName(getString(if (shuffle) Res.string.media_control_shuffle_off else Res.string.media_control_shuffle_on))
                    .setSessionCommand(SHUFFLE)
                    .setSlots(CommandButton.SLOT_OVERFLOW)
                    .build())
        }
        withContext(Dispatchers.Main) {
            session.setMediaButtonPreferences(ImmutableList.copyOf(buttons))
        }
    }

    /** Handles a tap on one of the buttons above; false for commands that aren't ours. */
    fun handle(command: SessionCommand): Boolean {
        when (command.customAction) {
            ACTION_LIKE -> scope.launch {
                val id = (audioPlayerQueue.currentQueueEntryFlow.value as? QueueEntry.StreamingTrack)
                    ?.track?.id ?: return@launch
                runCatching {
                    if (id in savedTracks.savedTracksIdsFlow.value) savedTracks.removeSavedTracks(listOf(id))
                    else savedTracks.saveTracks(listOf(id))
                }.onFailure { Log.w(TAG, "Like from the lock screen failed", it) }
            }
            ACTION_SHUFFLE -> scope.launch {
                runCatching { audioPlayer.shuffle(!audioPlayer.shuffleModeFlow.value) }
                    .onFailure { Log.w(TAG, "Shuffle from the lock screen failed", it) }
            }
            else -> return false
        }
        return true
    }

    companion object {
        private const val TAG = "LockScreenControls"
        private const val ACTION_LIKE = "com.mushi.app.LIKE"
        private const val ACTION_SHUFFLE = "com.mushi.app.SHUFFLE"
        val LIKE = SessionCommand(ACTION_LIKE, Bundle.EMPTY)
        val SHUFFLE = SessionCommand(ACTION_SHUFFLE, Bundle.EMPTY)
        val SUCCESS = SessionResult(SessionResult.RESULT_SUCCESS)
    }
}
