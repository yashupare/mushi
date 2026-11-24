package com.mushi.app.core.ui.component

import androidx.compose.runtime.Composable
import com.mushi.app.generated.resources.media_playlist
import com.mushi.app.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.playback.CollectionPlaybackHelper
import com.mushi.app.core.remote.CollectionPlaybackKey
import com.mushi.app.core.remote.PlaybackDestinationAction
import com.mushi.app.core.remote.RemoteCollectionType
import com.mushi.app.core.remote.RemotePlaybackController
import com.mushi.app.core.ui.component.cards.PlayableCard
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PlaylistCard(
    playlist: MetadataPlaylist,
    modifier: Modifier = Modifier,
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    playbackHelper: CollectionPlaybackHelper = koinInject(),
    navigationCommands: NavigationCommands = koinInject(),
    remotePlaybackController: RemotePlaybackController = koinInject(),
    listMode: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val currentCollectionEntry by audioPlayerQueue.currentCollectionEntryFlow.collectAsStateWithLifecycle()
    val busyCollections by remotePlaybackController.busyCollections.collectAsStateWithLifecycle()

    PlayableCard(
        title = playlist.title,
        subtitle = playlist.description?.takeIf { it.isNotBlank() }
            ?: stringResource(Res.string.media_playlist),
        imageURL = playlist.thumbnails.firstOrNull()?.url,
        isPlaying = currentCollectionEntry?.id == playlist.id,
        onClick = {
            navigationCommands.navigateTo(Routes.Playlist(playlist.id))
        },
        onPlay = {
            if (audioPlayerQueue.isPlaylistPlaying(playlist.id)) return@PlayableCard
            remotePlaybackController.requestCollectionPlay(
                RemoteCollectionType.Playlist,
                playlist.id,
                playlist.title
            )
        },
        onAddToQueue = {
            remotePlaybackController.requestCollectionAddToQueue(
                RemoteCollectionType.Playlist,
                playlist.id,
                playlist.title
            )
        },
        modifier = modifier,
        sharedElementKey = "playlist_art_${playlist.id}",
        listMode = listMode,
        playLoading = CollectionPlaybackKey(RemoteCollectionType.Playlist, playlist.id, PlaybackDestinationAction.Play) in busyCollections,
        addToQueueLoading = CollectionPlaybackKey(RemoteCollectionType.Playlist, playlist.id, PlaybackDestinationAction.AddToQueue) in busyCollections
    )
}
