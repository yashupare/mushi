package com.mushi.app.core.ui.component


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composeunstyled.Text
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.ui.misc.TextWithShimmer
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxPauseCircle
import com.mushi.app.resources.iconsax.IconsaxPlayCircle2
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@Composable
fun CollectionView(
    title: String,
    description: String,
    imageURL: String,
    imageResource: DrawableResource? = null,
    ownerName: String,
    ownerImageURL: String?,
    onOwnerClick: () -> Unit,
    onPlay: () -> Unit,
    onShufflePlay: () -> Unit,
    onAddToQueue: () -> Unit,
    isPlaying: Boolean = false,
    isFollowing: Boolean = false,
    isFollowLoading: Boolean = false,
    isPlayLoading: Boolean = false,
    isShuffleLoading: Boolean = false,
    isAddToQueueLoading: Boolean = false,
    onFollowClick: () -> Unit = {},
    showFollowButton: Boolean = true,
    onEdit: (() -> Unit)? = null,
    sharedElementKey: String? = null,
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    tracks: List<MetadataTrack> = emptyList(),
    hasMore: Boolean = false,
    isLoadingNextPage: Boolean = false,
    currentTrackId: String? = null,
    isCurrentTrackPlaying: Boolean = false,
    onTrackClick: (MetadataTrack) -> Unit = {},
    onLoadNextPage: () -> Unit = {},
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
    onAddToJam: (List<MetadataTrack>) -> Unit = {},
    trackOptionsState: (MetadataTrack) -> TrackOptionsState = { TrackOptionsState() },
    footerContent: (@Composable () -> Unit)? = null,
    trailingContent: @Composable () -> Unit = {}) {
    val listState = rememberLazyListState()
    val isCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 400
        }
    }

    // A wash of the cover's colour runs from the top of the page and fades as it scrolls away.
    val pageColor = org.spotube.millennium.theme.MillenniumTheme.colors.background
    val coverTint = com.mushi.app.core.ui.theming.rememberArtworkTint(imageURL.ifBlank { null })
    val washColor by androidx.compose.animation.animateColorAsState(
        targetValue = coverTint
            ?.let { androidx.compose.ui.graphics.lerp(pageColor, it, if (pageColor.luminance() < 0.5f) 0.6f else 0.38f) }
            ?: pageColor,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 700),
        label = "collection_wash")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val scrolled = if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else Int.MAX_VALUE
                val visible = (1f - scrolled / 1000f).coerceIn(0f, 1f)
                if (visible > 0f) {
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(washColor, androidx.compose.ui.graphics.Color.Transparent),
                            endY = size.height * 0.6f),
                        alpha = visible)
                }
            }
    ) {
        ApplicationMainBar(
            backButton = true,
            title = if (isCollapsed) {
                {
                    CollapsedCollectionTitle(
                        title = title,
                        imageURL = imageURL,
                        imageResource = imageResource)
                }
            } else {
                {}
            },
            actions = if (isCollapsed && !isJamGuest) {
                {
                    MillenniumIconButton(
                        onClick = onPlay,
                        loading = isPlayLoading,
                        style = MillenniumIconButtonStyle.Quiet) {
                        MillenniumIcon(
                            imageVector = if (isPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                            contentDescription = if (isPlaying) stringResource(Res.string.player_pause_content_description) else stringResource(Res.string.player_play_content_description))
                    }
                }
            } else {
                {}
            })

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
        if (error != null && tracks.isEmpty() && !isLoading) {
            ErrorDisplay(
                errorMessage = error,
                onRetry = onRetry ?: {},
                modifier = Modifier.fillMaxSize())
        } else {
            TrackList(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                headerContent = {
                    if (isLoading && tracks.isEmpty()) {
                        com.mushi.app.core.ui.misc.SkeletonTree(true) {
                            CollectionDetails(
                                title = title.ifBlank { stringResource(Res.string.common_loading) },
                                description = description,
                                imageURL = imageURL,
                                imageResource = imageResource,
                                ownerName = ownerName.ifBlank { stringResource(Res.string.common_unknown) },
                                ownerImageURL = ownerImageURL,
                                onOwnerClick = onOwnerClick,
                                onPlay = onPlay,
                                onShufflePlay = onShufflePlay,
                                onAddToQueue = onAddToQueue,
                                isPlaying = isPlaying,
                                isFollowing = isFollowing,
                                isFollowLoading = isFollowLoading,
                                isPlayLoading = isPlayLoading,
                                isShuffleLoading = isShuffleLoading,
                                isAddToQueueLoading = isAddToQueueLoading,
                                onFollowClick = onFollowClick,
                                showFollowButton = showFollowButton,
                                onEdit = onEdit,
                                sharedElementKey = sharedElementKey,
                                isJamGuest = isJamGuest,
                                onAddToJam = { onAddToJam(tracks) })
                        }
                    } else {
                        CollectionDetails(
                            title = title,
                            description = description,
                            imageURL = imageURL,
                            imageResource = imageResource,
                            ownerName = ownerName,
                            ownerImageURL = ownerImageURL,
                            onOwnerClick = onOwnerClick,
                            onPlay = onPlay,
                            onShufflePlay = onShufflePlay,
                            onAddToQueue = onAddToQueue,
                            isPlaying = isPlaying,
                            isFollowing = isFollowing,
                            isFollowLoading = isFollowLoading,
                            isPlayLoading = isPlayLoading,
                            isShuffleLoading = isShuffleLoading,
                            isAddToQueueLoading = isAddToQueueLoading,
                            onFollowClick = onFollowClick,
                            showFollowButton = showFollowButton,
                            onEdit = onEdit,
                            sharedElementKey = sharedElementKey,
                            isJamGuest = isJamGuest,
                            onAddToJam = { onAddToJam(tracks) })
                    }
                },
                footerContent = footerContent,
                tracks = tracks,
                error = error,
                hasMore = hasMore,
                isLoading = isLoading,
                isLoadingNextPage = isLoadingNextPage,
                currentTrackId = currentTrackId,
                isCurrentTrackPlaying = isCurrentTrackPlaying,
                onTrackClick = onTrackClick,
                onLoadNextPage = onLoadNextPage,
                onTrackOptionsAction = onTrackOptionsAction,
                onArtistClick = onArtistClick,
                onAlbumClick = onAlbumClick,
                onArtistsOverflowClick = onArtistsOverflowClick,
                onBulkDownload = onBulkDownload,
                onBulkAddToQueue = onBulkAddToQueue,
                onBulkPlayNext = onBulkPlayNext,
                onBulkAddToPlaylist = onBulkAddToPlaylist,
                onBulkAddToJam = onBulkAddToJam,
                isInJam = isInJam,
                isJamGuest = isJamGuest,
                trackOptionsState = trackOptionsState)
        }

        trailingContent()
        }
    }
}

@Composable
private fun CollapsedCollectionTitle(
    title: String,
    imageURL: String,
    imageResource: DrawableResource? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerApply(),
            contentAlignment = Alignment.Center) {
            if (imageURL.isNotBlank()) {
                AsyncImage(
                    model = imageURL,
                    contentDescription = title,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Crop)
            } else if (imageResource != null) {
                androidx.compose.foundation.Image(
                    painter = painterResource(imageResource),
                    contentDescription = title,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Crop)
            } else {
                Text(
                    text = title.take(1).ifBlank { "?" }.uppercase(),
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.onSurfaceVariant)
            }
        }
        TextWithShimmer(
            text = title,
            style = AppTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp))
    }
}
