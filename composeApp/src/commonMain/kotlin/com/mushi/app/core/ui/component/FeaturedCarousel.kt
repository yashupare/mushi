package com.mushi.app.core.ui.component


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseItem
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueCollectionEntry
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.remote.CollectionPlaybackKey
import com.mushi.app.core.remote.PlaybackDestinationAction
import com.mushi.app.core.remote.RemoteCollectionType
import com.mushi.app.core.remote.RemotePlaybackController
import com.mushi.app.core.ui.coverflow.Coverflow
import com.mushi.app.core.ui.coverflow.CoverflowParams
import com.mushi.app.core.ui.coverflow.rememberCoverflowState
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxPause
import com.mushi.app.resources.iconsax.IconsaxPlay
import com.mushi.app.resources.iconsax.IconsaxPlayCircle
import com.mushi.app.resources.iconsax.Play
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@Composable
fun FeaturedCarousel(
    items: List<MetadataBrowseItem>,
    modifier: Modifier = Modifier) {
    val filteredItems = remember(items) {
        items.filter { it is MetadataBrowseItem.Album || it is MetadataBrowseItem.Playlist || it is MetadataBrowseItem.Track }
    }

    if (filteredItems.isEmpty()) return

    FeaturedCarouselContent(
        items = filteredItems)
}

@Composable
private fun FeaturedCarouselContent(
    items: List<MetadataBrowseItem>,
    modifier: Modifier = Modifier) {
    val isSmallScreen = currentWindowWidthBreakpoint() isBelow WindowBreakpoints.Medium
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 0) { items.size }
    val scope = rememberCoroutineScope()

    // One large card with the next one peeking in at the edge, so the row reads as swipeable
    // without overlapping cards or text cut off by a neighbour.
    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        pageSpacing = 12.dp,
        pageSize = if (isSmallScreen) {
            androidx.compose.foundation.pager.PageSize.Fill
        } else {
            androidx.compose.foundation.pager.PageSize.Fixed(460.dp)
        },
        key = { index -> items[index].hashCode() }) { page ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.35f)) {
            FeaturedCarouselCard(
                item = items[page],
                isCentered = pagerState.currentPage == page,
                onScrollToCenter = {
                    scope.launch { pagerState.animateScrollToPage(page) }
                })
        }
    }
}

@Composable
private fun FeaturedCarouselCard(
    item: MetadataBrowseItem,
    isCentered: Boolean,
    modifier: Modifier = Modifier,
    onScrollToCenter: () -> Unit = { }) {
    val navigationCommands = koinInject<NavigationCommands>()
    val audioPlayerQueue = koinInject<AudioPlayerQueue>()

    val hazeState = rememberHazeState()

    val thumbnailUrl = when (item) {
        is MetadataBrowseItem.Album -> item.data.thumbnails.firstOrNull()?.url
        is MetadataBrowseItem.Playlist -> item.data.thumbnails.firstOrNull()?.url
        is MetadataBrowseItem.Track -> (item.data.album?.thumbnails ?: item.data.thumbnails)?.firstOrNull()?.url
        else -> null
    }

    val title = when (item) {
        is MetadataBrowseItem.Album -> item.data.title
        is MetadataBrowseItem.Playlist -> item.data.title
        is MetadataBrowseItem.Track -> item.data.title
        else -> ""
    }

    val subtitle = when (item) {
        is MetadataBrowseItem.Album -> item.data.artists.joinToString { it.name }
        is MetadataBrowseItem.Playlist -> item.data.owner?.displayName ?: item.data.owner?.username
        ?: stringResource(Res.string.media_playlist)

        is MetadataBrowseItem.Track -> item.data.artists.joinToString { it.name }
        else -> ""
    }

    val dimAlpha by animateFloatAsState(
        targetValue = if (isCentered) 0f else 0.5f,
        label = "dim_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable() {
                if (!isCentered) {
                    onScrollToCenter()
                    return@clickable
                }
                when (item) {
                    is MetadataBrowseItem.Album -> navigationCommands.navigateTo(Routes.Album(item.data.id))
                    is MetadataBrowseItem.Playlist -> navigationCommands.navigateTo(
                        Routes.Playlist(
                            item.data.id
                        )
                    )

                    is MetadataBrowseItem.Track -> {}
                    else -> {}
                }
            }
            .fillMaxSize()
            .hazeSource(hazeState)) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha)))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)))))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }

            val animatedAlpha by animateFloatAsState(
                targetValue = if (isCentered) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "ButtonFade"
            )

            FeaturedPlayButton(
                item = item,
                audioPlayerQueue = audioPlayerQueue,
                navigationCommands = navigationCommands,
                enabled = isCentered,
                modifier = Modifier
                    .clip(CircleShape)
                    .graphicsLayer {
                        alpha = animatedAlpha
                    }
                    .hazeEffect(state = hazeState) {
                        blurEffect {
                            blurRadius = 20.dp
                            colorEffects =
                                listOf(HazeColorEffect.tint(Color.White.copy(alpha = 0.1f)))
                        }
                    }
            )

        }
    }
}

@Composable
private fun FeaturedPlayButton(
    item: MetadataBrowseItem,
    audioPlayerQueue: AudioPlayerQueue,
    navigationCommands: NavigationCommands,
    remotePlaybackController: RemotePlaybackController = koinInject(),
    enabled: Boolean,
    modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val currentCollectionEntry by audioPlayerQueue.currentCollectionEntryFlow.collectAsStateWithLifecycle()
    val busyCollections by remotePlaybackController.busyCollections.collectAsStateWithLifecycle()

    when (item) {
        is MetadataBrowseItem.Album -> {
            val isPlaying = currentCollectionEntry is QueueCollectionEntry.Album &&
                    (currentCollectionEntry as QueueCollectionEntry.Album).id == item.data.id

            Box(
                modifier = modifier.size(44.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                MillenniumIconButton(
                    style = MillenniumIconButtonStyle.Quiet,
                    onClick = {
                        if (isPlaying) {
                            navigationCommands.navigateTo(Routes.Album(item.data.id))
                        } else {
                            remotePlaybackController.requestCollectionPlay(
                                RemoteCollectionType.Album,
                                item.data.id,
                                item.data.title
                            )
                        }
                    },
                    enabled = enabled,
                    loading = CollectionPlaybackKey(
                        RemoteCollectionType.Album, item.data.id, PlaybackDestinationAction.Play
                    ) in busyCollections,
                    foreground = Color.White
                ) {
                    MillenniumIcon(
                        imageVector = if (isPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        is MetadataBrowseItem.Playlist -> {
            val isPlaying = currentCollectionEntry is QueueCollectionEntry.Playlist &&
                    (currentCollectionEntry as QueueCollectionEntry.Playlist).id == item.data.id

            Box(
                modifier = modifier.size(44.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                MillenniumIconButton(
                    style = MillenniumIconButtonStyle.Quiet,
                    onClick = {
                        if (isPlaying) {
                            navigationCommands.navigateTo(Routes.Playlist(item.data.id))
                        } else {
                            remotePlaybackController.requestCollectionPlay(
                                RemoteCollectionType.Playlist,
                                item.data.id,
                                item.data.title
                            )
                        }
                    },
                    enabled = enabled,
                    loading = CollectionPlaybackKey(
                        RemoteCollectionType.Playlist, item.data.id, PlaybackDestinationAction.Play
                    ) in busyCollections,
                    foreground = Color.White
                ) {
                    MillenniumIcon(
                        imageVector = if (isPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        is MetadataBrowseItem.Track -> {
            val queueEntry = QueueEntry.StreamingTrack(track = item.data, url = "")
            val currentTrack by audioPlayerQueue.currentQueueEntryFlow.collectAsStateWithLifecycle()
            val isPlaying =
                currentTrack is QueueEntry.StreamingTrack && (currentTrack as QueueEntry.StreamingTrack).track.id ==
                        item.data.id

            Box(
                modifier = modifier.size(44.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                MillenniumIconButton(
                    style = MillenniumIconButtonStyle.Quiet,
                    onClick = {
                        scope.launch {
                            audioPlayerQueue.load(
                                entries = listOf(queueEntry),
                                autoPlay = true,
                                startPosition = 0,
                                collectionEntry = null)
                        }
                    },
                    enabled = enabled
                ) {
                    MillenniumIcon(
                        imageVector = if (isPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        else -> {}
    }
}
