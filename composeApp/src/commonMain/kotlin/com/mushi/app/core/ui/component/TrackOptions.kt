package com.mushi.app.core.ui.component


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumModalBottomSheet
import com.composeunstyled.SheetDetent
import com.composeunstyled.rememberModalBottomSheetState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.Iconsax3DotsMore
import com.mushi.app.resources.iconsax.IconsaxAddSquare
import com.mushi.app.resources.iconsax.IconsaxCd
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.resources.iconsax.IconsaxHeart
import com.mushi.app.resources.iconsax.IconsaxHeart2
import com.mushi.app.resources.iconsax.IconsaxMusicPlaylist
import com.mushi.app.resources.iconsax.IconsaxMusicSquareRemove
import com.mushi.app.resources.iconsax.IconsaxMusicSquareRemoveFilled
import com.mushi.app.resources.iconsax.IconsaxNext
import com.mushi.app.resources.iconsax.IconsaxShare
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*
import androidx.compose.foundation.background

sealed interface TrackOptionsAction {
    data object AddToJam : TrackOptionsAction
    data object StartRadio : TrackOptionsAction
    data object PlayNext : TrackOptionsAction
    data object AddToQueue : TrackOptionsAction
    data object RemoveFromQueue : TrackOptionsAction
    data object ToggleFavorite : TrackOptionsAction
    data object AddToPlaylist : TrackOptionsAction
    data object Download : TrackOptionsAction
    data object ToggleBlacklist : TrackOptionsAction
    data object Share : TrackOptionsAction
}

data class TrackOptionsState(
    val isInQueue: Boolean = false,
    val isCurrentlyPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isBlacklisted: Boolean = false)

data class TrackOptionsContext(
    val currentTrackId: String? = null,
    val queueTrackIds: Set<String> = emptySet(),
    val savedTrackIds: Set<String> = emptySet(),
    val blacklistedTrackIds: Set<String> = emptySet(),
    val blacklistedArtistIds: Set<String> = emptySet(),
    val forceFavorite: Boolean = false) {
    fun stateFor(track: MetadataTrack): TrackOptionsState = TrackOptionsState(
        isInQueue = track.id in queueTrackIds,
        isCurrentlyPlaying = track.id == currentTrackId,
        isFavorite = forceFavorite || track.id in savedTrackIds,
        isBlacklisted = track.id in blacklistedTrackIds ||
            track.artists.any { it.id in blacklistedArtistIds })
}

/**
 * Wraps [onAction] so confirmed actions (queue, like, download, playlist) give a light tap.
 * Menu navigation actions such as Share stay silent.
 */
@Composable
private fun rememberHapticTrackAction(
    onAction: (TrackOptionsAction) -> Unit
): (TrackOptionsAction) -> Unit {
    val haptics = com.mushi.app.core.ui.rememberHaptics()
    val current by androidx.compose.runtime.rememberUpdatedState(onAction)
    return remember(haptics) {
        { action ->
            when (action) {
                TrackOptionsAction.AddToQueue,
                TrackOptionsAction.PlayNext,
                TrackOptionsAction.RemoveFromQueue,
                TrackOptionsAction.ToggleFavorite,
                TrackOptionsAction.Download,
                TrackOptionsAction.AddToPlaylist -> haptics.tap()
                else -> Unit
            }
            current(action)
        }
    }
}

@Composable
fun TrackOptions(
    track: MetadataTrack,
    state: TrackOptionsState,
    onAction: (TrackOptionsAction) -> Unit,
    onAlbumClick: () -> Unit,
    modifier: Modifier = Modifier,
    isInJam: Boolean = false,
    isJamGuest: Boolean = false) {
    val hapticAction = rememberHapticTrackAction(onAction)
    AdaptiveDropdownBottomSheet(
        items = buildTrackMenuItems(
            track = track,
            state = state,
            onAction = hapticAction,
            onAlbumClick = onAlbumClick,
            isInJam = isInJam,
            isJamGuest = isJamGuest),
        trigger = { onClick ->
            MillenniumIconButton(
                onClick = onClick,
                style = MillenniumIconButtonStyle.Quiet) {
                MillenniumIcon(
                    imageVector = com.mushi.app.core.ui.theming.MushiIcons.More,
                    contentDescription = stringResource(Res.string.track_options_title),
                    modifier = Modifier.shimmerApply()
                )
            }
        },
        headerDisplayMode = HeaderDisplayMode.OnlyInBottomSheet,
        header = { TrackOptionsSheetHeader(track = track) },
        modifier = modifier)
}

@Composable
fun TrackOptionsBottomSheet(
    track: MetadataTrack,
    state: TrackOptionsState,
    onDismiss: () -> Unit,
    onAction: (TrackOptionsAction) -> Unit,
    onAlbumClick: () -> Unit,
    isInJam: Boolean = false,
    isJamGuest: Boolean = false) {
    val hapticAction = rememberHapticTrackAction(onAction)
    val sheetState = rememberModalBottomSheetState(initialDetent = SheetDetent.FullyExpanded)
    LaunchedEffect(sheetState.currentDetent) {
        if (sheetState.currentDetent == SheetDetent.Hidden) {
            onDismiss()
        }
    }
    MillenniumModalBottomSheet(
        state = sheetState,
        contentPadding = PaddingValues(top = 10.dp, bottom = 8.dp),
        contentSpacing = 4.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackOptionsSheetHeader(track = track)

            QuickActions(
                track = track,
                state = state,
                isJamGuest = isJamGuest,
                onAction = { action ->
                    hapticAction(action)
                    onDismiss()
                })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                buildTrackMenuItems(
                    withQuickActions = false,
                    track = track,
                    state = state,
                    onAction = { action ->
                        hapticAction(action)
                        onDismiss()
                    },
                    onAlbumClick = {
                        onAlbumClick()
                        onDismiss()
                    },
                    isInJam = isInJam,
                    isJamGuest = isJamGuest).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = item.enabled,
                                onClick = item.onClick)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        item.icon?.let { icon ->
                            MillenniumIcon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = if (item.enabled) {
                                    AppTheme.colors.onSurface
                                } else {
                                    AppTheme.colors.onSurface.copy(alpha = 0.38f)
                                })
                        } ?: Spacer(modifier = Modifier.size(22.dp))
                        Text(
                            text = item.label,
                            style = AppTheme.typography.bodyLarge,
                            color = if (item.enabled) {
                                AppTheme.colors.onSurface
                            } else {
                                AppTheme.colors.onSurface.copy(alpha = 0.38f)
                            })
                    }
                }
            }
        }
    }
}

/** The three things people do most, as large buttons above the rest of the menu. */
@Composable
private fun QuickActions(
    track: MetadataTrack,
    state: TrackOptionsState,
    isJamGuest: Boolean,
    onAction: (TrackOptionsAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickAction(
            icon = if (state.isFavorite) Iconsax.IconsaxHeart2 else Iconsax.IconsaxHeart,
            label = stringResource(
                if (state.isFavorite) Res.string.track_options_quick_liked else Res.string.track_options_quick_like),
            highlighted = state.isFavorite,
            onClick = { onAction(TrackOptionsAction.ToggleFavorite) },
            modifier = Modifier.weight(1f))
        if (!isJamGuest) {
            QuickAction(
                icon = if (state.isInQueue) com.mushi.app.core.ui.theming.MushiIcons.Trash
                    else Iconsax.IconsaxAddSquare,
                label = stringResource(
                    if (state.isInQueue) Res.string.track_options_quick_unqueue
                    else Res.string.player_add_to_queue_content_description),
                onClick = {
                    onAction(
                        if (state.isInQueue) TrackOptionsAction.RemoveFromQueue
                        else TrackOptionsAction.AddToQueue)
                },
                modifier = Modifier.weight(1f))
        }
        if (!track.externalUri.isNullOrBlank()) {
            QuickAction(
                icon = com.mushi.app.core.ui.theming.MushiIcons.Share,
                label = stringResource(Res.string.track_options_share),
                onClick = { onAction(TrackOptionsAction.Share) },
                modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.medium)
            .background(AppTheme.colors.onSurface.copy(alpha = 0.08f))
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MillenniumIcon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (highlighted) AppTheme.colors.primary else AppTheme.colors.onSurface)
        Text(
            text = label,
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun TrackOptionsSheetHeader(track: MetadataTrack) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(
            model = (track.album?.thumbnails ?: track.thumbnails)?.firstOrNull()?.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(AppTheme.shapes.small))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = AppTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Text(
                text = track.artists.joinToString(", ") { it.name },
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            track.album?.let { album ->
                Text(
                    text = album.title,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun buildTrackMenuItems(
    track: MetadataTrack,
    state: TrackOptionsState,
    onAction: (TrackOptionsAction) -> Unit,
    onAlbumClick: () -> Unit,
    isInJam: Boolean = false,
    isJamGuest: Boolean = false,
    /** False where [QuickActions] already shows like, queue and share. */
    withQuickActions: Boolean = true): List<AdaptiveMenuItem> = buildList {
    if (isInJam) {
        add(
            AdaptiveMenuItem(
                icon = Iconsax.IconsaxAddSquare,
                label = stringResource(Res.string.track_options_add_to_jam),
                onClick = { onAction(TrackOptionsAction.AddToJam) }))
    }

    // A guest's queue is the shared jam queue — mutating it locally is not
    // allowed, so queue actions are replaced by "Add to Jam".
    if (!isJamGuest) {
        if (!state.isInQueue && !state.isCurrentlyPlaying) {
            add(
                AdaptiveMenuItem(
                    icon = com.mushi.app.core.ui.theming.MushiIcons.SkipNext,
                    label = stringResource(Res.string.track_options_play_next),
                    onClick = { onAction(TrackOptionsAction.PlayNext) }))
        } else if (state.isInQueue && !state.isCurrentlyPlaying) {
            add(
                AdaptiveMenuItem(
                    icon = com.mushi.app.core.ui.theming.MushiIcons.SkipNext,
                    label = stringResource(Res.string.track_options_move_to_next),
                    onClick = { onAction(TrackOptionsAction.PlayNext) }))
        }

        if (!withQuickActions) {
            // Shown above the list
        } else if (!state.isInQueue) {
            add(
                AdaptiveMenuItem(
                    icon = Iconsax.IconsaxAddSquare,
                    label = stringResource(Res.string.player_add_to_queue_content_description),
                    onClick = { onAction(TrackOptionsAction.AddToQueue) }))
        } else {
            add(
                AdaptiveMenuItem(
                    icon = com.mushi.app.core.ui.theming.MushiIcons.Trash,
                    label = stringResource(Res.string.track_options_remove_from_queue),
                    onClick = { onAction(TrackOptionsAction.RemoveFromQueue) }))
        }
    }

    if (withQuickActions) {
        add(
            AdaptiveMenuItem(
                icon = if (state.isFavorite) Iconsax.IconsaxHeart2 else Iconsax.IconsaxHeart,
                label = if (state.isFavorite) stringResource(Res.string.track_options_remove_from_favorites) else stringResource(Res.string.track_options_save_as_favorite),
                onClick = { onAction(TrackOptionsAction.ToggleFavorite) }))
    }

    add(
        AdaptiveMenuItem(
            icon = com.mushi.app.core.ui.theming.MushiIcons.Playlist,
            label = stringResource(Res.string.track_options_add_to_playlist),
            onClick = { onAction(TrackOptionsAction.AddToPlaylist) }))

    add(
        AdaptiveMenuItem(
            icon = com.mushi.app.core.ui.theming.MushiIcons.Download,
            label = stringResource(Res.string.track_options_download),
            onClick = { onAction(TrackOptionsAction.Download) }))

    add(
        AdaptiveMenuItem(
            icon = com.mushi.app.core.ui.theming.MushiIcons.Forbidden,
            label = if (state.isBlacklisted) stringResource(Res.string.blacklist_remove) else stringResource(Res.string.blacklist_add),
            onClick = { onAction(TrackOptionsAction.ToggleBlacklist) }))

    if (withQuickActions && !track.externalUri.isNullOrBlank()) {
        add(
            AdaptiveMenuItem(
                icon = com.mushi.app.core.ui.theming.MushiIcons.Share,
                label = stringResource(Res.string.track_options_share),
                onClick = { onAction(TrackOptionsAction.Share) }))
    }

    if (track.album != null) {
        add(
            AdaptiveMenuItem(
                icon = com.mushi.app.core.ui.theming.MushiIcons.Album,
                label = stringResource(Res.string.player_go_to_album),
                onClick = onAlbumClick))
    }
}
