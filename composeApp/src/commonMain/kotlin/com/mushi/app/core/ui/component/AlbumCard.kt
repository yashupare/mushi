package com.mushi.app.core.ui.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.playback.CollectionPlaybackHelper
import com.mushi.app.core.remote.CollectionPlaybackKey
import com.mushi.app.core.remote.PlaybackDestinationAction
import com.mushi.app.core.remote.RemoteCollectionType
import com.mushi.app.core.remote.RemotePlaybackController
import com.mushi.app.core.ui.component.cards.PlayableCard
import org.koin.compose.koinInject

@Composable
fun AlbumCard(
    album: MetadataAlbum,
    modifier: Modifier = Modifier,
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    playbackHelper: CollectionPlaybackHelper = koinInject(),
    navigationCommands: NavigationCommands = koinInject(),
    remotePlaybackController: RemotePlaybackController = koinInject(),
    listMode: Boolean = false
) {
    val currentCollectionEntry by audioPlayerQueue.currentCollectionEntryFlow.collectAsStateWithLifecycle()
    val busyCollections by remotePlaybackController.busyCollections.collectAsStateWithLifecycle()

    PlayableCard(
        title = album.title,
        subtitle = album.description
            ?: "${album.albumType} • ${album.artists.joinToString { it.name }}",
        imageURL = album.thumbnails.firstOrNull()?.url,
        isPlaying = currentCollectionEntry?.id == album.id,
        onClick = {
            navigationCommands.navigateTo(Routes.Album(album.id))
        },
        onPlay = {
            if (currentCollectionEntry?.id == album.id) return@PlayableCard
            remotePlaybackController.requestCollectionPlay(
                RemoteCollectionType.Album,
                album.id,
                album.title
            )
        },
        onAddToQueue = {
            remotePlaybackController.requestCollectionAddToQueue(
                RemoteCollectionType.Album,
                album.id,
                album.title
            )
        },
        modifier = if (listMode) modifier else modifier.width(160.dp),
        sharedElementKey = "album_art_${album.id}",
        listMode = listMode,
        playLoading = CollectionPlaybackKey(RemoteCollectionType.Album, album.id, PlaybackDestinationAction.Play) in busyCollections,
        addToQueueLoading = CollectionPlaybackKey(RemoteCollectionType.Album, album.id, PlaybackDestinationAction.AddToQueue) in busyCollections
    )
}
