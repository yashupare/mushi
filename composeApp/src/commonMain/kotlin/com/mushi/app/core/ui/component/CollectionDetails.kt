package com.mushi.app.core.ui.component


import com.mushi.app.core.ui.component.MushiButton
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.height
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumCard
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumSeparator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.width
import com.mushi.app.core.ui.pressScale
import com.mushi.app.resources.iconsax.IconsaxPause
import com.mushi.app.resources.iconsax.IconsaxPlay
import com.mushi.app.core.ui.misc.TextWithShimmer
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import com.mushi.app.resources.iconsax.IconsaxEdit
import com.mushi.app.resources.iconsax.IconsaxHeart
import com.mushi.app.resources.iconsax.IconsaxHeart2
import com.mushi.app.resources.iconsax.IconsaxPauseCircle
import com.mushi.app.resources.iconsax.IconsaxPlayCircle2
import com.mushi.app.resources.iconsax.IconsaxShuffle
import com.mushi.app.resources.iconsax.User
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CollectionDetails(
    modifier: Modifier = Modifier,
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
    /** Guest in a jam session: play/shuffle are replaced by Add to Jam. */
    isJamGuest: Boolean = false,
    onAddToJam: (() -> Unit)? = null,
    isFollowing: Boolean = false,
    isFollowLoading: Boolean = false,
    isPlayLoading: Boolean = false,
    isShuffleLoading: Boolean = false,
    isAddToQueueLoading: Boolean = false,
    onFollowClick: () -> Unit = { },
    showFollowButton: Boolean = true,
    onEdit: (() -> Unit)? = null,
    sharedElementKey: String? = null) {
    Box(modifier = modifier.fillMaxWidth()) {
        val isCompact = currentWindowWidthBreakpoint() isBelow WindowBreakpoints.Medium

        val sharedTransitionScope = LocalSharedTransitionScope.current
        val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

        val playPauseButton = @Composable {
            if (isJamGuest) {
                if (onAddToJam != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MushiButton(
                            modifier = if (isCompact) Modifier.weight(1f) else Modifier,
                            onClick = onAddToJam!!) {
                            MillenniumIcon(
                                imageVector = Iconsax.IconsaxAddSquare,
                                contentDescription = stringResource(Res.string.track_options_add_to_jam))
                            TextWithShimmer(text = stringResource(Res.string.track_options_add_to_jam))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val modifier = if (isCompact) {
                        Modifier.weight(1f)
                    } else {
                        Modifier
                    }

                    MushiButton(
                        modifier = modifier,
                        onClick = onPlay,
                        loading = isPlayLoading) {
                        MillenniumIcon(
                            imageVector = if (isPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                            contentDescription = if (isPlaying) stringResource(Res.string.player_pause_content_description) else stringResource(Res.string.player_play_content_description))
                        TextWithShimmer(text = if (isPlaying) stringResource(Res.string.player_pause_content_description) else stringResource(Res.string.player_play_content_description))
                    }
                    MillenniumButton(
                        modifier = modifier,
                        onClick = onShufflePlay,
                        loading = isShuffleLoading,
                        style = MillenniumButtonStyle.Secondary
                    ) {
                        MillenniumIcon(imageVector = com.mushi.app.core.ui.theming.MushiIcons.Shuffle, contentDescription = stringResource(Res.string.collection_details_shuffle_play))
                        TextWithShimmer(text = stringResource(Res.string.collection_details_shuffle))
                    }
                }
            }
        }

        val actions = @Composable {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MillenniumIconButton(
                    onClick = onAddToQueue,
                    loading = isAddToQueueLoading,
                    style = MillenniumIconButtonStyle.Quiet) {
                    MillenniumIcon(
                        imageVector = Iconsax.IconsaxAddSquare,
                        contentDescription = stringResource(Res.string.player_add_to_queue_content_description)
                    )
                }
                MillenniumSeparator(modifier = Modifier.height(24.dp), vertical = true)
                if (showFollowButton) {
                    if (isFollowing) {
                        MillenniumIconButton(
                            onClick = onFollowClick,
                            loading = isFollowLoading,
                            style = MillenniumIconButtonStyle.Quiet) {
                            MillenniumIcon(
                                imageVector = Iconsax.IconsaxHeart2,
                                contentDescription = stringResource(Res.string.collection_details_unfollow))
                        }
                    } else {
                        MillenniumIconButton(
                            onClick = onFollowClick,
                            loading = isFollowLoading,
                            style = MillenniumIconButtonStyle.Quiet) {
                            MillenniumIcon(
                                imageVector = Iconsax.IconsaxHeart,
                                contentDescription = stringResource(Res.string.collection_details_follow),
                                tint = AppTheme.colors.onSurfaceVariant)
                        }
                    }
                }
                if (onEdit != null) {
                    MillenniumSeparator(modifier = Modifier.height(24.dp), vertical = true)
                    MillenniumIconButton(
                        onClick = onEdit,
                        style = MillenniumIconButtonStyle.Quiet) {
                        MillenniumIcon(
                            imageVector = com.mushi.app.core.ui.theming.MushiIcons.Edit,
                            contentDescription = stringResource(Res.string.collection_details_edit_playlist))
                    }
                }
            }
        }
        val ownerInfo = @Composable {
            Row(
                modifier = Modifier.clickable(onClick = onOwnerClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (ownerImageURL != null) {
                    AsyncImage(
                        model = ownerImageURL,
                        contentDescription = ownerName,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .shimmerApply(),
                        contentScale = ContentScale.Crop)
                } else {
                    MillenniumIcon(
                        imageVector = com.mushi.app.core.ui.theming.MushiIcons.User,
                        contentDescription = ownerName,
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                AppTheme.colors.primaryContainer,
                                CircleShape
                            )
                            .padding(6.dp),
                        tint = AppTheme.colors.onPrimaryContainer)
                }
                TextWithShimmer(
                    text = stringResource(Res.string.collection_details_by_owner, ownerName),
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.onSurfaceVariant)
            }
        }

        val artwork = @Composable {
            Box(
                modifier = Modifier
                    .then(
                        if (isCompact) {
                            // A large cover with a soft shadow, like a record sleeve.
                            Modifier
                                .fillMaxWidth(0.72f)
                                .aspectRatio(1f)
                                .shadow(elevation = 20.dp, shape = RoundedCornerShape(12.dp))
                        } else {
                            Modifier.size(200.dp)
                        })
                    .clip(RoundedCornerShape(12.dp))
                    .sharedElementOrNone(
                        key = sharedElementKey,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope)
                    .shimmerApply(),
                contentAlignment = Alignment.Center) {
                if (imageURL.isNotBlank()) {
                    AsyncImage(
                        model = imageURL,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop)
                } else if (imageResource != null) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(imageResource),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop)
                } else {
                    Text(
                        text = title.take(1).ifBlank { "?" }.uppercase(),
                        style = AppTheme.typography.displaySmall,
                        color = AppTheme.colors.onSurfaceVariant)
                }
            }
        }

        val metadata = @Composable {
            Column {
                TextWithShimmer(
                    text = title,
                    style = if (isCompact) AppTheme.typography.bodyLarge else AppTheme.typography.headlineLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                if (description.isNotBlank()) {
                    TextWithShimmer(
                        text = description,
                        style = if (isCompact) AppTheme.typography.labelSmall else AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }

        if (isCompact) {
            // Phone layout: the cover, a left-aligned title and owner, then the secondary actions
            // on the left with Shuffle and a big round Play on the right, like Spotify.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    artwork()
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextWithShimmer(
                        text = title,
                        style = AppTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                    TextWithShimmer(
                        text = stringResource(Res.string.collection_details_by_owner, ownerName),
                        style = AppTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(onClick = onOwnerClick))
                    if (description.isNotBlank()) {
                        TextWithShimmer(
                            text = description,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    actions()
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    if (isJamGuest) {
                        if (onAddToJam != null) {
                            CollectionPillButton(
                                label = stringResource(Res.string.track_options_add_to_jam),
                                icon = Iconsax.IconsaxAddSquare,
                                onClick = onAddToJam,
                                modifier = Modifier.width(170.dp))
                        }
                    } else {
                        MillenniumIconButton(
                            onClick = onShufflePlay,
                            loading = isShuffleLoading,
                            style = MillenniumIconButtonStyle.Secondary) {
                            MillenniumIcon(
                                imageVector = com.mushi.app.core.ui.theming.MushiIcons.Shuffle,
                                contentDescription = stringResource(Res.string.collection_details_shuffle_play))
                        }
                        MillenniumIconButton(
                            onClick = onPlay,
                            loading = isPlayLoading,
                            modifier = Modifier.size(56.dp).pressScale(0.92f),
                            style = MillenniumIconButtonStyle.Primary) {
                            MillenniumIcon(
                                imageVector = if (isPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                                contentDescription = if (isPlaying) stringResource(Res.string.player_pause_content_description) else stringResource(Res.string.player_play_content_description),
                                modifier = Modifier.size(28.dp),
                                tint = com.composeunstyled.LocalTextStyle.current.color)
                        }
                    }
                }
            }
            return@Box
        }

        MillenniumCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(0.dp)) {
            if (isCompact) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        artwork()
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            metadata()
                            ownerInfo()
                            actions()
                        }
                    }
                    playPauseButton()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top) {
                    artwork()

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        metadata()
                        ownerInfo()
                        actions()
                        playPauseButton()
                    }
                }
            }
        }
    }
}

/** A flat, full-width action button: a faint tint with the label and icon in the accent colour. */
@Composable
private fun CollectionPillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false) {
    val tint = AppTheme.colors.primary.copy(alpha = if (loading) 0.5f else 1f)
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.08f))
            .clickable(enabled = !loading, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
        MillenniumIcon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        TextWithShimmer(
            text = label,
            style = AppTheme.typography.labelLarge,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
fun CollectionDetailsPreview() {
    CollectionDetails(
        title = "My Playlist",
        description = "A collection of my favorite songs.",
        imageURL = "https://example.com/playlist.jpg",
        ownerName = "John Doe",
        ownerImageURL = "https://example.com/john.jpg",
        onOwnerClick = {},
        onPlay = {},
        onShufflePlay = {},
        onAddToQueue = {})
}
