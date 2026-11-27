package com.mushi.app.core.ui.component



import com.mushi.app.core.ui.listItemMotion
import com.mushi.app.core.ui.rememberStaggerState
import com.mushi.app.core.ui.staggeredEntrance
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.state.ToggleableState
import org.spotube.millennium.composables.MillenniumCard
import org.spotube.millennium.composables.MillenniumCheckbox
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumListTile
import org.spotube.millennium.composables.MillenniumSeparator
import org.spotube.millennium.composables.MillenniumTextField
import org.spotube.millennium.composables.MillenniumTriStateCheckbox

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.animation.core.animateFloat
import com.mushi.app.core.ui.pressScale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.ui.misc.SkeletonTree
import com.mushi.app.core.ui.misc.uniqueLazyKeys
import com.mushi.app.core.ui.misc.TextWithShimmer
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.getPlatform
import com.mushi.app.isDesktop
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.Iconsax3DotsMore
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.resources.iconsax.IconsaxFilterSearch
import com.mushi.app.resources.iconsax.IconsaxMusicPlaylist
import com.mushi.app.resources.iconsax.IconsaxNext
import com.mushi.app.resources.iconsax.IconsaxPause
import com.mushi.app.resources.iconsax.IconsaxPlay
import com.mushi.app.resources.iconsax.IconsaxSort
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

private val CompactTrackListBreakpoint = 600.dp
private val LargeTrackListBreakpoint = 840.dp
private const val ShimmerRowCount = 6

private val TrackCardOuterPadding = 8.dp
private val TrackCardInnerPadding = 12.dp
private const val TrackCardTopKey = "track-card-top"
private const val TrackCardRowKeyPrefix = "track-card-row:"
private const val TrackCardFooterKey = "track-card-footer"
private const val TrackCardBottomKey = "track-card-bottom"

private fun Any.isTrackCardKey() = this is String && startsWith("track-card-")

/**
 * Sizes the card drawn behind a [TrackList]'s rows to the rows the list has laid out. Edges that
 * are scrolled away are pushed a few viewports past the screen, far enough that the card's rounded
 * corners, shadow and edge gradient stay off-screen, while the height stays bounded for huge lists.
 */
private fun Modifier.trackCardBounds(listState: LazyListState, edgeInsetPx: Int): Modifier =
    layout { measurable, constraints ->
        val info = listState.layoutInfo
        val cardItems = info.visibleItemsInfo.filter { it.key.isTrackCardKey() }
        if (cardItems.isEmpty()) return@layout layout(constraints.maxWidth, constraints.maxHeight) {}

        // Item offsets are relative to the viewport start, which is negative by the top padding.
        val toLocal = -info.viewportStartOffset
        val viewport = info.viewportEndOffset - info.viewportStartOffset
        val overscan = viewport * 3
        val first = cardItems.first()
        val last = cardItems.last()
        val top = if (first.key == TrackCardTopKey) first.offset + toLocal + edgeInsetPx else -overscan
        val bottom = if (last.key == TrackCardBottomKey) {
            last.offset + last.size + toLocal - edgeInsetPx
        } else viewport + overscan
        val height = (bottom - top).coerceAtLeast(0)
        val placeable = measurable.measure(
            constraints.copy(minHeight = height, maxHeight = height))
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, top) }
    }

enum class TrackSortOption(val label: StringResource) {
    None(Res.string.track_list_sort_original),
    Title(Res.string.track_list_sort_title),
    Artist(Res.string.track_list_sort_artist),
    Album(Res.string.track_list_sort_album),
    Duration(Res.string.track_list_sort_duration),
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackList(
    tracks: List<MetadataTrack> = emptyList(),
    error: String? = null,
    hasMore: Boolean = false,
    isLoading: Boolean = false,
    isLoadingNextPage: Boolean = false,
    showEmptyMessage: Boolean = true,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    onLoadNextPage: () -> Unit = {},
    onTrackClick: (MetadataTrack) -> Unit = {},
    onTrackOptionsAction: (MetadataTrack, TrackOptionsAction) -> Unit = { _, _ -> },
    onArtistClick: (MetadataArtist.Basic) -> Unit = {},
    onAlbumClick: (MetadataAlbum.Detailed) -> Unit = {},
    onArtistsOverflowClick: (MetadataTrack) -> Unit = {},
    onBulkDownload: (List<MetadataTrack>) -> Unit = {},
    onBulkAddToQueue: (List<MetadataTrack>) -> Unit = {},
    onBulkPlayNext: (List<MetadataTrack>) -> Unit = {},
    onBulkAddToPlaylist: (List<MetadataTrack>) -> Unit = {},
    onBulkAddToJam: (List<MetadataTrack>) -> Unit = {},
    isInJam: Boolean = false,
    isJamGuest: Boolean = false,
    currentTrackId: String? = null,
    isCurrentTrackPlaying: Boolean = false,
    trackOptionsState: (MetadataTrack) -> TrackOptionsState = { TrackOptionsState() },
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = LocalAppShellBottomInset.current),
    simplified: Boolean = false,
    scrollable: Boolean = true,
    state: LazyListState? = null) {
    val filterState = rememberTextFieldState()
    val filterQuery = filterState.text.toString()
    var sortBy by rememberSaveable { mutableStateOf(TrackSortOption.None) }
    var selectedTrackForOptions by remember { mutableStateOf<MetadataTrack?>(null) }
    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedTrackIds by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }

    val normalizedQuery = remember(filterQuery) { filterQuery.trim().lowercase() }
    val visibleTracks = remember(tracks, normalizedQuery, sortBy) {
        val filtered = if (normalizedQuery.isBlank()) {
            tracks
        } else {
            tracks.filter { track ->
                val searchableText = buildString {
                    append(track.title).append(' ')
                    append(track.album?.title.orEmpty()).append(' ')
                    append(track.artists.joinToString(" ") { it.name })
                }.lowercase()
                searchableText.contains(normalizedQuery)
            }
        }

        when (sortBy) {
            TrackSortOption.None -> filtered
            TrackSortOption.Title -> filtered.sortedBy { it.title.lowercase() }
            TrackSortOption.Artist -> filtered.sortedBy {
                it.artists.firstOrNull()?.name?.lowercase().orEmpty()
            }

            TrackSortOption.Album -> filtered.sortedBy { it.album?.title?.lowercase() }
            TrackSortOption.Duration -> filtered.sortedBy { it.durationMs }
        }
    }

    val listState = state ?: rememberLazyListState()
    val density = LocalDensity.current
    val shouldLoadMore = remember(density) {
        derivedStateOf {
            val loadMoreThreshold = with(density) { 200.dp.toPx() }
            val cardItem = listState.layoutInfo.visibleItemsInfo.find { it.key == TrackCardBottomKey }
            // Trigger when the bottom of the track card is close to/inside the viewport,
            // which means the user has scrolled through (or is about to scroll through)
            // all loaded tracks.
            cardItem != null && cardItem.offset + cardItem.size <= listState.layoutInfo.viewportEndOffset + loadMoreThreshold
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && hasMore && !isLoading && !isLoadingNextPage) {
            onLoadNextPage()
        }
    }

    val widthBreakpoint = currentWindowWidthBreakpoint()
    val isCompact = widthBreakpoint isBelow WindowBreakpoints.Medium
    val isDesktop = remember { getPlatform().isDesktop() }
    val showIndex = widthBreakpoint isAtLeast WindowBreakpoints.Expanded
    val showAlbum = !isCompact
    val useDropdownForOptions = isDesktop && !isCompact

    val trackRow: @Composable (Int, MetadataTrack) -> Unit = { displayedIndex, track ->
        androidx.compose.runtime.key(track.id) {
        com.mushi.app.core.ui.SwipeToQueue(
            enabled = !isDesktop && !isSelectionMode && !trackOptionsState(track).isBlacklisted,
            onQueue = {
                onTrackOptionsAction(
                    track,
                    if (isJamGuest) TrackOptionsAction.AddToJam else TrackOptionsAction.AddToQueue)
            }
        ) {
        TrackListRow(
            index = displayedIndex + 1,
            track = track,
            showIndex = showIndex,
            showAlbum = showAlbum,
            useDropdownForOptions = useDropdownForOptions,
            isCurrentTrack = track.id == currentTrackId,
            // Only the current row depends on it, so play/pause/buffering flips don't recompose every row.
            isCurrentTrackPlaying = isCurrentTrackPlaying && track.id == currentTrackId,
            isSelectionMode = isSelectionMode,
            isSelected = selectedTrackIds.contains(track.id),
            onTrackClick = {
                if (isSelectionMode) {
                    selectedTrackIds =
                        if (selectedTrackIds.contains(track.id)) {
                            selectedTrackIds - track.id
                        } else {
                            selectedTrackIds + track.id
                        }
                } else if (isJamGuest) {
                    onTrackOptionsAction(track, TrackOptionsAction.AddToJam)
                } else {
                    onTrackClick(track)
                }
            },
            onLongClick = {
                if (!useDropdownForOptions && !isSelectionMode) {
                    isSelectionMode = true
                    selectedTrackIds = setOf(track.id)
                } else if (!useDropdownForOptions) {
                    selectedTrackForOptions = track
                }
            },
            onSelectionToggle = { checked ->
                isSelectionMode = true
                selectedTrackIds = if (checked) {
                    selectedTrackIds + track.id
                } else {
                    selectedTrackIds - track.id
                }
            },
            onTrackOptionsAction = { action ->
                onTrackOptionsAction(
                    track,
                    action
                )
            },
            trackOptionsState = trackOptionsState(track),
            isInJam = isInJam,
            isJamGuest = isJamGuest,
            onShowOptionsClick = { selectedTrackForOptions = track },
            onArtistClick = onArtistClick,
            onAlbumClick = onAlbumClick,
            onArtistsOverflowClick = { onArtistsOverflowClick(track) })
        }
        }
    }

    // Shimmer, error, empty and next-page rows at the end of the card.
    val trackCardFooter: @Composable () -> Unit = {
        if (isLoading && tracks.isEmpty()) {
            repeat(ShimmerRowCount) { shimmerIndex ->
                if (visibleTracks.isNotEmpty() || shimmerIndex > 0) {
                    MillenniumSeparator(
                        modifier = Modifier.padding(horizontal = 8.dp))
                }
                ShimmerTrackListRow(
                    index = shimmerIndex + 1,
                    showIndex = showIndex,
                    showAlbum = showAlbum,
                    useDropdownForOptions = useDropdownForOptions)
            }
        }

        if (error != null) {
            if (visibleTracks.isNotEmpty()) {
                MillenniumSeparator(
                    modifier = Modifier.padding(horizontal = 8.dp))
            }
            TrackListFeedbackRow(
                message = stringResource(Res.string.error_try_again),
                isError = true)
        }

        if (showEmptyMessage && !isLoading && visibleTracks.isEmpty() && error == null) {
            TrackListFeedbackRow(stringResource(Res.string.track_list_no_tracks))
        }

        if (isLoadingNextPage) {
            if (visibleTracks.isNotEmpty()) {
                MillenniumSeparator(
                    modifier = Modifier.padding(horizontal = 8.dp))
            }
            ShimmerTrackListRow(
                index = visibleTracks.size + 1,
                showIndex = showIndex,
                showAlbum = showAlbum,
                useDropdownForOptions = useDropdownForOptions)
        }
    }

    val trackCardContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TrackCardOuterPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TrackCardInnerPadding, bottom = TrackCardInnerPadding)
            ) {
                visibleTracks.forEachIndexed { displayedIndex, track ->
                    if (displayedIndex > 0) {
                        TrackRowDivider()
                    }
                    trackRow(displayedIndex, track)
                }
                trackCardFooter()
            }
        }
    }

    val filterSortRow: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                .heightIn(max = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)) {
            if (showIndex || isSelectionMode) {
                val allSelected =
                    visibleTracks.isNotEmpty() && selectedTrackIds.size == visibleTracks.size
                val anySelected = selectedTrackIds.isNotEmpty()
                val headerState = when {
                    allSelected -> ToggleableState.On
                    anySelected -> ToggleableState.Indeterminate
                    else -> ToggleableState.Off
                }
                MillenniumTriStateCheckbox(
                    value = headerState,
                    onClick = {
                        if (allSelected) {
                            isSelectionMode = false
                            selectedTrackIds = emptySet()
                        } else {
                            isSelectionMode = true
                            selectedTrackIds = visibleTracks.map { it.id }.toSet()
                        }
                    },
                    label = "")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                MillenniumTextField(
                    state = filterState,
                    label = "",
                    placeholder = stringResource(Res.string.track_list_filter_placeholder),
                    modifier = Modifier.fillMaxWidth())
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center) {
                    AdaptiveDropdownBottomSheet(
                        items = TrackSortOption.entries.map { option ->
                            AdaptiveMenuItem(
                                label = stringResource(option.label),
                                onClick = { sortBy = option },
                                selected = sortBy == option)
                        },
                        trigger = { onClick ->
                            // A pill that names the current order, so sorting is visible at a glance.
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .pressScale(0.96f)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                                    .background(AppTheme.colors.onSurface.copy(alpha = 0.08f))
                                    .clickable(onClick = onClick)
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MillenniumIcon(
                                    imageVector = com.mushi.app.core.ui.theming.MushiIcons.Sort,
                                    contentDescription = stringResource(Res.string.track_list_sort),
                                    modifier = Modifier.size(16.dp))
                                Text(
                                    text = stringResource(sortBy.label),
                                    style = AppTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.onSurface,
                                    maxLines = 1)
                            }
                        })
                }
                Box(
                    contentAlignment = Alignment.Center) {
                    val targetTracks = if (selectedTrackIds.isNotEmpty()) {
                        visibleTracks.filter { selectedTrackIds.contains(it.id) }
                    } else {
                        visibleTracks
                    }
                    val trackCount = targetTracks.size
                    val isAll =
                        selectedTrackIds.isEmpty() || trackCount == visibleTracks.size
                    AdaptiveDropdownBottomSheet(
                        items = buildList {
                            add(
                                AdaptiveMenuItem(
                                    icon = com.mushi.app.core.ui.theming.MushiIcons.Download,
                                    label = if (isAll) stringResource(Res.string.track_list_download_all) else stringResource(Res.string.track_list_download_count, trackCount),
                                    onClick = { onBulkDownload(targetTracks) }))
                            if (!isJamGuest) {
                                add(
                                    AdaptiveMenuItem(
                                        icon = Iconsax.IconsaxAddSquare,
                                        label = if (isAll) stringResource(Res.string.track_list_add_all_to_queue) else stringResource(Res.string.track_list_add_count_to_queue, trackCount),
                                        onClick = { onBulkAddToQueue(targetTracks) }))
                                add(
                                    AdaptiveMenuItem(
                                        icon = com.mushi.app.core.ui.theming.MushiIcons.SkipNext,
                                        label = if (isAll) stringResource(Res.string.track_list_play_all_next) else stringResource(Res.string.track_list_play_count_next, trackCount),
                                        onClick = { onBulkPlayNext(targetTracks) }))
                            }
                            add(
                                AdaptiveMenuItem(
                                    icon = com.mushi.app.core.ui.theming.MushiIcons.Playlist,
                                    label = if (isAll) stringResource(Res.string.track_list_add_all_to_playlist) else stringResource(Res.string.track_list_add_count_to_playlist, trackCount),
                                    onClick = { onBulkAddToPlaylist(targetTracks) }))
                        } + if (isInJam) {
                            listOf(
                                AdaptiveMenuItem(
                                    icon = Iconsax.IconsaxAddSquare,
                                    label = if (isAll) stringResource(Res.string.track_list_add_all_to_jam) else stringResource(Res.string.track_list_add_count_to_jam, trackCount),
                                    onClick = { onBulkAddToJam(targetTracks) }))
                        } else {
                            emptyList()
                        },
                        trigger = { onClick ->
                            MillenniumIconButton(
                                onClick = onClick,
                                style = MillenniumIconButtonStyle.Quiet) {
                                MillenniumIcon(
                                    imageVector = com.mushi.app.core.ui.theming.MushiIcons.More,
                                    contentDescription = stringResource(Res.string.track_list_bulk_actions))
                            }
                        })
                }
            }
        }
    }

    if (scrollable) {
        Box(modifier = modifier.fillMaxWidth()) {
          Box(
              modifier = Modifier
                  .widthIn(max = 1280.dp)
                  .align(Alignment.TopCenter)
                  .padding(horizontal = if (isCompact) 6.dp else 16.dp, vertical = 8.dp)) {
            // Rows are separate lazy items (only on-screen ones are composed, #3200), so the card
            // they sit on is drawn once behind the list, spanning the card's laid-out items.
            // No framed card behind the rows: they sit directly on the page, separated by hairlines.
            // No arrangement spacing: it would also open gaps between the card's rows. The 2.dp
            // gaps around the card are applied to the items before and after it instead.
            val rowStagger = rememberStaggerState(key = tracks.isEmpty())
            LazyColumn(
                state = listState,
                contentPadding = contentPadding) {
                if (headerContent != null) {
                    item {
                        Box(Modifier.padding(bottom = 2.dp)) { headerContent() }
                    }
                }
                if (!simplified) {
                    item { Box(Modifier.padding(bottom = 2.dp)) { filterSortRow() } }
                }
                item(key = TrackCardTopKey) {
                    Spacer(Modifier.fillMaxWidth().height(TrackCardOuterPadding + TrackCardInnerPadding))
                }
                // Plugin ids can repeat (the same track twice in a playlist).
                val rowKeys = uniqueLazyKeys(visibleTracks) { it.id }
                items(visibleTracks.size, key = { TrackCardRowKeyPrefix + rowKeys[it] }) { displayedIndex ->
                    // Rows rise in one after another when the tracks first arrive, and glide to
                    // their new place when the list is sorted or filtered.
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .then(listItemMotion())
                            .staggeredEntrance(rowStagger, displayedIndex)) {
                        if (displayedIndex > 0) {
                            TrackRowDivider()
                        }
                        trackRow(displayedIndex, visibleTracks[displayedIndex])
                    }
                }
                item(key = TrackCardFooterKey) {
                    Column(Modifier.fillMaxWidth()) { trackCardFooter() }
                }
                item(key = TrackCardBottomKey) {
                    Spacer(Modifier.fillMaxWidth().height(TrackCardInnerPadding + TrackCardOuterPadding))
                }
                if (footerContent != null) {
                    item {
                        Box(Modifier.padding(top = 2.dp)) { footerContent() }
                    }
                }
            }
          }

            if (!useDropdownForOptions) {
                selectedTrackForOptions?.let { track ->
                    TrackOptionsBottomSheet(
                        track = track,
                        state = trackOptionsState(track),
                        onDismiss = { selectedTrackForOptions = null },
                        onAction = { action ->
                            onTrackOptionsAction(track, action)
                            selectedTrackForOptions = null
                        },
                        onAlbumClick = { track.album?.let { onAlbumClick(it) } },
                        isInJam = isInJam,
                        isJamGuest = isJamGuest)
                }
            }
            VerticalScrollbar(listState, modifier = Modifier.align(Alignment.CenterEnd))
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 6.dp else 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (headerContent != null) {
                headerContent()
            }
            if (!simplified) {
                filterSortRow()
            }
            trackCardContent()
            if (footerContent != null) {
                footerContent()
            }
        }

        if (!useDropdownForOptions) {
            selectedTrackForOptions?.let { track ->
                TrackOptionsBottomSheet(
                    track = track,
                    state = trackOptionsState(track),
                    onDismiss = { selectedTrackForOptions = null },
                    onAction = { action ->
                        onTrackOptionsAction(track, action)
                        selectedTrackForOptions = null
                    },
                    onAlbumClick = { track.album?.let { onAlbumClick(it) } })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackListRow(
    index: Int,
    track: MetadataTrack,
    showIndex: Boolean,
    showAlbum: Boolean,
    useDropdownForOptions: Boolean,
    isCurrentTrack: Boolean,
    isCurrentTrackPlaying: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onTrackClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectionToggle: (Boolean) -> Unit,
    onTrackOptionsAction: (TrackOptionsAction) -> Unit,
    trackOptionsState: TrackOptionsState,
    isInJam: Boolean,
    isJamGuest: Boolean,
    onShowOptionsClick: () -> Unit,
    onArtistClick: (MetadataArtist.Basic) -> Unit,
    onAlbumClick: (MetadataAlbum.Detailed) -> Unit,
    onArtistsOverflowClick: () -> Unit,
    modifier: Modifier = Modifier) {
    val artworkInteractionSource = remember { MutableInteractionSource() }
    val isArtworkHovered by artworkInteractionSource.collectIsHoveredAsState()
    val rowInteractionSource = remember { MutableInteractionSource() }
    val isRowHovered by rowInteractionSource.collectIsHoveredAsState()
    val rowBackgroundColor = when {
        isSelected -> AppTheme.colors.secondary.copy(alpha = 0.12f)
        isCurrentTrack && isCurrentTrackPlaying -> AppTheme.colors.primaryContainer
        isCurrentTrack -> AppTheme.colors.surfaceVariant
        trackOptionsState.isBlacklisted -> AppTheme.colors.error.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    MillenniumListTile(
        modifier = modifier,
        selected = isSelected || isCurrentTrack,
        onClick = onTrackClick,
        title = {
            // The track that's playing is named in the accent colour.
            TextWithShimmer(
                text = track.title,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrentTrack) AppTheme.colors.primary else AppTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        },
        subtitle = {
            ArtistChips(
                artists = track.artists,
                onArtistClick = onArtistClick,
                onArtistsOverflowClick = onArtistsOverflowClick)
        },
        leading = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isSelectionMode) {
                    MillenniumCheckbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectionToggle(it) },
                        label = "",
                        modifier = Modifier.shimmerApply())
                } else if (showIndex) {
                    Box(
                        modifier = Modifier.width(30.dp),
                        contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = isRowHovered,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "index-checkbox") { hovered ->
                            if (hovered) {
                                MillenniumCheckbox(
                                    checked = isSelected,
                                    onCheckedChange = { onSelectionToggle(it) },
                                    label = "",
                                    modifier = Modifier.shimmerApply())
                            } else {
                                TextWithShimmer(
                                    text = index.toString(),
                                    style = AppTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    color = AppTheme.colors.onSurfaceVariant)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .hoverable(interactionSource = artworkInteractionSource),
                    contentAlignment = Alignment.Center) {
                    val imageUrl = (track.album?.thumbnails ?: track.thumbnails)?.firstOrNull()?.url
                    val platformContext = LocalPlatformContext.current
                    val imageRequest = remember(imageUrl) {
                        ImageRequest.Builder(platformContext)
                            .data(imageUrl)
                            .size(128)
                            .crossfade(false)
                            .build()
                    }

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().shimmerApply())

                    if (isArtworkHovered || isCurrentTrack) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center) {
                            if (isCurrentTrack && isCurrentTrackPlaying && !isArtworkHovered) {
                                EqualizerBars(modifier = Modifier.size(18.dp))
                            } else MillenniumIcon(
                                imageVector = if (isCurrentTrack && isCurrentTrackPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                                contentDescription = if (isCurrentTrack && isCurrentTrackPlaying) stringResource(Res.string.player_pause_content_description) else stringResource(Res.string.player_play_content_description),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp).shimmerApply())
                        }
                    }
                }
            }
        },
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showAlbum && track.album != null) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val colorScheme = AppTheme.colors

                    TextWithShimmer(
                        text = track.album!!.title,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .widthIn(max = 220.dp)
                            .hoverable(interactionSource)
                            .clickable(
                                indication = null,
                                interactionSource = interactionSource) { onAlbumClick(track.album!!) }
                            .then(
                                if (isHovered || isPressed) {
                                    Modifier.drawWithCache {
                                        val underlineHeight = 1.dp.toPx()
                                        onDrawWithContent {
                                            drawContent()
                                            drawLine(
                                                color = colorScheme.primary,
                                                start = Offset(0f, size.height - underlineHeight),
                                                end = Offset(size.width, size.height - underlineHeight),
                                                strokeWidth = underlineHeight)
                                        }
                                    }
                                } else {
                                    Modifier
                                }))
                }

                TextWithShimmer(
                    text = track.durationMs.toDurationString(),
                    style = AppTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    textAlign = TextAlign.End,
                    color = AppTheme.colors.onSurfaceVariant,
                    modifier = Modifier.width(56.dp))

                if (useDropdownForOptions) {
                    TrackOptions(
                        track = track,
                        state = trackOptionsState,
                        onAction = onTrackOptionsAction,
                        onAlbumClick = { track.album?.let { onAlbumClick(it) } },
                        isInJam = isInJam,
                        isJamGuest = isJamGuest)
                } else {
                    MillenniumIconButton(
                        onClick = onShowOptionsClick,
                        style = MillenniumIconButtonStyle.Quiet) {
                        MillenniumIcon(
                            imageVector = com.mushi.app.core.ui.theming.MushiIcons.More,
                            contentDescription = stringResource(Res.string.track_options_title),
                            tint = AppTheme.colors.onSurfaceVariant)
                    }
                }
            }
        })
}

/** A thin line between rows, inset to start under the title so it doesn't cut the artwork. */
@Composable
private fun TrackRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp, end = 16.dp)
            .height(0.5.dp)
            .background(AppTheme.colors.outlineVariant.copy(alpha = 0.6f)))
}

@Composable
private fun ArtistChips(
    artists: List<MetadataArtist.Basic>,
    onArtistClick: (MetadataArtist.Basic) -> Unit,
    onArtistsOverflowClick: () -> Unit) {
    val visibleArtists = artists.take(2)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()) {
        visibleArtists.forEachIndexed { index, artist ->
            Row(
                verticalAlignment = Alignment.CenterVertically) {
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                val isPressed by interactionSource.collectIsPressedAsState()

                TextWithShimmer(
                    text = artist.name,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isHovered || isPressed) TextDecoration.Underline else null,
                    modifier = Modifier
                        .clickable(
                        indication = null,
                        interactionSource = interactionSource
                    ) { onArtistClick(artist) }
                    )
                if (index < visibleArtists.size - 1)
                    Text(
                        ",",
                        color = AppTheme.colors.onSurfaceVariant,
                        style = AppTheme.typography.bodySmall
                    )
            }
        }

        val remaining = artists.size - visibleArtists.size
        if (remaining > 0) {
            TextWithShimmer(
                text = stringResource(Res.string.track_list_more_artists, remaining),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onArtistsOverflowClick))
        }
    }
}

@Composable
private fun ShimmerTrackListRow(
    index: Int,
    showIndex: Boolean,
    showAlbum: Boolean,
    useDropdownForOptions: Boolean) {
    val dummyTrack = remember(index) { dummyTrackForShimmer(index) }
    SkeletonTree(true) {
        TrackListRow(
            index = index,
            track = dummyTrack,
            showIndex = showIndex,
            showAlbum = showAlbum,
            useDropdownForOptions = useDropdownForOptions,
            isCurrentTrack = false,
            isCurrentTrackPlaying = false,
            isSelectionMode = false,
            isSelected = false,
            onTrackClick = {},
            onLongClick = {},
            onSelectionToggle = {},
            onTrackOptionsAction = {},
            trackOptionsState = TrackOptionsState(),
            isInJam = false,
            isJamGuest = false,
            onShowOptionsClick = {},
            onArtistClick = {},
            onAlbumClick = {},
            onArtistsOverflowClick = {})
    }
}

private fun dummyTrackForShimmer(index: Int): MetadataTrack {
    val artist = MetadataArtist.Basic(
        id = "shimmer_artist_$index",
        name = "Artist Name",
        thumbnails = emptyList(),
        externalUri = null)
    return MetadataTrack(
        id = "shimmer_track_$index",
        title = "Track Title Here",
        durationMs = 210_000,
        trackNumber = index,
        discNumber = 1,
        artists = listOf(
            artist,
            artist.copy(id = "shimmer_artist_${index}_2", name = "Another Artist")
        ),
        album = MetadataAlbum.Detailed(
            releaseDate = null,
            genres = emptyList(),
            trackCount = 10,
            id = "shimmer_album_$index",
            title = "Album Name",
            description = null,
            thumbnails = emptyList(),
            albumType = MetadataAlbumType.Album,
            artists = listOf(artist),
            externalUri = null),
        thumbnails = null,
        explicit = false,
        popularity = 0,
        isrcCode = null,
        externalUri = null)
}

@Composable
private fun TrackListFeedbackRow(
    message: String,
    isError: Boolean = false) {
    TextWithShimmer(
        text = message,
        color = if (isError) AppTheme.colors.error else AppTheme.colors.onSurfaceVariant,
        style = AppTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp))
}

private fun Long.toDurationString(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

fun previewTracks(): List<MetadataTrack> {
    val artists = listOf(
        MetadataArtist.Basic(
            id = "artist_1",
            name = "Aria Vale",
            thumbnails = emptyList(),
            externalUri = null),
        MetadataArtist.Basic(
            id = "artist_2",
            name = "Neon Harbor",
            thumbnails = emptyList(),
            externalUri = null),
        MetadataArtist.Basic(
            id = "artist_3",
            name = "Echo Drift",
            thumbnails = emptyList(),
            externalUri = null))

    val album = MetadataAlbum.Detailed(
        releaseDate = null,
        genres = emptyList(),
        trackCount = 10,
        id = "album_1",
        title = "Night Drive Archives",
        description = null,
        thumbnails = listOf(
            Thumbnail(
                url = "https://picsum.photos/120",
                width = 120,
                height = 120)
        ),
        albumType = MetadataAlbumType.Album,
        artists = artists.take(2),
        externalUri = null)

    return listOf(
        MetadataTrack(
            id = "track_1",
            title = "Signal Bloom",
            durationMs = 198_000,
            trackNumber = 1,
            discNumber = 1,
            artists = artists,
            album = album,
            thumbnails = null,
            explicit = false,
            popularity = 92,
            isrcCode = null,
            externalUri = null),
        MetadataTrack(
            id = "track_2",
            title = "Static Horizon",
            durationMs = 224_000,
            trackNumber = 2,
            discNumber = 1,
            artists = artists.take(2),
            album = album.copy(id = "album_2", title = "City Pulse"),
            explicit = false,
            popularity = 80,
            isrcCode = null,
            externalUri = null,
            thumbnails = null),
        MetadataTrack(
            id = "track_3",
            title = "Last Train Echo",
            durationMs = 246_000,
            trackNumber = 3,
            discNumber = 1,
            artists = artists.take(1),
            album = album.copy(id = "album_3", title = "Afterlight"),
            explicit = false,
            popularity = 77,
            isrcCode = null,
            externalUri = null,
            thumbnails = null))
}

@Composable
@Preview
private fun TrackListPreview() {
    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        TrackList(
            tracks = previewTracks(),
            hasMore = true,
            isLoadingNextPage = true)
    }
}


/**
 * Three bars that bounce out of phase, shown over the cover of the track that is playing. Each
 * bar runs on its own timing so the motion never looks like a single loop.
 */
@Composable
private fun EqualizerBars(modifier: Modifier = Modifier, color: Color = Color.White) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "equalizer")
    val periods = listOf(520, 700, 610)
    val heights = periods.mapIndexed { index, period ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(
                    durationMillis = period,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                initialStartOffset = androidx.compose.animation.core.StartOffset(index * 130)),
            label = "bar_$index")
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom) {
        heights.forEach { height ->
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight(height.value)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(color))
        }
    }
}
