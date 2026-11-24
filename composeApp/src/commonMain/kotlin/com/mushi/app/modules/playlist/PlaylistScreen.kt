package com.mushi.app.modules.playlist


import androidx.compose.ui.Alignment
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.mushi.app.modules.library.playlist.PlaylistFormData
import com.mushi.app.modules.library.playlist.PlaylistFormSheet
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.playlist_add_tracks_button
import com.mushi.app.generated.resources.playlist_loading_title
import com.mushi.app.generated.resources.playlist_owner_unknown

@Composable
fun PlaylistScreen(
    playlistId: String,
    viewModel: PlaylistViewModel,
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
    val savedPlaylistIds by viewModel.savedPlaylistIds.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val trackOptionsContext by viewModel.trackOptionsContext.collectAsStateWithLifecycle()
    val showAddToPlaylistPicker by viewModel.showAddToPlaylistPicker.collectAsStateWithLifecycle()
    val editPlaylistFormState by viewModel.editPlaylistFormState.collectAsStateWithLifecycle()
    var showAddTracksDialog by remember { mutableStateOf(false) }

    val dataState = state as? PlaylistScreenState.Data
    val playlist = dataState?.playlist
    val owner = playlist?.owner
    val ownerName = owner?.displayName ?: owner?.username ?: stringResource(Res.string.playlist_owner_unknown)
    val ownerImageURL = owner?.thumbnails?.firstOrNull()?.url
    val isOwner = currentUserId != null && playlist?.owner?.id == currentUserId
    val artworkUrl = playlist?.thumbnails?.firstOrNull()?.url.orEmpty()
    val isPlaying = currentCollectionEntry?.id == playlistId &&
        playerState == PlayerState.PLAYING

    val errorMessage = (state as? PlaylistScreenState.Error)?.message
    val isLoading = state is PlaylistScreenState.Loading && (dataState == null || dataState.tracks.isEmpty())

    val footerContent: (@Composable () -> Unit)? = if (isOwner && dataState != null) {
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center) {
                MillenniumButton(
                    onClick = { showAddTracksDialog = true },
                    style = MillenniumButtonStyle.Secondary) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MillenniumIcon(
                            imageVector = Iconsax.IconsaxAddSquare,
                            contentDescription = null)
                        Text(stringResource(Res.string.playlist_add_tracks_button))
                    }
                }
            }
        }
    } else null

    CollectionView(
        isPlayLoading = playbackUiState.isPlayLoading,
        isShuffleLoading = playbackUiState.isShuffleLoading,
        isAddToQueueLoading = playbackUiState.isAddToQueueLoading,
        title = playlist?.title ?: stringResource(Res.string.playlist_loading_title),
        description = playlist?.description.orEmpty(),
        imageURL = artworkUrl,
        ownerName = ownerName,
        ownerImageURL = ownerImageURL,
        onOwnerClick = {},
        onPlay = viewModel::playPlaylist,
        onShufflePlay = viewModel::shufflePlayPlaylist,
        onAddToQueue = viewModel::addPlaylistToQueue,
        isPlaying = isPlaying,
        isFollowing = savedPlaylistIds.contains(playlistId),
        isFollowLoading = dataState?.isSaving == true,
        onFollowClick = viewModel::toggleSavedPlaylist,
        showFollowButton = playlistId != "saved_tracks",
        onEdit = if (isOwner) viewModel::showEditPlaylist else null,
        isLoading = isLoading,
        error = errorMessage,
        onRetry = { viewModel.refresh() },
        tracks = dataState?.tracks ?: emptyList(),
        hasMore = dataState?.nextPagination != null,
        isLoadingNextPage = state is PlaylistScreenState.Data.LoadingMore,
        currentTrackId = trackOptionsContext.currentTrackId,
        isCurrentTrackPlaying = playerState == PlayerState.PLAYING,
        onTrackClick = viewModel::playPlaylistFromTrack,
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
        footerContent = footerContent,
        trailingContent = {
            val loadedPlaylist = (dataState as? PlaylistScreenState.Data.Loaded)?.playlist
            PlaylistFormSheet(
                visible = editPlaylistFormState.isVisible,
                formState = editPlaylistFormState,
                onDismiss = viewModel::dismissEditPlaylist,
                onImagePicked = viewModel::onEditPlaylistImagePicked,
                isEditing = true,
                initialData = loadedPlaylist?.let {
                    PlaylistFormData(
                        name = it.title,
                        description = it.description.orEmpty(),
                        isPublic = true,
                        isCollaborating = false,
                        imagePreviewUrl = it.thumbnails.firstOrNull()?.url)
                },
                onSubmit = viewModel::updatePlaylist)

            AddToPlaylistPicker(
                visible = showAddToPlaylistPicker,
                currentUserId = currentUserId,
                onDismiss = viewModel::dismissAddToPlaylistPicker,
                onPlaylistSelected = viewModel::addTracksToPlaylist)

            AddTracksToPlaylistDialog(
                visible = showAddTracksDialog,
                playlistId = playlistId,
                onDismiss = {
                    showAddTracksDialog = false
                    viewModel.refresh()
                })
        })
}
