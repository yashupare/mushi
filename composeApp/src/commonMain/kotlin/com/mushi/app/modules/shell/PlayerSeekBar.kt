package com.mushi.app.modules.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.player_seek_label
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumProgress
import org.spotube.millennium.composables.MillenniumSlider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** How far [position] is through [duration], 0 to 1 (0 while the duration is unknown). */
private fun progressOf(position: Duration, duration: Duration): Float {
    val total = duration.inWholeMilliseconds
    return if (total > 0L) (position.inWholeMilliseconds.toFloat() / total).coerceIn(0f, 1f) else 0f
}

/**
 * The seek slider with the elapsed and total times underneath.
 *
 * It is the only part of a player that reads the playback [position], which ticks four times a
 * second, so a tick recomposes this bar and nothing else (the players themselves are large and
 * the full player stays composed even while collapsed). While dragging it shows the drag target;
 * after release it holds the target until the player reports the new position, so the thumb
 * doesn't jump back for a moment.
 */
@Composable
internal fun PlayerSeekBar(
    position: State<Duration>,
    seekDuration: Duration,
    displayDuration: Duration,
    onSeek: (Duration) -> Unit,
    modifier: Modifier = Modifier,
    timeStyle: TextStyle = AppTheme.typography.labelSmall,
    timesTopPadding: Dp = 0.dp,
) {
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var pendingSeek by remember { mutableStateOf<Float?>(null) }

    // Once the player reports a position after a seek, show the real position again.
    LaunchedEffect(pendingSeek) {
        if (pendingSeek == null) return@LaunchedEffect
        snapshotFlow { position.value }.drop(1).first()
        pendingSeek = null
    }

    val shownProgress = when {
        isDragging -> dragProgress
        pendingSeek != null -> pendingSeek!!
        else -> progressOf(position.value, seekDuration)
    }
    val canSeek = seekDuration.inWholeMilliseconds > 0L

    Column(modifier) {
        MillenniumSlider(
            value = shownProgress,
            onValueChange = {
                dragProgress = it
                isDragging = true
            },
            onValueChangeFinished = {
                isDragging = false
                if (canSeek) {
                    pendingSeek = dragProgress
                    onSeek((seekDuration.inWholeMilliseconds * dragProgress.coerceIn(0f, 1f)).toLong().milliseconds)
                }
            },
            enabled = canSeek,
            label = stringResource(Res.string.player_seek_label),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = timesTopPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val elapsed = if (isDragging || pendingSeek != null) {
                (seekDuration.inWholeMilliseconds * shownProgress).toLong().milliseconds
            } else {
                position.value
            }
            Text(
                text = formatPlayerTime(elapsed),
                style = timeStyle,
                color = AppTheme.colors.onSurfaceVariant
            )
            Text(
                text = formatPlayerTime(displayDuration),
                style = timeStyle,
                color = AppTheme.colors.onSurfaceVariant
            )
        }
    }
}

/**
 * The mini player's thin progress line. Drawn directly rather than with MillenniumProgress: that
 * one springs to each new value, and with the position changing four times a second the spring
 * never settled, so the app rendered continuously on every page while music played. Here
 * [position] is read only while drawing, so each tick is a single cheap redraw.
 */
@Composable
internal fun MiniPlayerProgress(position: State<Duration>, seekDuration: Duration, modifier: Modifier = Modifier) {
    val fill = AppTheme.colors.onSurface
    val track = fill.copy(alpha = 0.12f)
    androidx.compose.foundation.Canvas(modifier.fillMaxWidth().height(2.dp)) {
        drawRect(track)
        val progress = progressOf(position.value, seekDuration)
        if (progress > 0f) {
            drawRect(fill, size = androidx.compose.ui.geometry.Size(size.width * progress, size.height))
        }
    }
}
