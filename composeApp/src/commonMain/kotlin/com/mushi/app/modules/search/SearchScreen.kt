package com.mushi.app.modules.search


import com.mushi.app.core.ui.gridItemMotion
import com.mushi.app.core.ui.component.MushiButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import com.composeunstyled.currentWindowWidthBreakpoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.spotube.millennium.adaptive.WindowBreakpoints
import org.spotube.millennium.composables.MillenniumAutocomplete
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumListTile
import org.spotube.millennium.composables.MillenniumMarqueeProgress
import org.spotube.millennium.composables.MillenniumText
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumIconButton
import com.mushi.app.generated.resources.common_close
import com.mushi.app.resources.iconsax.IconsaxCloseSquare
import com.mushi.app.resources.iconsax.IconsaxSearch
import org.spotube.millennium.composables.MillenniumTextField
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.search.MetadataSupportedSearchType
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import org.spotube.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.remote.RemotePlaybackController
import com.mushi.app.core.share.ShareService
import com.mushi.app.core.ui.component.AlbumCard
import com.mushi.app.core.ui.component.ApplicationMainBar
import com.mushi.app.core.ui.component.ArtistCard
import com.mushi.app.core.ui.component.ErrorDisplay
import com.mushi.app.core.ui.component.PlaylistCard
import com.mushi.app.core.ui.component.TrackList
import com.mushi.app.core.ui.component.TrackOptionsAction
import com.mushi.app.core.ui.component.TrackOptionsState
import com.mushi.app.core.ui.component.UserCard
import com.mushi.app.core.ui.component.cards.PlayableCard
import com.mushi.app.core.ui.component.dragScrollable
import com.mushi.app.core.ui.misc.SkeletonTree
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.getPlatform
import com.mushi.app.isDesktop
import com.mushi.app.modules.blacklist.BlacklistRepository
import com.mushi.app.modules.downloads.DownloadsViewModel
import com.mushi.app.modules.library.LibraryRepository
import com.mushi.app.modules.library.playlist.AddToPlaylistPicker
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import com.mushi.app.core.ui.misc.CardGridSkeleton
import com.mushi.app.core.ui.misc.TrackListSkeleton
import com.mushi.app.core.ui.misc.LoadingCrossfade
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxSearchBroken
import com.mushi.app.resources.iconsax.IconsaxTrash
import com.mushi.app.resources.iconsax.InconsaxClock
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.error_try_again
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import com.mushi.app.generated.resources.nav_search
import com.mushi.app.generated.resources.library_tab_albums
import com.mushi.app.generated.resources.library_tab_artists
import com.mushi.app.generated.resources.library_tab_playlists
import com.mushi.app.generated.resources.search_clear_history
import com.mushi.app.generated.resources.search_loading_albums
import com.mushi.app.generated.resources.search_loading_artists
import com.mushi.app.generated.resources.search_loading_playlists
import com.mushi.app.generated.resources.search_loading_tracks
import com.mushi.app.generated.resources.search_loading_users
import com.mushi.app.generated.resources.search_no_albums
import com.mushi.app.generated.resources.search_no_artists
import com.mushi.app.generated.resources.search_no_playlists
import com.mushi.app.generated.resources.search_no_results
import com.mushi.app.generated.resources.search_no_search_types
import com.mushi.app.generated.resources.search_no_users
import com.mushi.app.generated.resources.search_placeholder
import com.mushi.app.generated.resources.search_results_title
import com.mushi.app.generated.resources.search_searching
import com.mushi.app.generated.resources.search_section_tracks
import com.mushi.app.generated.resources.search_section_users
import com.mushi.app.generated.resources.search_see_all
import com.mushi.app.generated.resources.search_start_typing
import com.mushi.app.generated.resources.search_start_typing_tracks
import com.mushi.app.generated.resources.search_tab_all
import com.mushi.app.generated.resources.search_warm_antennas
import com.mushi.app.generated.resources.search_warm_cosmos
import com.mushi.app.generated.resources.search_warm_crates
import com.mushi.app.generated.resources.search_warm_music_gods
import com.mushi.app.generated.resources.search_warm_records
import com.mushi.app.generated.resources.search_warm_speakers
import com.mushi.app.generated.resources.search_warm_vinyl

private val GridMinCellSize = 180.dp

@Composable
fun SearchScreen(viewModel: SearchScreenViewModel = koinViewModel()) {
    val audioPlayerQueue: AudioPlayerQueue = koinInject()
    val shareService: ShareService = koinInject()
    val remotePlaybackController: RemotePlaybackController = koinInject()
    val searchResultsTitle = stringResource(Res.string.search_results_title)
    val downloadsViewModel: DownloadsViewModel = koinViewModel()
    val libraryRepository: LibraryRepository = koinInject()
    val blacklistRepository: BlacklistRepository = koinInject()
    val navigationCommands: NavigationCommands = koinInject()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val jamRoomService: JamRoomService = koinInject()
    val jamActive by jamRoomService.role.map { it != null }
        .collectAsStateWithLifecycle(initialValue = false)
    val isJamGuest by jamRoomService.role.map { it == JamRole.Guest }
        .collectAsStateWithLifecycle(initialValue = false)
    val selectedType = state.selectedSearchType
    val scope = rememberCoroutineScope()
    val savedTrackIds by viewModel.savedTrackIds.collectAsStateWithLifecycle()
    var showAddToPlaylistPicker by remember { mutableStateOf(false) }
    var tracksToAddToPlaylist by remember { mutableStateOf<List<MetadataTrack>>(emptyList()) }
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()

    val blacklistedTracks by blacklistRepository.blacklistedTracks.collectAsStateWithLifecycle(
        emptyList()
    )
    val blacklistedArtists by blacklistRepository.blacklistedArtists.collectAsStateWithLifecycle(
        emptyList()
    )

    val isExpanded =
        currentWindowWidthBreakpoint() isAtLeast WindowBreakpoints.Expanded && getPlatform().isDesktop()

    fun isTrackBlacklisted(track: MetadataTrack): Boolean {
        val trackIds = blacklistedTracks.map { it.id }.toSet()
        val artistIds = blacklistedArtists.map { it.id }.toSet()
        return track.id in trackIds || track.artists.any { it.id in artistIds }
    }

    fun toggleTrackBlacklist(track: MetadataTrack) {
        scope.launch {
            blacklistRepository.toggleTrack(track)
        }
    }

    val focusManager = LocalFocusManager.current

    fun playSingleTrack(track: MetadataTrack) {
        viewModel.rememberCurrentQuery()
        scope.launch {
            audioPlayerQueue.load(
                entries = listOf(QueueEntry.StreamingTrack(track = track, url = "")),
                autoPlay = true,
                startPosition = 0,
                collectionEntry = null
            )
        }
    }

    fun handleTrackOptionsAction(track: MetadataTrack, action: TrackOptionsAction) {
        scope.launch {
            when (action) {
                is TrackOptionsAction.StartRadio -> {}
                is TrackOptionsAction.PlayNext -> {
                    val queue = audioPlayerQueue.getQueue()
                    val existing = queue.find { entry ->
                        (entry as? QueueEntry.StreamingTrack)?.track?.id == track.id
                    }
                    if (existing != null) {
                        // Already in the local queue: move it to the next position
                        audioPlayerQueue.removeFromQueue(existing)
                        audioPlayerQueue.addAllAfterCurrent(
                            listOf(
                                QueueEntry.StreamingTrack(
                                    track = track,
                                    url = ""
                                )
                            )
                        )
                    } else {
                        remotePlaybackController.requestTrackPlayNext(track)
                    }
                }

                is TrackOptionsAction.AddToQueue -> {
                    remotePlaybackController.requestTrackAddToQueue(track)
                }

                is TrackOptionsAction.AddToJam -> {
                    remotePlaybackController.addTrackToJam(track)
                }

                is TrackOptionsAction.RemoveFromQueue -> {
                    val queue = audioPlayerQueue.getQueue()
                    queue.find { entry ->
                        (entry as? QueueEntry.StreamingTrack)?.track?.id == track.id
                    }?.let { audioPlayerQueue.removeFromQueue(it) }
                }

                is TrackOptionsAction.ToggleFavorite -> viewModel.toggleTrackIsFavorite(track.id)
                is TrackOptionsAction.Download -> downloadsViewModel.downloadTrack(track)
                is TrackOptionsAction.ToggleBlacklist -> toggleTrackBlacklist(track)
                is TrackOptionsAction.Share -> {
                    val uri = track.externalUri?.takeIf { it.isNotBlank() }
                    if (uri != null) {
                        shareService.share(uri, track.title)
                    }
                }

                is TrackOptionsAction.AddToPlaylist -> {
                    tracksToAddToPlaylist = listOf(track)
                    showAddToPlaylistPicker = true
                }
            }
        }
    }

    fun getTrackOptionsState(track: MetadataTrack): TrackOptionsState {
        val currentQueueEntry = audioPlayerQueue.currentQueueEntryFlow.value
        val currentTrackId = (currentQueueEntry as? QueueEntry.StreamingTrack)?.track?.id
        val queue = audioPlayerQueue.queueFlow.value
        val queueTrackIds = queue.mapNotNull { entry ->
            (entry as? QueueEntry.StreamingTrack)?.track?.id
        }.toSet()
        return TrackOptionsState(
            isInQueue = queueTrackIds.contains(track.id),
            isCurrentlyPlaying = track.id == currentTrackId,
            isFavorite = savedTrackIds.contains(track.id),
            isBlacklisted = isTrackBlacklisted(track)
        )
    }

    val queue by audioPlayerQueue.queueFlow.collectAsStateWithLifecycle(emptyList())
    val currentQueueEntry by audioPlayerQueue.currentQueueEntryFlow.collectAsStateWithLifecycle()

    fun getTrackOptionsStateReactive(track: MetadataTrack): TrackOptionsState {
        val currentTrackId = (currentQueueEntry as? QueueEntry.StreamingTrack)?.track?.id
        val queueTrackIds = queue.mapNotNull { entry ->
            (entry as? QueueEntry.StreamingTrack)?.track?.id
        }.toSet()
        return TrackOptionsState(
            isInQueue = queueTrackIds.contains(track.id),
            isCurrentlyPlaying = track.id == currentTrackId,
            isFavorite = savedTrackIds.contains(track.id),
            isBlacklisted = isTrackBlacklisted(track)
        )
    }

    fun bulkAddToQueue(tracks: List<MetadataTrack>) {
        remotePlaybackController.requestTracksAddToQueue(tracks, searchResultsTitle)
    }

    fun bulkAddToJam(tracks: List<MetadataTrack>) {
        remotePlaybackController.addTracksToJam(tracks)
    }

    fun bulkPlayNext(tracks: List<MetadataTrack>) {
        remotePlaybackController.requestTracksPlayNext(tracks, searchResultsTitle)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MillenniumTheme.colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        if (!isExpanded) {
            com.mushi.app.core.ui.component.PageHeader(
                title = stringResource(Res.string.nav_search)
            ) { com.mushi.app.core.ui.component.ProfileButton() }
            SearchBar(
                state = viewModel.queryText,
                // Recent searches live in the inline list under the field; the library's dropdown is off.
                recentSearchSuggestions = emptyList(),
                onRecentSearchClick = { search ->
                    viewModel.applyRecentSearch(search)
                    focusManager.clearFocus()
                },
                onClearAllRecentSearches = {
                    viewModel.clearAllRecentSearches()
                    focusManager.clearFocus()
                },
                onSubmit = { viewModel.rememberCurrentQuery() },
            )
        } else {
            ApplicationMainBar(
                backButton = false,
                actions = { com.mushi.app.core.ui.component.ProfileButton() })
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExpanded) {
                    SearchBar(
                        state = viewModel.queryText,
                        // Recent searches live in the inline list under the field; the library's dropdown is off.
                recentSearchSuggestions = emptyList(),
                        onRecentSearchClick = { search ->
                            viewModel.applyRecentSearch(search)
                            focusManager.clearFocus()
                        },
                        onClearAllRecentSearches = {
                            viewModel.clearAllRecentSearches()
                            focusManager.clearFocus()
                        },
                        onSubmit = { viewModel.rememberCurrentQuery() }
                    )
                }

                if (state.supportedSearchTypes.isNotEmpty()) {
                    SearchTabs(
                        types = state.supportedSearchTypes,
                        selectedType = selectedType,
                        onTabSelected = viewModel::onTabSelected
                    )
                }
            }

            when {
                state.isLoadingSearchTypes -> {
                    TrackListSkeleton(Modifier.fillMaxSize())
                }

                selectedType == null -> {
                    SearchMessage(
                        message = stringResource(Res.string.search_no_search_types),
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                state.query.isBlank() -> {
                    val categoryCovers by viewModel.categoryCovers.collectAsStateWithLifecycle()
                    RecentSearchesList(
                        categoryCovers = categoryCovers,
                        onCategoryShown = viewModel::loadCategoryCover,
                        searches = state.recentSearches,
                        onSearchClick = { search ->
                            viewModel.applyRecentSearch(search)
                            focusManager.clearFocus()
                        },
                        onRemove = viewModel::removeRecentSearch,
                        onClearAll = viewModel::clearAllRecentSearches,
                        onCategoryClick = { category ->
                            viewModel.applyRecentSearch(category)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                selectedType == MetadataSupportedSearchType.ALL -> {
                    SearchAllTab(
                        query = state.query,
                        tracks = state.tracks,
                        albums = state.albums,
                        artists = state.artists,
                        playlists = state.playlists,
                        users = state.users,
                        supportedSearchTypes = state.supportedSearchTypes,
                        onSeeAll = viewModel::onTabSelected,
                        onRetry = viewModel::retry,
                        onTrackClick = ::playSingleTrack,
                        onTrackOptionsAction = ::handleTrackOptionsAction,
                        onTrackOptionsState = ::getTrackOptionsStateReactive,
                        onBulkDownload = { tracks -> downloadsViewModel.downloadTracks(tracks) },
                        onBulkAddToQueue = ::bulkAddToQueue,
                        onBulkPlayNext = ::bulkPlayNext,
                        onBulkAddToPlaylist = { tracks ->
                            tracksToAddToPlaylist = tracks
                            showAddToPlaylistPicker = true
                        },
                        onBulkAddToJam = ::bulkAddToJam,
                        isInJam = jamActive,
                        isJamGuest = isJamGuest,
                        onArtistClick = { artist ->
                            viewModel.rememberCurrentQuery()
                            navigationCommands.navigateTo(Routes.Artist(artist.id))
                        },
                        onAlbumClick = { album ->
                            viewModel.rememberCurrentQuery()
                            navigationCommands.navigateTo(Routes.Album(album.id))
                        },
                        onArtistsOverflowClick = { track ->
                            // No-op for now, could show a dialog with all artists
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                selectedType == MetadataSupportedSearchType.TRACK -> {
                    SearchTracksTab(
                        query = state.query,
                        tracks = state.tracks,
                        onLoadNextPage = viewModel::loadNextTracks,
                        onRetry = viewModel::retry,
                        onTrackClick = ::playSingleTrack,
                        onTrackOptionsAction = ::handleTrackOptionsAction,
                        onTrackOptionsState = ::getTrackOptionsStateReactive,
                        onBulkDownload = { tracks -> downloadsViewModel.downloadTracks(tracks) },
                        onBulkAddToQueue = ::bulkAddToQueue,
                        onBulkPlayNext = ::bulkPlayNext,
                        onBulkAddToPlaylist = { tracks ->
                            tracksToAddToPlaylist = tracks
                            showAddToPlaylistPicker = true
                        },
                        onBulkAddToJam = ::bulkAddToJam,
                        isInJam = jamActive,
                        isJamGuest = isJamGuest,
                        onArtistClick = { artist ->
                            viewModel.rememberCurrentQuery()
                            navigationCommands.navigateTo(Routes.Artist(artist.id))
                        },
                        onAlbumClick = { album ->
                            viewModel.rememberCurrentQuery()
                            navigationCommands.navigateTo(Routes.Album(album.id))
                        },
                        onArtistsOverflowClick = { track ->
                            // No-op for now, could show a dialog with all artists
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                selectedType == MetadataSupportedSearchType.PLAYLIST -> {
                    SearchPlaylistsTab(
                        query = state.query,
                        playlists = state.playlists,
                        onLoadNextPage = viewModel::loadNextPlaylists,
                        onRetry = viewModel::retry,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                selectedType == MetadataSupportedSearchType.ALBUM -> {
                    SearchAlbumsTab(
                        query = state.query,
                        albums = state.albums,
                        onLoadNextPage = viewModel::loadNextAlbums,
                        onRetry = viewModel::retry,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                selectedType == MetadataSupportedSearchType.ARTIST -> {
                    SearchArtistsTab(
                        query = state.query,
                        artists = state.artists,
                        onLoadNextPage = viewModel::loadNextArtists,
                        onRetry = viewModel::retry,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                selectedType == MetadataSupportedSearchType.USER -> {
                    SearchUsersTab(
                        query = state.query,
                        users = state.users,
                        onLoadNextPage = viewModel::loadNextUsers,
                        onRetry = viewModel::retry,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }

        AddToPlaylistPicker(
            visible = showAddToPlaylistPicker,
            currentUserId = currentUserId,
            onDismiss = { showAddToPlaylistPicker = false },
            onPlaylistSelected = { playlistId ->
                scope.launch {
                    libraryRepository.addTracksToPlaylist(
                        playlistId,
                        tracksToAddToPlaylist.map { it.id }
                    )
                }
            })
    }
}

@Composable
private fun SearchBar(
    state: TextFieldState,
    recentSearchSuggestions: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onClearAllRecentSearches: () -> Unit,
    onSubmit: () -> Unit = {}
) {
    var hadFocus by remember { mutableStateOf(false) }

    // The same flat field as everywhere else (the library's autocomplete draws its own glossy
    // focus ring that the theme can't turn off). Recent searches live in the list under it.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        MillenniumTextField(
            state = state,
            label = "",
            placeholder = stringResource(Res.string.search_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                // Finishing with the field (keyboard dismissed, a result tapped) remembers the search.
                .onFocusChanged { focus ->
                    if (hadFocus && !focus.isFocused) onSubmit()
                    hadFocus = focus.isFocused
                })
        if (state.text.isNotEmpty()) {
            MillenniumIconButton(
                onClick = { state.edit { replace(0, length, "") } },
                modifier = Modifier.align(Alignment.CenterEnd),
                style = MillenniumIconButtonStyle.Quiet) {
                MillenniumIcon(
                    imageVector = com.mushi.app.core.ui.theming.MushiIcons.Close,
                    contentDescription = stringResource(Res.string.common_close),
                    modifier = Modifier.size(18.dp),
                    tint = AppTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SearchTabs(
    types: List<MetadataSupportedSearchType>,
    selectedType: MetadataSupportedSearchType?,
    onTabSelected: (MetadataSupportedSearchType) -> Unit
) {
    val listState = rememberLazyListState()
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().dragScrollable(listState),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(types) { type ->
            com.mushi.app.core.ui.component.MushiFilterChip(
                label = type.tabTitle(),
                selected = selectedType == type,
                onClick = { onTabSelected(type) })
        }
    }
}

@Composable
private fun SearchAllTab(
    query: String,
    tracks: SearchPagedState<MetadataTrack>,
    albums: SearchPagedState<MetadataAlbum.Basic>,
    artists: SearchPagedState<MetadataArtist.Basic>,
    playlists: SearchPagedState<MetadataPlaylist>,
    users: SearchPagedState<MetadataUser>,
    supportedSearchTypes: List<MetadataSupportedSearchType>,
    onSeeAll: (MetadataSupportedSearchType) -> Unit,
    onRetry: () -> Unit,
    onTrackClick: (MetadataTrack) -> Unit,
    onTrackOptionsAction: (MetadataTrack, TrackOptionsAction) -> Unit,
    onTrackOptionsState: (MetadataTrack) -> TrackOptionsState,
    onBulkDownload: (List<MetadataTrack>) -> Unit,
    onBulkAddToQueue: (List<MetadataTrack>) -> Unit,
    onBulkPlayNext: (List<MetadataTrack>) -> Unit,
    onBulkAddToPlaylist: (List<MetadataTrack>) -> Unit,
    onBulkAddToJam: (List<MetadataTrack>) -> Unit,
    isInJam: Boolean,
    isJamGuest: Boolean,
    onArtistClick: (MetadataArtist.Basic) -> Unit,
    onAlbumClick: (MetadataAlbum.Detailed) -> Unit,
    onArtistsOverflowClick: (MetadataTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    if (query.isBlank()) {
        SearchMessage(message = stringResource(Res.string.search_start_typing), modifier = modifier)
        return
    }

    val hasAnyContent =
        tracks.items.isNotEmpty() || playlists.items.isNotEmpty() || albums.items.isNotEmpty() ||
                artists.items.isNotEmpty() || users.items.isNotEmpty()

    val hasAnyError = listOfNotNull(
        tracks.error,
        playlists.error,
        albums.error,
        artists.error,
        users.error
    ).firstOrNull()
    val isLoadingAll =
        tracks.isLoading || playlists.isLoading || albums.isLoading || artists.isLoading || users.isLoading

    if (!hasAnyContent && !isLoadingAll && hasAnyError != null) {
        ErrorDisplay(
            errorMessage = hasAnyError,
            onRetry = onRetry,
            modifier = modifier
        )
        return
    }

    if (!hasAnyContent && !isLoadingAll) {
        SearchMessage(message = stringResource(Res.string.search_no_results), modifier = modifier)
        return
    }

    val bottomInset = LocalAppShellBottomInset.current
    val showTracks =
        MetadataSupportedSearchType.TRACK in supportedSearchTypes && tracks.items.isNotEmpty()

    TrackList(
        tracks = if (showTracks) tracks.items.take(20) else emptyList(),
        isLoading = tracks.isLoading && showTracks,
        error = if (showTracks) tracks.error else null,
        hasMore = false,
        simplified = true,
        showEmptyMessage = false,
        modifier = modifier,
        onTrackClick = onTrackClick,
        onTrackOptionsAction = onTrackOptionsAction,
        trackOptionsState = onTrackOptionsState,
        onBulkDownload = onBulkDownload,
        onBulkAddToQueue = onBulkAddToQueue,
        onBulkPlayNext = onBulkPlayNext,
        onBulkAddToPlaylist = onBulkAddToPlaylist,
        onBulkAddToJam = onBulkAddToJam,
        isInJam = isInJam,
        isJamGuest = isJamGuest,
        onArtistClick = onArtistClick,
        onAlbumClick = onAlbumClick,
        onArtistsOverflowClick = onArtistsOverflowClick,
        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp + bottomInset),
        headerContent = {
            if (showTracks) {
                Text(
                    text = stringResource(Res.string.search_section_tracks),
                    style = AppTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                )
            }
            if (MetadataSupportedSearchType.TRACK in supportedSearchTypes) {
                if (tracks.isLoading && tracks.items.isEmpty()) {
                    TrackListSkeleton(count = 5)
                }
                if (tracks.error != null && tracks.items.isEmpty()) {
                    SearchMessageInline(stringResource(Res.string.error_try_again), isError = true)
                }
            }
        },
        footerContent = {
            Column(
                modifier = Modifier.padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (MetadataSupportedSearchType.PLAYLIST in supportedSearchTypes && playlists.items.isNotEmpty()) {
                    SearchHorizontalSection(
                        title = stringResource(Res.string.library_tab_playlists),
                        onSeeAll = { onSeeAll(MetadataSupportedSearchType.PLAYLIST) }) {
                        playlists.items.forEach { playlist ->
                            item(key = playlist.id) { PlaylistCard(playlist = playlist) }
                        }
                    }
                }

                if (MetadataSupportedSearchType.ALBUM in supportedSearchTypes && albums.items.isNotEmpty()) {
                    SearchHorizontalSection(
                        title = stringResource(Res.string.library_tab_albums),
                        onSeeAll = { onSeeAll(MetadataSupportedSearchType.ALBUM) }) {
                        albums.items.forEach { album ->
                            item(key = album.id) { AlbumCard(album = album) }
                        }
                    }
                }

                if (MetadataSupportedSearchType.ARTIST in supportedSearchTypes && artists.items.isNotEmpty()) {
                    SearchHorizontalSection(
                        title = stringResource(Res.string.library_tab_artists),
                        onSeeAll = { onSeeAll(MetadataSupportedSearchType.ARTIST) }) {
                        artists.items.forEach { artist ->
                            item(key = artist.id) { ArtistCard(artist = artist) }
                        }
                    }
                }

                if (MetadataSupportedSearchType.USER in supportedSearchTypes && users.items.isNotEmpty()) {
                    SearchHorizontalSection(
                        title = stringResource(Res.string.search_section_users),
                        onSeeAll = { onSeeAll(MetadataSupportedSearchType.USER) }) {
                        users.items.forEach { user ->
                            item(key = user.id) { UserCard(user = user) }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SearchTracksTab(
    query: String,
    tracks: SearchPagedState<MetadataTrack>,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onTrackClick: (MetadataTrack) -> Unit,
    onTrackOptionsAction: (MetadataTrack, TrackOptionsAction) -> Unit,
    onTrackOptionsState: (MetadataTrack) -> TrackOptionsState,
    onBulkDownload: (List<MetadataTrack>) -> Unit,
    onBulkAddToQueue: (List<MetadataTrack>) -> Unit,
    onBulkPlayNext: (List<MetadataTrack>) -> Unit,
    onBulkAddToPlaylist: (List<MetadataTrack>) -> Unit,
    onBulkAddToJam: (List<MetadataTrack>) -> Unit,
    isInJam: Boolean,
    isJamGuest: Boolean,
    onArtistClick: (MetadataArtist.Basic) -> Unit,
    onAlbumClick: (MetadataAlbum.Detailed) -> Unit,
    onArtistsOverflowClick: (MetadataTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    if (query.isBlank()) {
        SearchMessage(message = stringResource(Res.string.search_start_typing_tracks), modifier = modifier)
        return
    }

    LoadingCrossfade(
        isLoading = tracks.isLoading && tracks.items.isEmpty(),
        modifier = modifier,
        skeleton = { TrackListSkeleton() }
    ) {
    val failure = tracks.error
    if (failure != null && tracks.items.isEmpty()) ErrorDisplay(
        errorMessage = failure,
        onRetry = onRetry,
        modifier = Modifier.fillMaxSize())
    else TrackList(
        tracks = tracks.items,
        error = tracks.error,
        hasMore = tracks.hasNextPage,
        isLoading = false,
        isLoadingNextPage = tracks.isLoading && tracks.items.isNotEmpty(),
        onLoadNextPage = onLoadNextPage,
        onTrackClick = onTrackClick,
        onTrackOptionsAction = onTrackOptionsAction,
        trackOptionsState = onTrackOptionsState,
        onBulkDownload = onBulkDownload,
        onBulkAddToQueue = onBulkAddToQueue,
        onBulkPlayNext = onBulkPlayNext,
        onBulkAddToPlaylist = onBulkAddToPlaylist,
        onBulkAddToJam = onBulkAddToJam,
        isInJam = isInJam,
        isJamGuest = isJamGuest,
        onArtistClick = onArtistClick,
        onAlbumClick = onAlbumClick,
        onArtistsOverflowClick = onArtistsOverflowClick,
        simplified = true,
        modifier = modifier
    )
    }
}

@Composable
private fun SearchAlbumsTab(
    query: String,
    albums: SearchPagedState<MetadataAlbum.Basic>,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchGridTab(
        query = query,
        items = albums.items,
        isLoading = albums.isLoading,
        error = albums.error,
        hasNextPage = albums.hasNextPage,
        onLoadNextPage = onLoadNextPage,
        onRetry = onRetry,
        emptyMessage = stringResource(Res.string.search_no_albums),
        modifier = modifier
    ) { album ->
        AlbumCard(album = album, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SearchArtistsTab(
    query: String,
    artists: SearchPagedState<MetadataArtist.Basic>,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchGridTab(
        query = query,
        items = artists.items,
        isLoading = artists.isLoading,
        error = artists.error,
        hasNextPage = artists.hasNextPage,
        onLoadNextPage = onLoadNextPage,
        onRetry = onRetry,
        emptyMessage = stringResource(Res.string.search_no_artists),
        modifier = modifier
    ) { artist ->
        ArtistCard(artist = artist, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SearchPlaylistsTab(
    query: String,
    playlists: SearchPagedState<MetadataPlaylist>,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchGridTab(
        query = query,
        items = playlists.items,
        isLoading = playlists.isLoading,
        error = playlists.error,
        hasNextPage = playlists.hasNextPage,
        onLoadNextPage = onLoadNextPage,
        onRetry = onRetry,
        emptyMessage = stringResource(Res.string.search_no_playlists),
        modifier = modifier
    ) { playlist ->
        PlaylistCard(playlist = playlist, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SearchUsersTab(
    query: String,
    users: SearchPagedState<MetadataUser>,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchGridTab(
        query = query,
        items = users.items,
        isLoading = users.isLoading,
        error = users.error,
        hasNextPage = users.hasNextPage,
        onLoadNextPage = onLoadNextPage,
        onRetry = onRetry,
        emptyMessage = stringResource(Res.string.search_no_users),
        modifier = modifier
    ) { user ->
        UserCard(user = user, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> SearchGridTab(
    query: String,
    items: List<T>,
    isLoading: Boolean,
    error: String?,
    hasNextPage: Boolean,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    if (query.isBlank()) {
        SearchMessage(message = stringResource(Res.string.search_start_typing), modifier = modifier)
        return
    }

    val gridState = rememberLazyGridState()
    val bottomInset = LocalAppShellBottomInset.current

    LaunchedEffect(gridState, items.size, hasNextPage, isLoading) {
        snapshotFlow { gridState.layoutInfo }
            .map { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                lastVisible to layoutInfo.totalItemsCount
            }
            .distinctUntilChanged()
            .collect { (lastVisible, totalItems) ->
                if (totalItems > 0 && lastVisible >= totalItems - 6 && hasNextPage && !isLoading) {
                    onLoadNextPage()
                }
            }
    }

    LoadingCrossfade(
        isLoading = isLoading && items.isEmpty(),
        modifier = modifier,
        skeleton = { CardGridSkeleton(minCellSize = GridMinCellSize) }
    ) {
    when {
        error != null && items.isEmpty() -> {
            ErrorDisplay(
                errorMessage = error,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )
        }

        !isLoading && items.isEmpty() -> {
            SearchMessage(message = emptyMessage, modifier = modifier)
        }

        else -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = GridMinCellSize),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp + bottomInset),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    Box(gridItemMotion()) { itemContent(item) }
                }

                if (isLoading && items.isNotEmpty()) {
                    items(4) {
                        SkeletonTree(true) {
                            PlayableCard(
                                title = "Sample Title",
                                subtitle = "Sample Subtitle",
                                imageURL = "https://placehold.co/600x400"
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun SearchHorizontalSection(
    title: String,
    onSeeAll: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(Res.string.search_see_all),
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colors.primary,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SearchTrackRow(
    index: Int,
    track: MetadataTrack,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = (index + 1).toString(),
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.End
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = track.title,
                style = AppTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                text = track.artists.joinToString { it.name },
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1
            )
        }

        if (track.album != null) Text(
            text = track.album!!.title,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SearchMessage(
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = if (isError) AppTheme.colors.error else AppTheme.colors.onSurfaceVariant,
            style = AppTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun SearchMessageInline(
    message: String,
    isError: Boolean = false
) {
    Text(
        text = message,
        color = if (isError) AppTheme.colors.error else AppTheme.colors.onSurfaceVariant,
        style = AppTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun MetadataSupportedSearchType.tabTitle(): String = when (this) {
    MetadataSupportedSearchType.ALL -> stringResource(Res.string.search_tab_all)
    MetadataSupportedSearchType.TRACK -> stringResource(Res.string.search_section_tracks)
    MetadataSupportedSearchType.PLAYLIST -> stringResource(Res.string.library_tab_playlists)
    MetadataSupportedSearchType.ALBUM -> stringResource(Res.string.library_tab_albums)
    MetadataSupportedSearchType.ARTIST -> stringResource(Res.string.library_tab_artists)
    MetadataSupportedSearchType.USER -> stringResource(Res.string.search_section_users)
}
