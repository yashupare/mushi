package com.mushi.app.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mushi.app.core.ui.motionOrSnap

/*
 * Play and pause as one shape that morphs between them. Each is two four-cornered shapes on a
 * 24-unit grid: pause is two bars; play is a triangle split down the middle into a left
 * trapezoid and a right triangle (whose two right-hand corners meet at the tip).
 */
private val PauseLeft = listOf(Offset(6f, 5f), Offset(10f, 5f), Offset(10f, 19f), Offset(6f, 19f))
private val PauseRight = listOf(Offset(14f, 5f), Offset(18f, 5f), Offset(18f, 19f), Offset(14f, 19f))
private val PlayLeft = listOf(Offset(7f, 4.5f), Offset(13.25f, 8.25f), Offset(13.25f, 15.75f), Offset(7f, 19.5f))
private val PlayRight = listOf(Offset(13.25f, 8.25f), Offset(19.5f, 12f), Offset(19.5f, 12f), Offset(13.25f, 15.75f))

/**
 * A play/pause glyph that morphs between the two states: the pause bars bend into the two halves
 * of the play triangle, and back. [contentDescription] describes the action, as for an icon.
 */
@Composable
fun PlayPauseGlyph(
    isPlaying: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color,
) {
    // No overshoot: corners past their end positions would cross over each other.
    val progress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = motionOrSnap(spring(dampingRatio = 1f, stiffness = 420f)),
        label = "play_pause_morph")
    Canvas(modifier.semantics { this.contentDescription = contentDescription }) {
        val p = progress.coerceIn(0f, 1f)
        drawQuad(PlayLeft, PauseLeft, p, tint)
        drawQuad(PlayRight, PauseRight, p, tint)
    }
}

private fun DrawScope.drawQuad(from: List<Offset>, to: List<Offset>, t: Float, color: Color) {
    val unit = size.minDimension / 24f
    val left = (size.width - 24f * unit) / 2f
    val top = (size.height - 24f * unit) / 2f
    fun point(i: Int): Offset {
        val a = from[i]
        val b = to[i]
        return Offset(left + (a.x + (b.x - a.x) * t) * unit, top + (a.y + (b.y - a.y) * t) * unit)
    }
    val path = Path().apply {
        val start = point(0)
        moveTo(start.x, start.y)
        for (i in 1..3) point(i).let { lineTo(it.x, it.y) }
        close()
    }
    // Slightly rounded corners, like the rest of the app's icons.
    drawPath(path, color, style = Fill)
    drawPath(
        path,
        color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 1.6f * unit,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(0.8f * unit)))
}
