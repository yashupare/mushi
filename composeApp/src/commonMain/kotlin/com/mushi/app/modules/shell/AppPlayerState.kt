package com.mushi.app.modules.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.LoopState
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueEntry
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.player_fallback_artists
import com.mushi.app.generated.resources.player_fallback_title
import com.mushi.app.generated.resources.player_unknown_album

internal data class PlayerArtist(
    val id: String?,
    val name: String
)

internal data class PlayerUiState(
    val queue: List<QueueEntry> = emptyList(),
    val currentQueueEntry: QueueEntry? = null,
    val title: String = "No-op track title",
    val artists: String = "No-op artists",
    val artistList: List<PlayerArtist> = emptyList(),
    val album: String = "",
    val actualDuration: Duration = 0.milliseconds,
    val metadataDuration: Duration = 0.milliseconds,
    val playerState: PlayerState = PlayerState.IDLE,
    val loopState: LoopState = LoopState.NONE,
    val isShuffling: Boolean = false,
    val volume: Float = 0f
) {
    val isPlaying: Boolean get() = playerState == PlayerState.PLAYING

    val coverModel: Any? get() = currentQueueEntry?.artworkModel
    val displayDuration: Duration
        get() = if (actualDuration.inWholeMilliseconds > 0L) actualDuration else metadataDuration
    val seekDuration: Duration
        get() = if (actualDuration.inWholeMilliseconds > 0L) actualDuration else Duration.ZERO

    fun toDebugString(): String {
        return """PlayerUiState(
            queue=${queue.map { it.displayTitle() }},
            currentQueueEntry=${currentQueueEntry?.displayTitle()},
            title='$title', artists='$artists',
            album='$album', coverModel=${coverModel?.let { it::class.simpleName }},
            actualDuration=$actualDuration, metadataDuration=$metadataDuration,
            playerState=$playerState, loopState=$loopState, isShuffling=$isShuffling, volume=$volume
          )
           """.trimIndent()
    }
}

/**
 * The playback position. Kept out of [PlayerUiState] on purpose: it ticks four times a second
 * while playing, and if it were part of the state every tick would recompose each player in
 * full (the full player stays composed even while collapsed). Pass the [State] down to the small
 * pieces that show it ([PlayerSeekBar], [MiniPlayerProgress]) and read it there.
 */
@Composable
internal fun rememberPlayerPosition(audioPlayer: AudioPlayerInterface): State<Duration> =
    audioPlayer.positionFlow.collectAsState()

@Composable
internal fun rememberPlayerUiState(
    audioPlayer: AudioPlayerInterface,
    audioPlayerQueue: AudioPlayerQueue
): PlayerUiState {
    val queue by audioPlayerQueue.queueFlow.collectAsState(initial = emptyList())
    val currentQueueEntry by audioPlayerQueue.currentQueueEntryFlow.collectAsState(initial = null)
    val playerState by audioPlayer.playerStateFlow.collectAsState()
    val duration by audioPlayer.durationFlow.collectAsState()
    val loopState by audioPlayer.loopStateFlow.collectAsState()
    val isShuffling by audioPlayer.shuffleModeFlow.collectAsState()
    val volume by audioPlayer.volumeFlow.collectAsState()
    val metadataDuration = (currentQueueEntry?.durationInMilliseconds() ?: 0L).milliseconds
    val fallbackTitle = stringResource(Res.string.player_fallback_title)
    val fallbackArtists = stringResource(Res.string.player_fallback_artists)
    val unknownAlbum = stringResource(Res.string.player_unknown_album)

    val state = PlayerUiState(
        queue = queue,
        currentQueueEntry = currentQueueEntry,
        title = currentQueueEntry?.displayTitle().orEmpty().ifBlank { fallbackTitle },
        artists = currentQueueEntry?.displayArtists().orEmpty().ifBlank { fallbackArtists },
        artistList = currentQueueEntry?.displayArtistList().orEmpty(),
        album = currentQueueEntry?.displayAlbum(unknownAlbum).orEmpty(),
        actualDuration = duration,
        metadataDuration = metadataDuration,
        playerState = playerState,
        loopState = loopState,
        isShuffling = isShuffling,
        volume = volume
    )

//    val logger = rememberLogger<PlayerUiState>()
//    logger.d { state.toDebugString() }
    return state
}

private fun QueueEntry.displayTitle(): String {
    return when (this) {
        is QueueEntry.StreamingTrack -> track.title
        is QueueEntry.LocalTrack -> name
    }
}

private fun QueueEntry.displayArtists(): String {
    return when (this) {
        is QueueEntry.StreamingTrack -> track.artists.joinToString(", ") { artist -> artist.name }
        is QueueEntry.LocalTrack -> artists.joinToString(", ")
    }
}

private fun QueueEntry.displayArtistList(): List<PlayerArtist> {
    return when (this) {
        is QueueEntry.StreamingTrack -> track.artists.map { PlayerArtist(it.id, it.name) }
        // Local tracks have no artist ids, so they are shown but not navigable.
        is QueueEntry.LocalTrack -> artists.map { PlayerArtist(id = null, name = it) }
    }
}

private fun QueueEntry.displayAlbum(unknownAlbum: String): String {
    return when (this) {
        is QueueEntry.StreamingTrack -> track.album?.title ?: unknownAlbum
        is QueueEntry.LocalTrack -> album.orEmpty()
    }
}

private fun QueueEntry.durationInMilliseconds(): Long {
    return when (this) {
        is QueueEntry.StreamingTrack -> track.durationMs
        is QueueEntry.LocalTrack -> duration
    }
}
