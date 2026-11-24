package com.mushi.app.modules.album

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import org.koin.compose.koinInject
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.ui.component.CollectionView
import com.mushi.app.modules.library.playlist.AddToPlaylistPicker
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.album_loading_title
import com.mushi.app.generated.resources.media_unknown_artist

@Composable
fun AlbumScreen(
    albumId: String,
    viewModel: AlbumViewModel,
    audioPlayerQueue: AudioPlayerQueue,
    audioPlayer: AudioPlayerInterface,
    navigationCommands: NavigationCommands
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val jamRoomService: JamRoomService = koinInject()
    val jamActive by jamRoomService.role.map { it != null }
        .collectAsStateWithLifecycle(initialValue = false)
    val isJamGuest by jamRoomService.role.map { it == JamRole.Guest }
        .collectAsStateWithLifecycle(initialValue = false)
    val currentCollectionEntry by audioPlayerQueue.currentCollectionEntryFlow.collectAsStateWithLifecycle()
    val playerState by audioPlayer.playerStateFlow.collectAsStateWithLifecycle()
    val savedAlbumIds by viewModel.savedAlbumIds.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val trackOptionsContext by viewModel.trackOptionsContext.collectAsStateWithLifecycle()
    val showAddToPlaylistPicker by viewModel.showAddToPlaylistPicker.collectAsStateWithLifecycle()

    val dataState = state as? AlbumScreenState.Data
    val album = dataState?.album
    val ownerName =
        album?.artists?.joinToString { it.name }.orEmpty().ifBlank { stringResource(Res.string.media_unknown_artist) }
    val ownerImageURL = album?.artists?.firstOrNull()?.thumbnails?.firstOrNull()?.url
    val firstArtistId = album?.artists?.firstOrNull()?.id
    val artworkUrl = album?.thumbnails?.firstOrNull()?.url.orEmpty()
    val isAlbumPlaying = currentCollectionEntry?.id == albumId &&
        playerState == PlayerState.PLAYING

    val errorMessage = (state as? AlbumScreenState.Error)?.message
    val isLoading = state is AlbumScreenState.Loading && (dataState == null || dataState.tracks.isEmpty())

    CollectionView(
        isPlayLoading = playbackUiState.isPlayLoading,
        isShuffleLoading = playbackUiState.isShuffleLoading,
        isAddToQueueLoading = playbackUiState.isAddToQueueLoading,
        title = album?.title ?: stringResource(Res.string.album_loading_title),
        description = album?.description ?: listOfNotNull(
            album?.albumType?.name?.takeIf { it.isNotBlank() },
            // The source sends a full timestamp ("2015-05-21T00:00:00Z"); show just the year.
            album?.releaseDate?.take(4)?.takeIf { year -> year.length == 4 && year.all { it.isDigit() } }
        ).joinToString(" • "),
        imageURL = artworkUrl,
        ownerName = ownerName,
        ownerImageURL = ownerImageURL,
        onOwnerClick = {
            firstArtistId?.let {
                navigationCommands.navigateTo(Routes.Artist(it))
            }
        },
        onPlay = viewModel::playAlbum,
        onShufflePlay = viewModel::shufflePlayAlbum,
        onAddToQueue = viewModel::addAlbumToQueue,
        isPlaying = isAlbumPlaying,
        isFollowing = savedAlbumIds.contains(albumId),
        isFollowLoading = dataState?.isSaving == true,
        onFollowClick = viewModel::toggleSavedAlbum,
        isLoading = isLoading,
        error = errorMessage,
        onRetry = { viewModel.refresh() },
        tracks = dataState?.tracks ?: emptyList(),
        hasMore = dataState?.nextPagination != null,
        isLoadingNextPage = state is AlbumScreenState.Data.LoadingMore,
        currentTrackId = trackOptionsContext.currentTrackId,
        isCurrentTrackPlaying = playerState == PlayerState.PLAYING,
        onTrackClick = viewModel::playAlbumFromTrack,
        onLoadNextPage = viewModel::loadNextTracksPage,
        onArtistClick = { navigationCommands.navigateTo(Routes.Artist(it.id)) },
        onAlbumClick = { navigationCommands.navigateTo(Routes.Album(it.id)) },
        onTrackOptionsAction = viewModel::handleTrackOptionsAction,
        trackOptionsState = trackOptionsContext::stateFor,
        onBulkDownload = viewModel::downloadTracks,
        onBulkAddToQueue = viewModel::addTracksToQueue,
        onBulkPlayNext = viewModel::playTracksNext,
        onBulkAddToPlaylist = viewModel::showAddToPlaylistPicker,
        onBulkAddToJam = viewModel::addTracksToJam,
        isInJam = jamActive,
        isJamGuest = isJamGuest,
        onAddToJam = viewModel::addTracksToJam,
        trailingContent = {
            AddToPlaylistPicker(
                visible = showAddToPlaylistPicker,
                currentUserId = currentUserId,
                onDismiss = viewModel::dismissAddToPlaylistPicker,
                onPlaylistSelected = viewModel::addTracksToPlaylist)
        })
}
