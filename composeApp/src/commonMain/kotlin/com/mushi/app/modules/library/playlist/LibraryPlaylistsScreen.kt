package com.mushi.app.modules.library.playlist

import com.mushi.app.resources.iconsax.IconsaxMusicPlaylist
import com.mushi.app.resources.iconsax.IconsaxSearch
import com.mushi.app.resources.iconsax.Iconsax
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import com.mushi.app.core.ui.component.ErrorDisplay
import com.mushi.app.core.ui.component.LikedTracksCard
import com.mushi.app.core.ui.component.PlaylistCard
import com.mushi.app.core.ui.component.cards.PlayableCard
import com.mushi.app.modules.library.LibraryMediaCollection
import com.mushi.app.modules.library.LibraryViewMode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.mushi.app.generated.resources.*

@Composable
fun LibraryPlaylistsScreen(
    modifier: Modifier = Modifier,
    showCreatePlaylist: Boolean = false,
    viewMode: LibraryViewMode = LibraryViewMode.Grid,
    viewModel: LibraryPlaylistsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playlistFormState by viewModel.playlistFormState.collectAsStateWithLifecycle()
    Column(modifier) {
        when (val current = state) {
            is LibraryPlaylistsState.Error -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorDisplay(errorMessage = current.message, onRetry = viewModel::refresh)
            }

            else -> {
                val data = current as? LibraryPlaylistsState.Data
                if (data != null && data.items.isEmpty() && current !is LibraryPlaylistsState.Data.LoadingMore) {
                    com.mushi.app.core.ui.component.cards.MediaEmptyState(
                    icon = if (data.query.isNotBlank()) com.mushi.app.core.ui.theming.MushiIcons.Search
                        else com.mushi.app.core.ui.theming.MushiIcons.Playlist,
                    title = if (data.query.isNotBlank()) stringResource(Res.string.library_playlists_no_results, data.query)
                        else stringResource(Res.string.library_playlists_empty),
                    description = if (data.query.isBlank()) stringResource(Res.string.library_playlists_empty_hint) else null)
                } else {
                    LibraryMediaCollection<MetadataPlaylist>(
                        items = data?.items.orEmpty(), itemKey = { it.id }, viewMode = viewMode,
                        loading = current is LibraryPlaylistsState.Loading,
                        loadingMore = current is LibraryPlaylistsState.Data.LoadingMore,
                        onViewportChanged = viewModel::onItemsVisible,
                        header = { list ->
                            LikedTracksCard(
                                Modifier.fillMaxWidth(),
                                listMode = list
                            )
                        },
                        itemContent = { item, list ->
                            PlaylistCard(
                                item,
                                Modifier.fillMaxWidth(),
                                listMode = list
                            )
                        },
                        skeleton = { list ->
                            PlayableCard(
                                stringResource(Res.string.media_preview_title),
                                stringResource(Res.string.media_preview_subtitle),
                                modifier = Modifier.fillMaxWidth(),
                                listMode = list
                            )
                        }
                    )
                }
            }
        }
        PlaylistFormSheet(
            visible = showCreatePlaylist,
            formState = playlistFormState,
            onDismiss = viewModel::dismissCreatePlaylist,
            onSubmit = viewModel::createPlaylist,
            onImagePicked = viewModel::onPlaylistFormImagePicked
        )
    }
}
