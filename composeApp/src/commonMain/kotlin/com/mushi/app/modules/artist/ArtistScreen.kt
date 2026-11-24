package com.mushi.app.modules.artist


import com.mushi.app.core.ui.component.MushiButton
import com.mushi.app.core.ui.misc.itemsWithUniqueKeys
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumCard
import org.spotube.millennium.composables.MillenniumTooltip
import org.spotube.millennium.theme.MillenniumTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.core.ui.misc.TrackRowSkeleton
import com.mushi.app.core.ui.misc.SkeletonBar
import com.mushi.app.core.ui.pressScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import com.composeunstyled.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import org.koin.compose.koinInject
import com.mushi.app.core.ui.component.AlbumCard
import com.mushi.app.core.ui.component.ApplicationMainBar
import com.mushi.app.core.ui.component.ArtistCard
import com.mushi.app.core.ui.component.PlaylistCard
import com.mushi.app.core.ui.component.TrackList
import com.mushi.app.core.ui.component.cards.PlayableCard
import com.mushi.app.core.ui.component.dragScrollable
import com.mushi.app.core.ui.misc.SkeletonTree
import com.mushi.app.modules.library.playlist.AddToPlaylistPicker
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import com.mushi.app.resources.iconsax.IconsaxPlay
import com.mushi.app.resources.iconsax.IconsaxShuffle
import com.mushi.app.resources.iconsax.IconsaxUserRemove
import com.mushi.app.resources.iconsax.User
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@Composable
fun ArtistScreen(
    viewModel: ArtistViewModel,
    audioPlayerQueue: AudioPlayerQueue,
    audioPlayer: AudioPlayerInterface,
    navigationCommands: NavigationCommands
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val jamRoomService: JamRoomService = koinInject()
    val jamActive by jamRoomService.role.map { it != null }
        .collectAsStateWithLifecycle(initialValue = false)
    val isJamGuest by jamRoomService.role.map { it == JamRole.Guest }
        .collectAsStateWithLifecycle(initialValue = false)
    val currentQueueEntry by audioPlayerQueue.currentQueueEntryFlow.collectAsStateWithLifecycle()
    // On phones play and shuffle live in the hero, so the track list header drops them.
    val isCompactScreen = currentWindowWidthBreakpoint() isBelow WindowBreakpoints.Medium
    val playerState by audioPlayer.playerStateFlow.collectAsStateWithLifecycle()
    val savedArtistIds by viewModel.savedArtistIds.collectAsStateWithLifecycle()
    val blacklistedArtistIds by viewModel.blacklistedArtistIds.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val trackOptionsContext by viewModel.trackOptionsContext.collectAsStateWithLifecycle()
    val showAddToPlaylistPicker by viewModel.showAddToPlaylistPicker.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MillenniumTheme.colors.background)
    ) {
        ApplicationMainBar()
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (val currentState = state) {
                is ArtistScreenState.Loading -> {
                    ArtistLoadingContent()
                }

                is ArtistScreenState.Error -> {
                    ArtistErrorContent(
                        message = currentState.message,
                        onRetry = { viewModel.refresh() }
                    )
                }

                is ArtistScreenState.Loaded -> {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            ArtistHeaderCard(
                                artist = currentState.artist,
                                isSaved = savedArtistIds.contains(currentState.artist.id),
                                isSaving = currentState.isSaving,
                                onFollowClick = viewModel::toggleSavedArtist,
                                isBlacklisted = currentState.artist.id in blacklistedArtistIds,
                                onBlacklistClick = { viewModel.toggleArtistBlacklist(currentState.artist) },
                                onPlay = viewModel::playTopTracks,
                                onShuffle = viewModel::shuffleTopTracks,
                                isPlayLoading = playbackUiState.isPlayLoading)
                        }

                        item {
                            TopTracksHeader(
                                showPlayControls = !isCompactScreen,
                                onPlay = viewModel::playTopTracks,
                                onShuffle = viewModel::shuffleTopTracks,
                                onAddToQueue = viewModel::addTopTracksToQueue,
                                isPlayLoading = playbackUiState.isPlayLoading,
                                isAddToQueueLoading = playbackUiState.isAddToQueueLoading
                            )
                        }

                        item {
                            TrackList(
                                tracks = currentState.topTracks,
                                error = null,
                                hasMore = false,
                                isLoading = false,
                                isLoadingNextPage = false,
                                currentTrackId = (currentQueueEntry as? QueueEntry.StreamingTrack)?.track?.id,
                                isCurrentTrackPlaying = playerState == PlayerState.PLAYING,
                                onTrackClick = viewModel::playTopTracksFromTrack,
                                onTrackOptionsAction = viewModel::handleTrackOptionsAction,
                                trackOptionsState = trackOptionsContext::stateFor,
                                onArtistClick = { trackArtist ->
                                    navigationCommands.navigateTo(
                                        Routes.Artist(
                                            trackArtist.id
                                        )
                                    )
                                },
                                onAlbumClick = { album ->
                                    navigationCommands.navigateTo(
                                        Routes.Album(
                                            album.id
                                        )
                                    )
                                },
                                onLoadNextPage = {},
                                simplified = true,
                                scrollable = false,
                                onBulkDownload = viewModel::downloadTracks,
                                onBulkAddToQueue = viewModel::addTracksToQueue,
                                onBulkPlayNext = viewModel::playTracksNext,
                                onBulkAddToPlaylist = viewModel::showAddToPlaylistPicker,
                                onBulkAddToJam = viewModel::addTracksToJam,
                                isInJam = jamActive,
                                isJamGuest = isJamGuest
                            )
                        }

                        if (currentState.albums.isNotEmpty() || currentState.albumsNextPagination != null) {
                            item {
                                AlbumsSection(
                                    albums = currentState.albums,
                                    onAlbumClick = { album ->
                                        navigationCommands.navigateTo(
                                            Routes.Album(
                                                album.id
                                            )
                                        )
                                    },
                                    onLoadMore = { viewModel.loadMoreAlbums() })
                            }
                        }

                        if (currentState.relatedArtists.isNotEmpty() || currentState.relatedArtistsNextPagination != null) {
                            item {
                                RelatedArtistsSection(
                                    artists = currentState.relatedArtists,
                                    onArtistClick = { artist ->
                                        navigationCommands.navigateTo(
                                            Routes.Artist(
                                                artist.id
                                            )
                                        )
                                    },
                                    onLoadMore = { viewModel.loadMoreRelatedArtists() })
                            }
                        }

                        if (currentState.featuredPlaylists.isNotEmpty() || currentState.featuredPlaylistsNextPagination != null) {
                            item {
                                FeaturedPlaylistsSection(
                                    playlists = currentState.featuredPlaylists,
                                    onPlaylistClick = { playlist ->
                                        navigationCommands.navigateTo(
                                            Routes.Playlist(
                                                playlist.id
                                            )
                                        )
                                    },
                                    onLoadMore = { viewModel.loadMoreFeaturedPlaylists() })
                            }
                        }
                    }
                }
            }
        }

        AddToPlaylistPicker(
            visible = showAddToPlaylistPicker,
            currentUserId = currentUserId,
            onDismiss = viewModel::dismissAddToPlaylistPicker,
            onPlaylistSelected = viewModel::addTracksToPlaylist
        )
    }
}

@Composable
private fun ArtistLoadingContent() {
    SkeletonTree(isLoading = true, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).shimmerApply())
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBar(Modifier.width(96.dp), height = 40.dp, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(44.dp).clip(CircleShape).shimmerApply())
                Box(Modifier.size(56.dp).clip(CircleShape).shimmerApply())
            }
            repeat(4) { TrackRowSkeleton(it) }
        }
    }
}

@Composable
private fun ArtistErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    // The shared error screen: a friendly title, the technical details clipped and copyable,
    // and Retry. A raw stack trace never fills the page.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        com.mushi.app.core.ui.component.ErrorDisplay(
            errorMessage = message,
            onRetry = onRetry,
            modifier = Modifier.widthIn(max = 420.dp)
        )
    }
}

@Composable
private fun ArtistHeaderCard(
    artist: MetadataArtist.Detailed,
    isSaved: Boolean,
    isSaving: Boolean,
    onFollowClick: () -> Unit,
    isBlacklisted: Boolean,
    onBlacklistClick: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    isPlayLoading: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val isCompact = currentWindowWidthBreakpoint() isBelow WindowBreakpoints.Medium

        // Phone: a full-width cover that fades into the page, like Spotify's artist page.
        if (isCompact) {
            ArtistHero(
                artist = artist,
                isSaved = isSaved,
                isSaving = isSaving,
                onFollowClick = onFollowClick,
                isBlacklisted = isBlacklisted,
                onBlacklistClick = onBlacklistClick,
                onPlay = onPlay,
                onShuffle = onShuffle,
                isPlayLoading = isPlayLoading
            )
            return@Box
        }

        MillenniumCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (isCompact) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArtistAvatar(
                        artist = artist,
                        size = 180.dp
                    )

                    ArtistMeta(
                        artist = artist,
                        isCompact = true
                    )

                    ArtistHeaderActions(
                        isSaved = isSaved,
                        isSaving = isSaving,
                        onFollowClick = onFollowClick,
                        isBlacklisted = isBlacklisted,
                        onBlacklistClick = onBlacklistClick
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ArtistAvatar(
                        artist = artist,
                        size = 220.dp
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ArtistMeta(
                            artist = artist,
                            isCompact = false
                        )

                        ArtistHeaderActions(
                            isSaved = isSaved,
                            isSaving = isSaving,
                            onFollowClick = onFollowClick,
                            isBlacklisted = isBlacklisted,
                            onBlacklistClick = onBlacklistClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistHero(
    artist: MetadataArtist.Detailed,
    isSaved: Boolean,
    isSaving: Boolean,
    onFollowClick: () -> Unit,
    isBlacklisted: Boolean,
    onBlacklistClick: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    isPlayLoading: Boolean
) {
    val background = AppTheme.colors.background
    val imageUrl = artist.thumbnails.firstOrNull()?.url.orEmpty()
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = artist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize().background(AppTheme.colors.surfaceContainerHighest))
            }
            // Fades the photo into the page at the top (under the back button) and at the
            // bottom (where the name sits), so there is no hard edge.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to background,
                            0.14f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1f to background
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = artist.name,
                    style = AppTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(formatFollowers(artist.followersCount))
                        if (artist.genres.isNotEmpty()) {
                            append(" \u2022 ")
                            append(artist.genres.take(2).joinToString(", "))
                        }
                    },
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtistHeaderActions(
                isSaved = isSaved,
                isSaving = isSaving,
                onFollowClick = onFollowClick,
                isBlacklisted = isBlacklisted,
                onBlacklistClick = onBlacklistClick
            )
            Spacer(Modifier.weight(1f))
            MillenniumIconButton(
                onClick = onShuffle,
                style = MillenniumIconButtonStyle.Secondary
            ) {
                MillenniumIcon(
                    imageVector = com.mushi.app.core.ui.theming.MushiIcons.Shuffle,
                    contentDescription = stringResource(Res.string.collection_details_shuffle_play)
                )
            }
            MillenniumIconButton(
                onClick = onPlay,
                loading = isPlayLoading,
                modifier = Modifier.size(56.dp).pressScale(0.92f),
                style = MillenniumIconButtonStyle.Primary
            ) {
                MillenniumIcon(
                    imageVector = com.mushi.app.core.ui.theming.MushiIcons.Play,
                    contentDescription = stringResource(Res.string.artist_play_top_tracks),
                    modifier = Modifier.size(28.dp),
                    tint = LocalTextStyle.current.color
                )
            }
        }
        artist.biography?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it.decodeHtmlEntities(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun ArtistAvatar(
    artist: MetadataArtist.Detailed,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        val imageUrl = artist.thumbnails.firstOrNull()?.url.orEmpty()
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            MillenniumIcon(
                imageVector = com.mushi.app.core.ui.theming.MushiIcons.User,
                contentDescription = artist.name,
                tint = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(size * 0.4f)
            )
        }
    }
}

@Composable
private fun ArtistMeta(
    artist: MetadataArtist.Detailed,
    isCompact: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCompact) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = artist.name,
            style = if (isCompact) AppTheme.typography.headlineMedium else AppTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = if (isCompact) TextAlign.Center else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = buildString {
                append(formatFollowers(artist.followersCount))
                if (artist.genres.isNotEmpty()) {
                    append(" • ")
                    append(artist.genres.joinToString(", "))
                }
            },
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = if (isCompact) TextAlign.Center else TextAlign.Start
        )

        artist.biography?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it.decodeHtmlEntities(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = if (isCompact) 4 else 6,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (isCompact) TextAlign.Center else TextAlign.Start
            )
        }
    }
}

@Composable
private fun ArtistHeaderActions(
    isSaved: Boolean,
    isSaving: Boolean,
    onFollowClick: () -> Unit,
    isBlacklisted: Boolean,
    onBlacklistClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSaved) {
            MillenniumButton(
                onClick = onFollowClick,
                loading = isSaving,
                style = MillenniumButtonStyle.Secondary
            ) {
                Text(
                    stringResource(Res.string.artist_following),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            MushiButton(onClick = onFollowClick, loading = isSaving) {
                Text(stringResource(Res.string.collection_details_follow), modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
            }
        }

        MillenniumTooltip(
            text = if (isBlacklisted) stringResource(Res.string.blacklist_remove) else stringResource(Res.string.blacklist_add)
        ) {
            if (isBlacklisted) {
                MillenniumIconButton(
                    onClick = onBlacklistClick,
                    style = MillenniumIconButtonStyle.Secondary
                ) {
                    MillenniumIcon(
                        imageVector = com.mushi.app.core.ui.theming.MushiIcons.UserRemove,
                        contentDescription = stringResource(Res.string.blacklist_remove),
                        tint = AppTheme.colors.error
                    )
                }
            } else {
                MillenniumIconButton(
                    onClick = onBlacklistClick,
                    style = MillenniumIconButtonStyle.Secondary
                ) {
                    MillenniumIcon(
                        imageVector = com.mushi.app.core.ui.theming.MushiIcons.UserRemove,
                        contentDescription = stringResource(Res.string.blacklist_add)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopTracksHeader(
    showPlayControls: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onAddToQueue: () -> Unit,
    isPlayLoading: Boolean = false,
    isAddToQueueLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(Res.string.artist_top_tracks),
            style = AppTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showPlayControls) {
                MillenniumIconButton(
                    onClick = onPlay,
                    loading = isPlayLoading,
                    style = MillenniumIconButtonStyle.Primary
                ) {
                    MillenniumIcon(
                        imageVector = com.mushi.app.core.ui.theming.MushiIcons.Play,
                        contentDescription = stringResource(Res.string.artist_play_top_tracks),
                        tint = LocalTextStyle.current.color
                    )
                }
                MillenniumIconButton(
                    onClick = onShuffle,
                    style = MillenniumIconButtonStyle.Secondary
                ) {
                    MillenniumIcon(
                        imageVector = com.mushi.app.core.ui.theming.MushiIcons.Shuffle,
                        contentDescription = stringResource(
                            com.mushi.app.generated.resources.Res.string.collection_details_shuffle_play)
                    )
                }
            }
            MillenniumIconButton(
                onClick = onAddToQueue,
                loading = isAddToQueueLoading,
                style = MillenniumIconButtonStyle.Secondary
            ) {
                MillenniumIcon(
                    imageVector = Iconsax.IconsaxAddSquare,
                    contentDescription = stringResource(Res.string.artist_add_top_tracks_to_queue)
                )
            }
        }
    }
}

@Composable
private fun AlbumsSection(
    albums: List<MetadataAlbum.Detailed>,
    onAlbumClick: (MetadataAlbum.Detailed) -> Unit,
    onLoadMore: () -> Unit
) {
    val rowState = rememberLazyListState()

    LaunchedEffect(rowState) {
        snapshotFlow { rowState.layoutInfo }
            .map { layoutInfo ->
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItems = layoutInfo.totalItemsCount
                Pair(lastVisibleIndex, totalItems)
            }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItems) ->
                if (lastVisibleIndex != null && totalItems > 0 && lastVisibleIndex >= totalItems - 2) {
                    onLoadMore()
                }
            }
    }

    SectionHeader(title = stringResource(Res.string.library_tab_albums))
    Spacer(modifier = Modifier.size(8.dp))
    LazyRow(
        state = rowState,
        modifier = Modifier.dragScrollable(rowState),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        itemsWithUniqueKeys(albums, id = { it.id }) { album ->
            AlbumCard(album = album)
        }
    }
}

@Composable
private fun RelatedArtistsSection(
    artists: List<MetadataArtist.Basic>,
    onArtistClick: (MetadataArtist.Basic) -> Unit,
    onLoadMore: () -> Unit
) {
    val rowState = rememberLazyListState()

    LaunchedEffect(rowState) {
        snapshotFlow { rowState.layoutInfo }
            .map { layoutInfo ->
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItems = layoutInfo.totalItemsCount
                Pair(lastVisibleIndex, totalItems)
            }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItems) ->
                if (lastVisibleIndex != null && totalItems > 0 && lastVisibleIndex >= totalItems - 2) {
                    onLoadMore()
                }
            }
    }

    SectionHeader(title = stringResource(Res.string.artist_related_artists))
    Spacer(modifier = Modifier.size(8.dp))
    LazyRow(
        state = rowState,
        modifier = Modifier.dragScrollable(rowState),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        itemsWithUniqueKeys(artists, id = { it.id }) { artist ->
            ArtistCard(artist = artist)
        }
    }
}

@Composable
private fun FeaturedPlaylistsSection(
    playlists: List<MetadataPlaylist>,
    onPlaylistClick: (MetadataPlaylist) -> Unit,
    onLoadMore: () -> Unit
) {
    val rowState = rememberLazyListState()

    LaunchedEffect(rowState) {
        snapshotFlow { rowState.layoutInfo }
            .map { layoutInfo ->
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItems = layoutInfo.totalItemsCount
                Pair(lastVisibleIndex, totalItems)
            }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItems) ->
                if (lastVisibleIndex != null && totalItems > 0 && lastVisibleIndex >= totalItems - 2) {
                    onLoadMore()
                }
            }
    }

    SectionHeader(title = stringResource(Res.string.artist_featured_playlists))
    Spacer(modifier = Modifier.size(8.dp))
    LazyRow(
        state = rowState,
        modifier = Modifier.dragScrollable(rowState),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        itemsWithUniqueKeys(playlists, id = { it.id }) { playlist ->
            PlaylistCard(playlist = playlist)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = AppTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun formatFollowers(count: Int?): String {
    if (count == null) return stringResource(Res.string.artist_followers_unavailable)
    return when {
        count >= 1_000_000_000 -> stringResource(Res.string.artist_followers_billions, formatAbbreviatedCount(count, 1_000_000_000))
        count >= 1_000_000 -> stringResource(Res.string.artist_followers_millions, formatAbbreviatedCount(count, 1_000_000))
        count >= 1_000 -> stringResource(Res.string.artist_followers_thousands, formatAbbreviatedCount(count, 1_000))
        else -> pluralStringResource(Res.plurals.artist_followers_count, count, count)
    }
}

private fun formatAbbreviatedCount(count: Int, divisor: Int): String {
    val scaled = count / divisor.toDouble()
    val rounded = (scaled * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}


/** Biographies come from the service with HTML entities ("&#61;" for "="); show the real characters. */
private fun String.decodeHtmlEntities(): String = this
    .replace("&#61;", "=")
    .replace("&#39;", "'")
    .replace("&quot;", "\"")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&amp;", "&")
