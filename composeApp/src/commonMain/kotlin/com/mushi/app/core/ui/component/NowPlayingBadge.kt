package com.mushi.app.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.ui.LocalReduceMotion
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.media_now_playing
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Bar heights for each step of the meter; the first row is also the resting picture. */
private val BarSteps = listOf(
    floatArrayOf(0.5f, 0.9f, 0.65f),
    floatArrayOf(0.85f, 0.5f, 1f),
    floatArrayOf(0.4f, 0.75f, 0.45f),
    floatArrayOf(1f, 0.6f, 0.8f),
    floatArrayOf(0.65f, 1f, 0.35f),
)

/**
 * Three small bars on the cover of the album or playlist that is playing. They step a few times
 * a second rather than animating every frame, so a page showing them stays mostly idle, and hold
 * still while paused.
 */
@Composable
fun NowPlayingBadge(modifier: Modifier = Modifier) {
    val player: AudioPlayerInterface = koinInject()
    val state by player.playerStateFlow.collectAsStateWithLifecycle()
    val moving = state == PlayerState.PLAYING && !LocalReduceMotion.current
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(moving) {
        while (moving) {
            delay(250)
            step = (step + 1) % BarSteps.size
        }
    }
    val label = stringResource(Res.string.media_now_playing)
    Box(
        modifier
            .size(24.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(24.dp).padding(6.dp)) {
            val heights = BarSteps[step]
            val gap = size.width * 0.14f
            val width = (size.width - 2 * gap) / 3
            heights.forEachIndexed { i, h ->
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(i * (width + gap), size.height * (1f - h)),
                    size = Size(width, size.height * h),
                    cornerRadius = CornerRadius(width / 2f))
            }
        }
    }
}
