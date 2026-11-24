package com.mushi.app.core.ui.component.cards

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalTextStyle
import coil3.compose.AsyncImage
import com.mushi.app.core.ui.component.LocalAnimatedVisibilityScope
import com.mushi.app.core.ui.component.LocalSharedTransitionScope
import com.mushi.app.core.ui.component.sharedElementOrNone
import com.mushi.app.core.ui.misc.TextWithShimmer
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import com.mushi.app.resources.iconsax.IconsaxMusic
import com.mushi.app.resources.iconsax.IconsaxPause
import com.mushi.app.resources.iconsax.IconsaxPlay
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumListTile
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumBevelBrush
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import com.mushi.app.generated.resources.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayableListTile(
    title: String,
    subtitle: String? = null,
    imageURL: String? = null,
    isPlaying: Boolean = false,
    onClick: (() -> Unit)? = null,
    onPlay: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    sharedElementKey: String? = null,
    playLoading: Boolean = false,
    addToQueueLoading: Boolean = false,
    imageModel: Any? = imageURL,
    imageShape: Shape = RoundedCornerShape(8.dp),
    placeholderIcon: ImageVector = com.mushi.app.core.ui.theming.MushiIcons.MusicNote,
    artwork: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isBlacklisted: Boolean = false,
    compact: Boolean = false
) {
    val colors = MillenniumTheme.colors
    // On touch the row itself is the target (tap to open), so the add-to-queue button goes away
    // and play becomes a quiet icon, like Spotify's and YouTube Music's list rows.
    val touchInput = androidx.compose.ui.platform.LocalInputModeManager.current.inputMode ==
        androidx.compose.ui.input.InputMode.Touch
    MillenniumListTile(
        modifier = modifier,
        onClick = onClick ?: onPlay,
        selected = isPlaying,
        // Flat rows: no panel fill or border, the theme's transparent row (and soft selected
        // tint for the one that is playing). Only a blacklisted item gets a coloured outline.
        background = null,
        shape = RoundedCornerShape(12.dp),
        borderColor = if (isBlacklisted) colors.danger.copy(alpha = 0.5f) else null,
        titleForeground = if (isBlacklisted) colors.danger else null,
        borderWidth = if (isBlacklisted) 1.dp else 0.dp,
        contentPadding = PaddingValues(
            horizontal = if (compact) 8.dp else 12.dp,
            vertical = if (compact) 6.dp else 8.dp),
        minimumHeight = if (compact) 52.dp else 72.dp,
        leading = {
            MediaArtwork(
                imageModel, title,
                Modifier.size(if (compact) 36.dp else 60.dp).sharedElementOrNone(
                    sharedElementKey,
                    LocalSharedTransitionScope.current,
                    LocalAnimatedVisibilityScope.current
                ),
                imageShape, placeholderIcon, artwork
            )
        },
        title = {
            TextWithShimmer(
                title,
                style = AppTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = subtitle?.let { text ->
            {
                TextWithShimmer(
                    text,
                    style = AppTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        trailing = if (onPlay != null || onAddToQueue != null || trailingContent != null) {
            {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trailingContent?.invoke()
                    if (onAddToQueue != null && !isPlaying && !touchInput) {
                        MillenniumIconButton(
                            onClick = onAddToQueue,
                            loading = addToQueueLoading,
                            modifier = Modifier.size(if (compact) 32.dp else 40.dp),
                            style = MillenniumIconButtonStyle.Secondary
                        ) {
                            MillenniumIcon(
                                Iconsax.IconsaxAddSquare,
                                stringResource(Res.string.player_add_to_queue_content_description)
                            )
                        }
                    }
                    if (onPlay != null) {
                        MillenniumIconButton(
                            onClick = onPlay,
                            loading = playLoading,
                            modifier = Modifier.size(if (compact) 32.dp else 40.dp),
                            style = if (touchInput) MillenniumIconButtonStyle.Quiet else MillenniumIconButtonStyle.Secondary
                        ) {
                            MillenniumIcon(
                                if (isPlaying) com.mushi.app.core.ui.theming.MushiIcons.Pause else com.mushi.app.core.ui.theming.MushiIcons.Play,
                                stringResource(if (isPlaying) Res.string.player_pause_content_description else Res.string.player_play_content_description),
                                tint = AppTheme.colors.primary
                            )
                        }
                    }
                }
            }
        } else null
    )
}

@Composable
internal fun MediaArtwork(
    model: Any?,
    title: String,
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
    placeholderIcon: ImageVector = com.mushi.app.core.ui.theming.MushiIcons.MusicNote,
    artwork: (@Composable () -> Unit)? = null
) {
    Box(
        // A plain faint tint behind the cover (visible while it loads or when there is none).
        modifier
            .clip(shape)
            .background(AppTheme.colors.onSurface.copy(alpha = 0.08f))
            .shimmerApply(),
        contentAlignment = Alignment.Center
    ) {
        if (artwork != null) artwork()
        else if (model != null) AsyncImage(
            model,
            title,
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        else MillenniumIcon(
            placeholderIcon,
            null,
            modifier = Modifier.size(24.dp),
            tint = AppTheme.colors.onSurfaceVariant
        )
    }
}
