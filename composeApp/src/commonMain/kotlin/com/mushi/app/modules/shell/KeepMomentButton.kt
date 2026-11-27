package com.mushi.app.modules.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.historyKey
import com.mushi.app.core.moments.MomentsRepository
import com.mushi.app.core.ui.drawSparkle
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.player_keep_moment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val HOLD_MS = 600

class KeepMomentViewModel(
    private val repository: MomentsRepository,
    private val audioPlayer: AudioPlayerInterface,
    private val audioPlayerQueue: AudioPlayerQueue) : ViewModel() {

    /** Whether the current song has a kept moment. */
    val hasMoment = combine(repository.library, audioPlayerQueue.currentQueueEntryFlow) { library, entry ->
        entry != null && library.moments.any { it.songKey == entry.historyKey() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** The song and spot playing now, read when the press begins. */
    fun spot(): Pair<QueueEntry, Long>? = audioPlayerQueue.currentQueueEntryFlow.value
        ?.let { it to audioPlayer.positionFlow.value.inWholeMilliseconds }

    fun keep(spot: Pair<QueueEntry, Long>) {
        viewModelScope.launch { repository.toggle(spot.first, spot.second) }
    }
}

/**
 * Hold to keep the moment that's playing, so a stray tap does nothing. A ring fills while
 * holding, then a spark flies up towards the sky.
 */
@Composable
internal fun KeepMomentButton(viewModel: KeepMomentViewModel = koinViewModel()) {
    val hasMoment by viewModel.hasMoment.collectAsStateWithLifecycle()
    val haptics = com.mushi.app.core.ui.rememberHaptics()
    val reduceMotion = com.mushi.app.core.ui.LocalReduceMotion.current
    val scope = rememberCoroutineScope()
    val hold = remember { Animatable(0f) }
    // 1 = nothing in flight
    val spark = remember { Animatable(1f) }
    val label = stringResource(Res.string.player_keep_moment)
    val accent = AppTheme.colors.primary
    val idle = AppTheme.colors.onSurfaceVariant

    val keep by rememberUpdatedState { spot: Pair<QueueEntry, Long> ->
        haptics.tap()
        viewModel.keep(spot)
        if (!reduceMotion) scope.launch {
            spark.snapTo(0f)
            spark.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    val spot = viewModel.spot() ?: return@awaitEachGesture
                    val filling = scope.launch {
                        hold.animateTo(1f, tween(HOLD_MS, easing = LinearEasing))
                        keep(spot)
                        hold.snapTo(0f)
                    }
                    waitForUpOrCancellation()
                    if (filling.isActive) {
                        filling.cancel()
                        scope.launch { hold.animateTo(0f, tween(150)) }
                    }
                }
            }
            .semantics {
                contentDescription = label
                role = Role.Button
                // Screen readers can't hold, so their click keeps straight away
                onClick { viewModel.spot()?.let { keep(it) } != null }
            }
    ) {
        Canvas(Modifier.size(44.dp)) {
            if (hold.value > 0f) {
                val inset = 5.dp.toPx()
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * hold.value,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    style = Stroke(2.5.dp.toPx()))
            }
            drawSparkle(center, 11.dp.toPx(), if (hasMoment) accent else idle, filled = hasMoment)
            val flight = spark.value
            if (flight < 1f) {
                val at = center + Offset(24.dp.toPx() * flight * (1f - flight), -180.dp.toPx() * flight)
                drawCircle(accent.copy(alpha = 0.35f * (1f - flight)), 14.dp.toPx(), at)
                drawSparkle(at, 9.dp.toPx(), accent.copy(alpha = 1f - flight * flight))
            }
        }
    }
}
