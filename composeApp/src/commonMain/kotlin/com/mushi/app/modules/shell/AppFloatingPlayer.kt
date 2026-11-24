package com.mushi.app.modules.shell


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumProgress
import org.spotube.millennium.theme.millenniumSurface
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.MillenniumTheme
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import com.mushi.app.core.ui.swipeToChangeTrack
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.modules.saved_tracks.SavedTracksViewModel
import com.mushi.app.modules.saved_tracks.SAVED_TRACKS_COLLECTION_ID
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxPause
import com.mushi.app.resources.iconsax.IconsaxPlay
import com.mushi.app.resources.iconsax.IconsaxMusic
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.player_play_content_description
import com.mushi.app.generated.resources.player_pause_content_description
import com.mushi.app.generated.resources.player_nothing_playing
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.mushi.app.core.audioplayer.AudioPlayerInterface

// The card is 64 dp tall; the extra 8 dp is the gap that separates it from the tab bar below.
internal val FloatingPlayerContentHeight = 72.dp
internal val FloatingPlayerBottomBleed = 8.dp

// The floating player on small screens that appears above the floating AppBottombar
//
// on left the album art, song title, artists grouped together
// on right the playback controls
// top border is actually the progress bar
// users can skip to next or previous track by swiping left or right on the player
//
// the player can be expanded to the full screen by swiping up on the player, and can be collapsed
// back to the floating player by swiping down on the player
@Composable
fun AppFloatingPlayer(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    audioPlayer: AudioPlayerInterface = koinInject(),
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    savedTracksViewModel: SavedTracksViewModel = koinViewModel<SavedTracksViewModel>(
        key = SAVED_TRACKS_COLLECTION_ID,
        parameters = { parametersOf() }
    )
) {
    val playerUiState = rememberPlayerUiState(audioPlayer, audioPlayerQueue)
    val scope = rememberCoroutineScope()
    val albumArtModifier =
        rememberSharedAlbumArtModifier(sharedTransitionScope, animatedVisibilityScope)
    val haptics = com.mushi.app.core.ui.rememberHaptics()
    val jamRoomService: JamRoomService = koinInject()
    val isJamGuest by jamRoomService.role
        .map { it == JamRole.Guest }
        .collectAsStateWithLifecycle(initialValue = false)

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

    val playerPosition = rememberPlayerPosition(audioPlayer)
    FloatingPlayerContent(
        playerPosition = playerPosition,
        playerUiState = playerUiState,
        modifier = modifier,
        albumArtModifier = albumArtModifier,
        onPlayPause = ::onPlayPause,
        onSkipNext = { scope.launch { audioPlayer.skipToNext() } },
        onSkipPrevious = { scope.launch { audioPlayer.skipToPrevious() } },
        swipeEnabled = !isJamGuest,
        heartButton = {
            PlayerHeartButton(
                audioPlayerQueue = audioPlayerQueue,
                savedTracksViewModel = savedTracksViewModel
            )
        }
    )
}

@Composable
internal fun FloatingPlayerContent(
    playerUiState: PlayerUiState,
    modifier: Modifier = Modifier,
    albumArtModifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    swipeEnabled: Boolean = false,
    /** The playback position for the progress line; null (previews, tests) shows no progress. */
    playerPosition: androidx.compose.runtime.State<kotlin.time.Duration>? = null,
    heartButton: @Composable () -> Unit
) {
    val coverModel = playerUiState.coverModel
    // The bar takes on the cover's colour, like Spotify's and YouTube Music's mini players.
    val coverTint = com.mushi.app.core.ui.theming.rememberArtworkTint(coverModel)
    val baseSurface = AppTheme.colors.surfaceContainerHigh
    val light = com.mushi.app.core.ui.theming.isLightAppearance()
    val surfaceColor by androidx.compose.animation.animateColorAsState(
        targetValue = coverTint
            ?.let {
                // On a light page a dark cover colour would turn the card muddy grey, so use a
                // pastel of it instead.
                if (light) {
                    androidx.compose.ui.graphics.lerp(baseSurface, it, 0.16f)
                } else {
                    androidx.compose.ui.graphics.lerp(baseSurface, it, 0.34f)
                }
            }
            ?: baseSurface,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
        label = "mini_player_tint")
    val canSwipe = swipeEnabled && playerUiState.queue.isNotEmpty()


    // A floating card: rounded on every corner, inset from the screen edges, with a small gap
    // above the tab bar. The gap itself is the tab bar's colour so no page content shows in it.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(FloatingPlayerContentHeight)
    ) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(FloatingPlayerBottomBleed)
                .background(AppTheme.colors.surfaceContainer)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp, bottom = FloatingPlayerBottomBleed)
                .clip(RoundedCornerShape(16.dp))
                .background(surfaceColor)
                .swipeToChangeTrack(canSwipe, onSkipNext, onSkipPrevious)
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (playerPosition != null) {
                MiniPlayerProgress(
                    position = playerPosition,
                    seekDuration = playerUiState.seekDuration
                )
            } else {
                androidx.compose.foundation.layout.Spacer(Modifier.height(2.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(MiniCoverSize)
                            .then(albumArtModifier)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppTheme.colors.onSurface.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverModel != null) AsyncImage(
                            model = coverModel,
                            contentDescription = playerUiState.title,
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        else MillenniumIcon(
                            com.mushi.app.core.ui.theming.MushiIcons.MusicNote, contentDescription = null,
                            tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            text = if (playerUiState.queue.isEmpty()) stringResource(Res.string.player_nothing_playing) else playerUiState.title,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (playerUiState.queue.isNotEmpty()) Text(
                            text = playerUiState.artists,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Nothing queued: no like or play buttons, just the "Nothing playing" label.
                if (playerUiState.queue.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
                    heartButton()
                    Spacer(modifier = Modifier.size(8.dp))
                    MillenniumIconButton(
                        onClick = onPlayPause,
                        enabled = playerUiState.queue.isNotEmpty(),
                        style = MillenniumIconButtonStyle.Quiet
                    ) {
                        com.mushi.app.core.ui.component.PlayPauseGlyph(
                            isPlaying = playerUiState.isPlaying,
                            modifier = Modifier.size(28.dp),
                            contentDescription = stringResource(if (playerUiState.isPlaying) Res.string.player_pause_content_description else Res.string.player_play_content_description),
                            tint = AppTheme.colors.onSurface
                        )
                    }
                }
            }
        }
        }
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
