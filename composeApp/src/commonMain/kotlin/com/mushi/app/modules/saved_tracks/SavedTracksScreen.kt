package com.mushi.app.modules.saved_tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueCollectionEntry
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import org.koin.compose.koinInject
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.ui.component.CollectionView
import com.mushi.app.modules.library.playlist.AddToPlaylistPicker
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.liked_tracks
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@Composable
fun SavedTracksScreen(
    viewModel: SavedTracksViewModel,
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
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val trackOptionsContext by viewModel.trackOptionsContext.collectAsStateWithLifecycle()
    val showAddToPlaylistPicker by viewModel.showAddToPlaylistPicker.collectAsStateWithLifecycle()

    val dataState = state as? SavedTracksScreenState.Data
    val isPlaying = currentCollectionEntry is QueueCollectionEntry.SavedTracks &&
        playerState == PlayerState.PLAYING

    val errorMessage = (state as? SavedTracksScreenState.Error)?.message
    val isLoading = state is SavedTracksScreenState.Loading && (dataState == null || dataState.tracks.isEmpty())

    CollectionView(
        isPlayLoading = playbackUiState.isPlayLoading,
        isShuffleLoading = playbackUiState.isShuffleLoading,
        isAddToQueueLoading = playbackUiState.isAddToQueueLoading,
        title = stringResource(Res.string.saved_tracks_title),
        description = dataState?.let { pluralStringResource(Res.plurals.library_local_tracks_count, it.totalCount, it.totalCount) } ?: "",
        imageURL = "",
        imageResource = Res.drawable.liked_tracks,
        ownerName = stringResource(Res.string.saved_tracks_owner_you),
        ownerImageURL = null,
        onOwnerClick = {},
        onPlay = viewModel::playSavedTracks,
        onShufflePlay = viewModel::shufflePlaySavedTracks,
        onAddToQueue = viewModel::addSavedTracksToQueue,
        isPlaying = isPlaying,
        isFollowing = false,
        onFollowClick = {},
        showFollowButton = false,
        isLoading = isLoading,
        error = errorMessage,
        onRetry = { viewModel.refresh() },
        tracks = dataState?.tracks ?: emptyList(),
        hasMore = dataState?.nextPagination != null,
        isLoadingNextPage = state is SavedTracksScreenState.Data.LoadingMore,
        currentTrackId = trackOptionsContext.currentTrackId,
        isCurrentTrackPlaying = playerState == PlayerState.PLAYING,
        onTrackClick = viewModel::playSavedTracksFromTrack,
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
