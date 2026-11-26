package com.mushi.app.core.ui.component.cards


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import com.composeunstyled.LocalTextStyle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.component.LocalAnimatedVisibilityScope
import com.mushi.app.core.ui.component.LocalSharedTransitionScope
import com.mushi.app.core.ui.component.sharedElementOrNone
import com.mushi.app.core.ui.misc.TextWithShimmer
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import com.mushi.app.resources.iconsax.IconsaxPause
import com.mushi.app.resources.iconsax.IconsaxPlay
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.player_add_to_queue_content_description
import com.mushi.app.generated.resources.player_pause_content_description
import com.mushi.app.generated.resources.player_play_content_description
import com.mushi.app.generated.resources.media_preview_title
import com.mushi.app.generated.resources.media_preview_subtitle

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayableCard(
    title: String,
    subtitle: String? = null,
    imageURL: String? = null,
    isPlaying: Boolean = false,
    onClick: (() -> Unit)? = null,
    onPlay: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    sharedElementKey: String? = null,
    listMode: Boolean = false,
    playLoading: Boolean = false,
    addToQueueLoading: Boolean = false
) {
    if (listMode) {
        PlayableListTile(
            title,
            subtitle,
            imageURL,
            isPlaying,
            onClick,
            onPlay,
            onAddToQueue,
            modifier,
            sharedElementKey,
            playLoading = playLoading,
            addToQueueLoading = addToQueueLoading
        )
        return
    }
    val touchInput = LocalInputModeManager.current.inputMode == InputMode.Touch
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    MediaCardSurface(
        modifier = modifier.width(160.dp),
        onClick = onClick ?: onPlay,
        interactive = onClick != null || onPlay != null || onAddToQueue != null
    ) { highlighted ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().shimmerApply()) {
                MediaArtwork(
                    model = imageURL,
                    title = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .sharedElementOrNone(
                            key = sharedElementKey,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    shape = RoundedCornerShape(10.dp)
                )

                if (isPlaying) {
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) { com.mushi.app.core.ui.component.NowPlayingBadge() }
                }

                if (onPlay != null || onAddToQueue != null)
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        this@Column.AnimatedVisibility(
                            visible = (highlighted && !touchInput) || (isPlaying && onPlay != null) ||
                                playLoading || addToQueueLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                // While this collection is playing the play/pause toggle stays
                                // visible and the add-to-queue action is hidden entirely.
                                // On touch a tap opens the item, where Play lives, so covers stay
                                // clear unless this one is playing.
                                onAddToQueue?.takeIf { !isPlaying && !touchInput }?.let {
                                    MillenniumIconButton(
                                        onClick = onAddToQueue,
                                        loading = addToQueueLoading,
                                        modifier = Modifier.size(34.dp),
                                        style = MillenniumIconButtonStyle.Secondary
                                    ) {
                                        MillenniumIcon(
                                            imageVector = Iconsax.IconsaxAddSquare,
                                            contentDescription = stringResource(
                                                Res.string.player_add_to_queue_content_description
                                            ),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                onPlay?.let {
                                    // A flat translucent dark circle with a white icon, the same as the
                                    // play button on the home carousel.
                                    MillenniumIconButton(
                                        onClick = onPlay,
                                        loading = playLoading,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f)),
                                        style = MillenniumIconButtonStyle.Quiet,
                                        foreground = androidx.compose.ui.graphics.Color.White
                                    ) {
                                        MillenniumIcon(
                                            imageVector = if (isPlaying) {
                                                com.mushi.app.core.ui.theming.MushiIcons.Pause
                                            } else com.mushi.app.core.ui.theming.MushiIcons.Play,
                                            contentDescription = stringResource(
                                                if (isPlaying) {
                                                    Res.string.player_pause_content_description
                                                } else {
                                                    Res.string.player_play_content_description
                                                }
                                            ),
                                            tint = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
            // Text lines up with the artwork's left edge, with no inner padding.
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 2.dp, top = 10.dp, end = 2.dp, bottom = 4.dp)
            ) {
                TextWithShimmer(
                    text = title,
                    style = AppTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                subtitle?.let {
                    TextWithShimmer(
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
}

@PreviewLightDark
@Composable
private fun PlayableCardPreview() {
    Box {
        PlayableCard(
            title = stringResource(Res.string.media_preview_title),
            subtitle = stringResource(Res.string.media_preview_subtitle),
            imageURL = "https://placehold.co/600x400",
            isPlaying = true,
            onClick = {},
            onPlay = {},
            onAddToQueue = {}
        )
    }
}
