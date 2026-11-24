package com.mushi.app.core.ui.component


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import com.composeunstyled.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueCollectionEntry
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import com.mushi.app.resources.iconsax.IconsaxPauseCircle
import com.mushi.app.resources.iconsax.Play
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import com.mushi.app.core.remote.CollectionPlaybackKey
import com.mushi.app.core.remote.PlaybackDestinationAction
import com.mushi.app.core.remote.RemoteCollectionType
import com.mushi.app.core.remote.RemotePlaybackController
import com.mushi.app.modules.saved_tracks.SAVED_TRACKS_COLLECTION_ID
import com.mushi.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.core.ui.component.cards.PlayableListTile
import com.mushi.app.core.ui.component.cards.MediaCardSurface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LikedTracksCard(
    modifier: Modifier = Modifier,
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    remotePlaybackController: RemotePlaybackController = koinInject(),
    navigationCommands: NavigationCommands = koinInject(),
    listMode: Boolean = false
) {
    val currentCollectionEntry by audioPlayerQueue.currentCollectionEntryFlow.collectAsStateWithLifecycle()
    val busyCollections by remotePlaybackController.busyCollections.collectAsStateWithLifecycle()
    val isPlaying = currentCollectionEntry is QueueCollectionEntry.SavedTracks
    val title = stringResource(Res.string.media_liked_tracks)
    val playLoading = CollectionPlaybackKey(
        RemoteCollectionType.SavedTracks, SAVED_TRACKS_COLLECTION_ID, PlaybackDestinationAction.Play
    ) in busyCollections
    val addToQueueLoading = CollectionPlaybackKey(
        RemoteCollectionType.SavedTracks, SAVED_TRACKS_COLLECTION_ID, PlaybackDestinationAction.AddToQueue
    ) in busyCollections
    val onPlay = {
        remotePlaybackController.requestCollectionPlay(RemoteCollectionType.SavedTracks, SAVED_TRACKS_COLLECTION_ID, title)
    }
    val onAddToQueue = {
        remotePlaybackController.requestCollectionAddToQueue(RemoteCollectionType.SavedTracks, SAVED_TRACKS_COLLECTION_ID, title)
    }
    val subtitle = stringResource(Res.string.media_saved_songs)
    if (listMode) {
        PlayableListTile(
            title = title,
            subtitle = subtitle,
            isPlaying = isPlaying,
            onClick = { navigationCommands.navigateTo(Routes.SavedTracks) },
            onPlay = onPlay,
            onAddToQueue = onAddToQueue,
            modifier = modifier,
            sharedElementKey = "saved_tracks_art",
            playLoading = playLoading,
            addToQueueLoading = addToQueueLoading,
            artwork = {
                Image(
                    painterResource(Res.drawable.liked_tracks),
                    title,
                    Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        )
        return
    }
    val touchInput = LocalInputModeManager.current.inputMode == InputMode.Touch

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    MediaCardSurface(
        modifier = modifier.width(160.dp),
        onClick = { navigationCommands.navigateTo(Routes.SavedTracks) }
    ) { isHovered ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.Image(
                    painter = painterResource(Res.drawable.liked_tracks),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .sharedElementOrNone(
                            key = "saved_tracks_art",
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (isHovered || touchInput || isPlaying || playLoading || addToQueueLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            MillenniumIconButton(
                                onClick = onAddToQueue,
                                loading = addToQueueLoading,
                                modifier = Modifier.size(30.dp),
                                style = MillenniumIconButtonStyle.Secondary
                            ) {
                                MillenniumIcon(
                                    imageVector = Iconsax.IconsaxAddSquare,
                                    contentDescription = stringResource(Res.string.player_add_to_queue_content_description),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            MillenniumIconButton(
                                onClick = onPlay,
                                loading = playLoading,
                                modifier = Modifier.size(30.dp),
                                style = MillenniumIconButtonStyle.Primary
                            ) {
                                MillenniumIcon(
                                    imageVector = if (isPlaying) {
                                        com.mushi.app.core.ui.theming.MushiIcons.Pause
                                    } else com.mushi.app.core.ui.theming.MushiIcons.Play,
                                    contentDescription = stringResource(Res.string.player_play_content_description),
                                    modifier = Modifier.size(16.dp),
                                    tint = LocalTextStyle.current.color
                                )
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
