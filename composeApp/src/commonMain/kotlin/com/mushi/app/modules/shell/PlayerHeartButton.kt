package com.mushi.app.modules.shell


import kotlinx.coroutines.launch
import androidx.compose.ui.draw.drawWithContent
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import androidx.compose.foundation.shape.CircleShape

import com.mushi.app.core.ui.theming.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.modules.saved_tracks.SavedState
import com.mushi.app.modules.saved_tracks.SavedTracksViewModel
import com.mushi.app.modules.saved_tracks.rememberIsSavedTracks
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxHeart
import com.mushi.app.resources.iconsax.IconsaxHeart2
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.player_like_content_description
import com.mushi.app.generated.resources.player_unlike_content_description

/** Whether the current track is liked, and how to change that. */
internal class CurrentTrackLike(
    val trackId: String?,
    val isLiked: Boolean,
    /** False until the saved state is known, so a tap can't undo an unseen like. */
    val isKnown: Boolean,
    val isPending: Boolean,
    private val viewModel: SavedTracksViewModel
) {
    fun toggle() {
        trackId?.let { viewModel.toggleSavedTrack(it, isLiked) }
    }
}

@Composable
internal fun rememberCurrentTrackLike(
    audioPlayerQueue: AudioPlayerQueue,
    savedTracksViewModel: SavedTracksViewModel
): CurrentTrackLike {
    val currentQueueEntry by audioPlayerQueue.currentQueueEntryFlow.collectAsStateWithLifecycle()
    val currentTrackId = (currentQueueEntry as? QueueEntry.StreamingTrack)?.track?.id
    val isSavedTrackState =
        rememberIsSavedTracks(trackIds = currentTrackId?.let { listOf(it) } ?: emptyList())
    val savedTrackIds by savedTracksViewModel.savedTrackIdsFlow.collectAsStateWithLifecycle()
    val pendingSavedTrackIds by savedTracksViewModel.pendingSavedTrackIds.collectAsStateWithLifecycle()

    val isInSavedIds = if (currentTrackId != null) currentTrackId in savedTrackIds else false
    val isLiked = if (isSavedTrackState is SavedState.Success) {
        isSavedTrackState.data.firstOrNull() == true || isInSavedIds
    } else {
        isInSavedIds
    }

    return CurrentTrackLike(
        trackId = currentTrackId,
        isLiked = isLiked,
        isKnown = isSavedTrackState is SavedState.Success,
        isPending = currentTrackId != null && currentTrackId in pendingSavedTrackIds,
        viewModel = savedTracksViewModel)
}

@Composable
fun PlayerHeartButton(
    audioPlayerQueue: AudioPlayerQueue,
    savedTracksViewModel: SavedTracksViewModel) {
    val like = rememberCurrentTrackLike(audioPlayerQueue, savedTracksViewModel)
    val isLiked = like.isLiked

    val haptics = com.mushi.app.core.ui.rememberHaptics()

    // A small pop when a track becomes liked, so the action feels confirmed.
    val pop = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(1f) }
    // The burst around the heart: 0 when it starts, 1 when it has faded out (1 = nothing drawn).
    val burst = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(1f) }
    val reduceMotion = com.mushi.app.core.ui.LocalReduceMotion.current
    val burstColors = com.mushi.app.core.ui.theming.LocalMushiBrand.current.gradient
    var seenLiked by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(isLiked) }
    // The burst plays on the tap itself (saving through the plugin can take a moment, and the
    // button shows a spinner meanwhile), so liking always feels immediate.
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    fun playBurst() {
        if (reduceMotion) return
        scope.launch {
            burst.snapTo(0f)
            burst.animateTo(1f, androidx.compose.animation.core.tween(650, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
        }
    }
    androidx.compose.runtime.LaunchedEffect(isLiked) {
        if (isLiked && !seenLiked) {
            pop.snapTo(0.6f)
            pop.animateTo(1f, com.mushi.app.core.ui.pressSpring())
        }
        seenLiked = isLiked
    }

    // The burst is drawn around the whole button, so the saving spinner never hides it.
    androidx.compose.foundation.layout.Box(
        Modifier.drawWithContent {
            drawContent()
            drawLikeBurst(burst.value, burstColors)
        }) {
    MillenniumIconButton(
        onClick = {
            if (like.trackId != null) {
                haptics.tap()
                if (!isLiked) playBurst()
                like.toggle()
            }
        },
        enabled = like.isKnown,
        loading = like.isPending,
        style = MillenniumIconButtonStyle.Quiet) {
        MillenniumIcon(
            imageVector = if (isLiked) Iconsax.IconsaxHeart2 else Iconsax.IconsaxHeart,
            contentDescription = stringResource(if (isLiked) Res.string.player_unlike_content_description else Res.string.player_like_content_description),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                },
            tint = if (isLiked) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant)
    }
    }
}

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLikeBurst(
    progress: Float,
    colors: List<androidx.compose.ui.graphics.Color>,
) {
    if (progress >= 1f || colors.isEmpty()) return
    val fade = 1f - progress
    val eased = 1f - (1f - progress) * (1f - progress)
    // Sized from the heart icon (about 55% of the 44 dp button), not the whole button.
    val unit = size.minDimension * 0.55f
    drawCircle(
        color = colors[colors.size / 2].copy(alpha = 0.8f * fade),
        radius = unit * (0.4f + 0.55f * eased),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = unit * 0.12f * fade))
    for (i in 0 until 8) {
        val angle = (i * 45f + 22.5f) * (kotlin.math.PI.toFloat() / 180f)
        val distance = unit * (0.45f + 0.6f * eased)
        drawCircle(
            color = colors[i % colors.size].copy(alpha = fade),
            radius = unit * 0.13f * (0.45f + 0.55f * fade),
            center = center + androidx.compose.ui.geometry.Offset(
                kotlin.math.cos(angle) * distance,
                kotlin.math.sin(angle) * distance))
    }
}
