package com.mushi.app.modules.shell


import com.mushi.app.core.ui.component.livingBackdrop
import org.spotube.millennium.composables.MillenniumIcon
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.mushi.app.core.ui.theming.rememberArtworkTint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import com.mushi.app.core.ui.theming.AppTheme
import org.spotube.millennium.composables.MillenniumSnackbarHost
import org.spotube.millennium.composables.MillenniumSnackbarHostState
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumModalBottomSheet
import org.spotube.millennium.composables.MillenniumCard
import org.spotube.millennium.composables.MillenniumSlider
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import com.composeunstyled.SheetDetent
import com.composeunstyled.rememberModalBottomSheetState

import com.composeunstyled.Text
import com.composeunstyled.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Color
import com.mushi.app.resources.iconsax.IconsaxMusicPlaylist
import com.mushi.app.resources.iconsax.IconsaxTextalignLeft
import com.mushi.app.core.ui.pressScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.mushi.app.core.ui.swipeToChangeTrack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.LoopState
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.modules.downloads.DownloadProgressIcon
import com.mushi.app.modules.downloads.DownloadStatus
import com.mushi.app.modules.downloads.DownloadsViewModel
import com.mushi.app.modules.lyrics.LyricsViewModel
import com.mushi.app.modules.lyrics.SyncedLyricsList
import com.mushi.app.modules.saved_tracks.SAVED_TRACKS_COLLECTION_ID
import com.mushi.app.modules.saved_tracks.SavedTracksViewModel
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.Iconsax3DotsMore
import com.mushi.app.resources.iconsax.IconsaxArrowDown4
import com.mushi.app.resources.iconsax.IconsaxArrowSquareUp
import com.mushi.app.resources.iconsax.IconsaxCd
import com.mushi.app.resources.iconsax.IconsaxCheckCircle
import com.mushi.app.resources.iconsax.IconsaxCloseSquare
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.resources.iconsax.IconsaxMusicFilter
import com.mushi.app.resources.iconsax.IconsaxNext
import com.mushi.app.resources.iconsax.IconsaxPause
import com.mushi.app.resources.iconsax.IconsaxPlay
import com.mushi.app.resources.iconsax.IconsaxPrevious
import com.mushi.app.resources.iconsax.IconsaxRefreshRight
import com.mushi.app.resources.iconsax.IconsaxRepeatMusic
import com.mushi.app.resources.iconsax.IconsaxRepeateMusic
import com.mushi.app.resources.iconsax.IconsaxRepeateOne
import com.mushi.app.resources.iconsax.IconsaxShuffle
import com.mushi.app.resources.iconsax.InconsaxClock
import com.mushi.app.resources.iconsax.SwapHorizontal2
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds
import com.mushi.app.core.i18n.getString
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.downloads_status_cancelled
import com.mushi.app.generated.resources.downloads_status_failed
import com.mushi.app.generated.resources.downloads_status_queued
import com.mushi.app.generated.resources.player_alternative_source
import com.mushi.app.generated.resources.player_collapse_content_description
import com.mushi.app.generated.resources.player_disable_shuffle_content_description
import com.mushi.app.generated.resources.player_download
import com.mushi.app.generated.resources.player_download_already_downloaded
import com.mushi.app.generated.resources.player_download_retrying
import com.mushi.app.generated.resources.player_download_started
import com.mushi.app.generated.resources.player_download_status_downloaded
import com.mushi.app.generated.resources.player_download_status_downloading
import com.mushi.app.generated.resources.player_enable_shuffle_content_description
import com.mushi.app.generated.resources.player_expand_lyrics_content_description
import com.mushi.app.generated.resources.player_go_to_album
import com.mushi.app.generated.resources.player_loop_mode_all
import com.mushi.app.generated.resources.player_loop_mode_none
import com.mushi.app.generated.resources.player_loop_mode_one
import com.mushi.app.generated.resources.player_lyrics
import com.mushi.app.generated.resources.player_next_content_description
import com.mushi.app.generated.resources.player_now_playing
import com.mushi.app.generated.resources.player_options_content_description
import com.mushi.app.generated.resources.player_pause_content_description
import com.mushi.app.generated.resources.player_play_content_description
import com.mushi.app.generated.resources.player_previous_content_description
import com.mushi.app.generated.resources.player_seek_label
import com.mushi.app.generated.resources.player_sleep_timer
import com.mushi.app.generated.resources.player_track_details
import com.mushi.app.generated.resources.queue_title
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

// The expanded player on small screens
// on the top left it has a angle/chevron down that can be used to collapse the player back to the
// floating player
// on the top right it has a three dots for options
// The top bar can be used to swipe down and close the player back to the floating player. It should
// follow the user's finger while swiping down and should have a nice animation when collapsing back
// to the floating player
// On the middle the album art stays rounded, centered and takes up most of the space
// Below the album art, on the left the song title and artists are shown and on the right the like
// button is shown
// below that we have the playback controls and the progress bar, which should be similar to the
// large player but with a different layout to fit the smaller screen
// And at the very end we will have the volume slider
@Composable
fun AppExpandedPlayer(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onCollapse: () -> Unit = {},
    onQueue: () -> Unit = {},
    onAlternativeSource: () -> Unit = {},
    onExpandLyrics: () -> Unit = {},
    onDownloadTrack: () -> Unit = {},
    onGoToAlbum: () -> Unit = {},
    onSleepTimer: () -> Unit = {},
    audioPlayer: AudioPlayerInterface = koinInject(),
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    playerOptionsViewModel: PlayerOptionsViewModel = koinViewModel(),
    savedTracksViewModel: SavedTracksViewModel = koinViewModel<SavedTracksViewModel>(
        key = SAVED_TRACKS_COLLECTION_ID,
        parameters = { parametersOf() }
    )
) {
    val playerUiState = rememberPlayerUiState(audioPlayer, audioPlayerQueue)
    val jamRoomService: JamRoomService = koinInject()
    val isJamGuest by jamRoomService.role
        .map { it == JamRole.Guest }
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val downloadsViewModel: DownloadsViewModel = koinViewModel()
    val navigationCommands: NavigationCommands = koinInject()
    val currentEntry by audioPlayerQueue.currentQueueEntryFlow.collectAsStateWithLifecycle()
    val playerOptionsUiState by playerOptionsViewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack = remember(currentEntry) {
        (currentEntry as? QueueEntry.StreamingTrack)?.track
    }
    val downloads by downloadsViewModel.downloads.collectAsStateWithLifecycle()
    val currentDownload = remember(currentTrack, downloads) {
        currentTrack?.let { t ->
            downloads.lastOrNull { it.track?.id == t.id }
        }
    }
    val snackbarHostState = remember { MillenniumSnackbarHostState() }
    val albumArtModifier =
        rememberSharedAlbumArtModifier(sharedTransitionScope, animatedVisibilityScope)
    val coverModel = playerUiState.coverModel
    val playerPosition = rememberPlayerPosition(audioPlayer)
    val moreOptionsSheetState = rememberModalBottomSheetState(
        initialDetent = SheetDetent.FullyExpanded
    )

    PlayerOptionDialogs(viewModel = playerOptionsViewModel)

    // The artwork colours the top of the screen and fades into the normal background, so the
    // player follows each cover. The blend stays light enough that text keeps its contrast.
    val baseBackground = AppTheme.colors.background
    val artworkTint = rememberArtworkTint(coverModel)
    val tintStrength = if (baseBackground.luminance() < 0.5f) 0.75f else 0.22f
    val topTint by animateColorAsState(
        targetValue = artworkTint?.let { lerp(baseBackground, it, tintStrength) } ?: baseBackground,
        animationSpec = tween(durationMillis = 700),
        label = "player_tint")
    // The living background: soft blobs in the cover's own colours that drift while music plays.
    val artworkPalette = com.mushi.app.core.ui.component.animatedPalette(
        com.mushi.app.core.ui.theming.rememberArtworkPalette(coverModel)
            ?: artworkTint?.let { listOf(it, it, it) })
    // This screen stays composed while the player is collapsed to the mini player, so its
    // continuous animations only run while it is actually on screen; otherwise they would ask
    // for a new frame 60 times a second on every page while music plays.
    val sheetProgress = LocalPlayerTransition.current
    val playerOnScreen by androidx.compose.runtime.remember(sheetProgress) {
        androidx.compose.runtime.derivedStateOf { sheetProgress() > 0.01f }
    }
    val livingTime = com.mushi.app.core.ui.component.rememberLivingTime(
        moving = playerUiState.isPlaying && playerOnScreen)
    // The cover breathes gently while playing.
    val breath = com.mushi.app.core.ui.component.rememberBreathing(
        active = playerUiState.isPlaying && playerOnScreen)
    // The cover grows while playing and settles back a little when paused.
    val artScale by animateFloatAsState(
        targetValue = if (playerUiState.isPlaying) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "player_art_scale")

    val haptics = com.mushi.app.core.ui.rememberHaptics()
    val reduceMotion = com.mushi.app.core.ui.LocalReduceMotion.current
    val queue by audioPlayerQueue.queueFlow.collectAsStateWithLifecycle(emptyList())
    val coverSlide = remember { CoverSlideDirection() }
    val slideDirection = coverSlide.onIndex(queue.indexOf(currentEntry), queue.size)
    val like = rememberCurrentTrackLike(audioPlayerQueue, savedTracksViewModel)
    val currentLike by androidx.compose.runtime.rememberUpdatedState(like)
    val coverLike = remember { CoverLikeState() }

    fun onPlayPause() {
        haptics.tap()
        scope.launch {
            if (playerUiState.isPlaying) {
                audioPlayer.pause()
            } else {
                audioPlayer.play()
            }
        }
    }

    fun onSkipPrevious() {
        if (isJamGuest) return
        haptics.tick()
        scope.launch { audioPlayer.skipToPrevious() }
    }

    fun onSkipNext() {
        if (isJamGuest) return
        haptics.tick()
        scope.launch { audioPlayer.skipToNext() }
    }

    fun onShuffleToggle() {
        if (isJamGuest) return
        haptics.tick()
        scope.launch { audioPlayer.shuffle(!playerUiState.isShuffling) }
    }

    fun onLoopToggle() {
        if (isJamGuest) return
        haptics.tick()
        scope.launch { audioPlayer.loop(playerUiState.loopState.next()) }
    }

    fun onSeek(target: kotlin.time.Duration) {
        // A tick where the seek lands, so the new position feels set.
        haptics.tick()
        scope.launch { audioPlayer.seekTo(target) }
    }

    LaunchedEffect(moreOptionsSheetState.bottomSheetState.currentDetent) {
        if (moreOptionsSheetState.bottomSheetState.currentDetent == SheetDetent.Hidden) {
            playerOptionsViewModel.dismissMoreOptionsSheet()
        }
    }

    LaunchedEffect(playerOptionsUiState.isMoreOptionsSheetOpen) {
        if (playerOptionsUiState.isMoreOptionsSheetOpen) {
            moreOptionsSheetState.animateTo(SheetDetent.FullyExpanded)
        }
    }

    // Where the cover sits in this screen, measured so it can start at the mini player's cover.
    val playerTransition = LocalPlayerTransition.current
    var playerRootPos by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var coverHostPos by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var coverHostWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .livingBackdrop(artworkPalette, baseBackground, tintStrength, livingTime)
            .onGloballyPositioned { playerRootPos = it.positionInRoot() }
    ) {
        if (playerOptionsUiState.isMoreOptionsSheetOpen) {
            MillenniumModalBottomSheet(
                state = moreOptionsSheetState,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                contentSpacing = 4.dp
            ) {
                // Grid of Options
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OptionTile(
                            icon = com.mushi.app.core.ui.theming.MushiIcons.Swap,
                            label = stringResource(Res.string.player_alternative_source),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                onAlternativeSource()
                            })
                    }
                    item {
                        val download = currentDownload
                        val status = download?.status
                        val statusLabel = when (status) {
                            is DownloadStatus.Completed -> stringResource(Res.string.player_download_status_downloaded)
                            is DownloadStatus.Failed -> stringResource(Res.string.downloads_status_failed)
                            is DownloadStatus.Cancelled -> stringResource(Res.string.downloads_status_cancelled)
                            is DownloadStatus.Queued -> stringResource(Res.string.downloads_status_queued)
                            is DownloadStatus.Downloading -> stringResource(Res.string.player_download_status_downloading)
                            null -> stringResource(Res.string.player_download)
                        }
                        val statusColor = when (status) {
                            is DownloadStatus.Completed -> AppTheme.colors.primary
                            is DownloadStatus.Failed -> AppTheme.colors.error
                            else -> AppTheme.colors.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppTheme.colors.onSurface.copy(alpha = 0.08f))
                                .clickable {
                                    playerOptionsViewModel.dismissMoreOptionsSheet()
                                    when (status) {
                                        is DownloadStatus.Completed -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(getString(Res.string.player_download_already_downloaded))
                                            }
                                        }

                                        is DownloadStatus.Failed, is DownloadStatus.Cancelled -> {
                                            download?.let { downloadsViewModel.retry(it.id) }
                                            scope.launch {
                                                snackbarHostState.showSnackbar(getString(Res.string.player_download_retrying))
                                            }
                                        }

                                        is DownloadStatus.Downloading, is DownloadStatus.Queued -> {
                                            // already in progress
                                        }

                                        null -> {
                                            val track = currentTrack
                                            if (track != null) {
                                                downloadsViewModel.downloadTrack(track)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = getString(Res.string.player_download_started, track.title)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    onDownloadTrack()
                                }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (status is DownloadStatus.Downloading) {
                                    DownloadProgressIcon(
                                        track = currentTrack,
                                        icon = com.mushi.app.core.ui.theming.MushiIcons.Download,
                                        contentDescription = stringResource(Res.string.player_download),
                                        modifier = Modifier.size(28.dp),
                                        tint = AppTheme.colors.onSurface
                                    )
                                } else {
                                    val icon = when (status) {
                                        is DownloadStatus.Completed -> Iconsax.IconsaxCheckCircle
                                        is DownloadStatus.Failed -> com.mushi.app.core.ui.theming.MushiIcons.Close
                                        is DownloadStatus.Cancelled -> com.mushi.app.core.ui.theming.MushiIcons.Refresh
                                        is DownloadStatus.Queued -> com.mushi.app.core.ui.theming.MushiIcons.Clock
                                        null -> com.mushi.app.core.ui.theming.MushiIcons.Download
                                    }
                                    MillenniumIcon(
                                        imageVector = icon,
                                        contentDescription = statusLabel,
                                        modifier = Modifier.size(28.dp),
                                        tint = when (status) {
                                            is DownloadStatus.Completed -> AppTheme.colors.primary
                                            is DownloadStatus.Failed -> AppTheme.colors.error
                                            else -> AppTheme.colors.onSurface
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = statusLabel,
                                    style = AppTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = statusColor
                                )
                            }
                        }
                    }
                    item {
                        OptionTile(
                            icon = com.mushi.app.core.ui.theming.MushiIcons.Clock,
                            label = stringResource(Res.string.player_sleep_timer),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                playerOptionsViewModel.showSleepTimerDialog()
                                onSleepTimer()
                            })
                    }
                    item {
                        OptionTile(
                            icon = com.mushi.app.core.ui.theming.MushiIcons.Album,
                            label = stringResource(Res.string.player_go_to_album),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                val albumId = currentTrack?.album?.id
                                if (albumId != null) {
                                    navigationCommands.navigateTo(Routes.Album(albumId))
                                    onCollapse()
                                }
                                onGoToAlbum()
                            })
                    }
                    item {
                        OptionTile(
                            icon = com.mushi.app.core.ui.theming.MushiIcons.More,
                            label = stringResource(Res.string.player_track_details),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                playerOptionsViewModel.showTrackDetails()
                            })
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MillenniumIconButton(
                    onClick = onCollapse,
                    style = MillenniumIconButtonStyle.Quiet
                ) {
                    MillenniumIcon(
                        Iconsax.IconsaxArrowDown4,
                        contentDescription = stringResource(Res.string.player_collapse_content_description)
                    )
                }
                Text(
                    text = stringResource(Res.string.player_now_playing),
                    style = AppTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                MillenniumIconButton(
                    onClick = playerOptionsViewModel::showMoreOptionsSheet,
                    style = MillenniumIconButtonStyle.Quiet
                ) {
                    MillenniumIcon(com.mushi.app.core.ui.theming.MushiIcons.More, contentDescription = stringResource(Res.string.player_options_content_description))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 28.dp)
                    .onGloballyPositioned {
                        coverHostPos = it.positionInRoot()
                        coverHostWidth = it.size.width.toFloat()
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .aspectRatio(1f)
                        .then(albumArtModifier)
                        .swipeToChangeTrack(
                            enabled = !isJamGuest,
                            onNext = ::onSkipNext,
                            onPrevious = ::onSkipPrevious
                        )
                        // Long press shows track details, like Apple Music's credits.
                        // Double tap likes the track; it never takes a like away.
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    haptics.tap()
                                    playerOptionsViewModel.showTrackDetails()
                                },
                                onDoubleTap = {
                                    val track = currentLike
                                    if (track.trackId != null && track.isKnown && !track.isPending) {
                                        haptics.tap()
                                        coverLike.play()
                                        if (!track.isLiked) track.toggle()
                                    }
                                })
                        }
                        // Grow out of the mini player's cover: start at its position and size, end
                        // in place. Runs from the sheet's progress, so dragging scrubs it.
                        .graphicsLayer {
                            val width = coverHostWidth
                            val p = playerTransition()
                            if (width > 0f && p < 1f) {
                                val e = androidx.compose.animation.core.FastOutSlowInEasing.transform(p)
                                val artSize = width * 0.94f
                                val artLeft = (coverHostPos.x - playerRootPos.x) + (width - artSize) / 2f
                                val artTop = coverHostPos.y - playerRootPos.y
                                val startScale = MiniCoverSize.toPx() / artSize
                                val scale = startScale + (1f - startScale) * e
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                                scaleX = scale
                                scaleY = scale
                                translationX = (1f - e) * (MiniCoverStartX.toPx() - artLeft)
                                translationY = (1f - e) * (MiniCoverStartY.toPx() - artTop)
                            }
                        }
                        .graphicsLayer {
                            scaleX = artScale * breath.value
                            scaleY = artScale * breath.value
                        }
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.surfaceContainerHighest)
                ) {
                    // The next cover slides in from the side the queue is moving towards.
                    androidx.compose.animation.AnimatedContent(
                        targetState = coverModel,
                        transitionSpec = {
                            val fade = fadeIn(tween(420)) togetherWith fadeOut(tween(420))
                            if (reduceMotion) fade else {
                                val way = slideDirection
                                (slideInHorizontally(tween(420)) { it * way } + fadeIn(tween(420))) togetherWith
                                    (slideOutHorizontally(tween(420)) { -it * way } + fadeOut(tween(420)))
                            }
                        },
                        label = "player_cover"
                    ) { model ->
                        AsyncImage(
                            model = model,
                            contentDescription = playerUiState.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    CoverLikeBurst(coverLike, Modifier.fillMaxSize())
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerUiState.title,
                        style = AppTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    PlayerArtistsText(
                        artists = playerUiState.artistList,
                        onArtistClick = { artistId ->
                            navigationCommands.navigateTo(Routes.Artist(artistId))
                            // The page opens beneath the player, so get out of the way
                            onCollapse()
                        },
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
                KeepMomentButton()
                PlayerHeartButton(
                    audioPlayerQueue = audioPlayerQueue,
                    savedTracksViewModel = savedTracksViewModel
                )
            }

            Column(modifier = Modifier.padding(top = 20.dp)) {
                PlayerSeekBar(
                    position = playerPosition,
                    seekDuration = playerUiState.seekDuration,
                    displayDuration = playerUiState.displayDuration,
                    onSeek = ::onSeek,
                    timeStyle = AppTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    timesTopPadding = 2.dp,
                    // Driven by the backdrop's clock, so it adds no frames and stops with it.
                    wave = livingTime,
                    waving = playerUiState.isPlaying && !reduceMotion
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MillenniumIconButton(
                        onClick = ::onShuffleToggle,
                        enabled = !isJamGuest,
                        style = MillenniumIconButtonStyle.Quiet
                    ) {
                        MillenniumIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.Shuffle,
                            contentDescription = stringResource(if (playerUiState.isShuffling) Res.string.player_disable_shuffle_content_description else Res.string.player_enable_shuffle_content_description),
                            tint = if (playerUiState.isShuffling) {
                                AppTheme.colors.primary
                            } else {
                                AppTheme.colors.onSurfaceVariant
                            }
                        )
                    }
                    MillenniumIconButton(
                        onClick = ::onSkipPrevious,
                        enabled = !isJamGuest,
                        style = MillenniumIconButtonStyle.Quiet
                    ) {
                        MillenniumIcon(com.mushi.app.core.ui.theming.MushiIcons.SkipPrevious, contentDescription = stringResource(Res.string.player_previous_content_description))
                    }
                    MillenniumIconButton(
                        onClick = ::onPlayPause,
                        modifier = Modifier.size(72.dp).pressScale(0.92f),
                        style = MillenniumIconButtonStyle.Primary
                    ) {
                        com.mushi.app.core.ui.component.PlayPauseGlyph(
                            isPlaying = playerUiState.isPlaying,
                            contentDescription = stringResource(if (playerUiState.isPlaying) Res.string.player_pause_content_description else Res.string.player_play_content_description),
                            modifier = Modifier.size(34.dp),
                            tint = LocalTextStyle.current.color
                        )
                    }
                    MillenniumIconButton(
                        onClick = ::onSkipNext,
                        enabled = !isJamGuest,
                        style = MillenniumIconButtonStyle.Quiet
                    ) {
                        MillenniumIcon(com.mushi.app.core.ui.theming.MushiIcons.SkipNext, contentDescription = stringResource(Res.string.player_next_content_description))
                    }
                    MillenniumIconButton(
                        onClick = ::onLoopToggle,
                        enabled = !isJamGuest,
                        style = MillenniumIconButtonStyle.Quiet
                    ) {
                        MillenniumIcon(
                            imageVector = when (playerUiState.loopState) {
                                LoopState.NONE -> com.mushi.app.core.ui.theming.MushiIcons.Repeat
                                LoopState.ONE -> com.mushi.app.core.ui.theming.MushiIcons.RepeatOne
                                LoopState.ALL -> com.mushi.app.core.ui.theming.MushiIcons.Repeat
                            },
                            contentDescription = stringResource(when (playerUiState.loopState) { LoopState.NONE -> Res.string.player_loop_mode_none; LoopState.ONE -> Res.string.player_loop_mode_one; LoopState.ALL -> Res.string.player_loop_mode_all }),
                            tint = if (playerUiState.loopState == LoopState.NONE) {
                                AppTheme.colors.onSurfaceVariant
                            } else {
                                AppTheme.colors.primary
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlayerActionChip(
                    icon = com.mushi.app.core.ui.theming.MushiIcons.AlignLeft,
                    label = stringResource(Res.string.player_lyrics),
                    onClick = onExpandLyrics,
                    modifier = Modifier.weight(1f)
                )
                PlayerActionChip(
                    icon = com.mushi.app.core.ui.theming.MushiIcons.Playlist,
                    label = stringResource(Res.string.queue_title),
                    onClick = onQueue,
                    modifier = Modifier.weight(1f)
                )
            }

            LyricsPreviewCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                containerColor = lerp(baseBackground, topTint, 0.9f),
                onExpand = onExpandLyrics
            )
        }

        MillenniumSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun rememberSharedAlbumArtModifier(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return Modifier

    with(sharedTransitionScope) {
        return Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(key = "player_album_art"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun LyricsPreviewCard(
    modifier: Modifier = Modifier,
    containerColor: Color,
    onExpand: () -> Unit,
    viewModel: LyricsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncedLyrics = uiState.syncedLyrics ?: emptyList()
    val isLoading = uiState.isLoading
    if (syncedLyrics.isEmpty() || isLoading) return

    // The card takes the cover's colour, like the lyrics card in Spotify.
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .pressScale(0.98f)
            .clickable(onClick = onExpand)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.player_lyrics),
                style = AppTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.onSurface
            )
            MillenniumIcon(
                Iconsax.IconsaxArrowSquareUp,
                contentDescription = stringResource(Res.string.player_expand_lyrics_content_description),
                tint = AppTheme.colors.onSurface.copy(alpha = 0.8f)
            )
        }

        // Passive preview: no onLineClick, so lines never seek playback.
        SyncedLyricsList(
            lines = syncedLyrics,
            currentIndex = uiState.currentIndex,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
    }
}

/** A full-width pill under the player controls, with an icon, used for Lyrics and Queue. */
@Composable
private fun PlayerActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(50.dp)
            .pressScale(0.96f)
            .clip(RoundedCornerShape(25.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MillenniumIcon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = AppTheme.colors.onSurface
        )
        Text(
            text = label,
            style = AppTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun OptionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MillenniumCard(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(12.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MillenniumIcon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = AppTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = AppTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}
