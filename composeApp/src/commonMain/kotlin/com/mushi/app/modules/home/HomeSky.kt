package com.mushi.app.modules.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** The sky behind the Home greeting: its colours and where the sun or moon sits. */
internal enum class SkyTime(
    val top: Color,
    val bottom: Color,
    val orb: Color,
    /** Fraction of the width, then dp from the top. */
    val orbX: Float,
    val orbY: Float,
    val orbRadius: Float,
    val hasStars: Boolean = false) {
    Morning(Color(0xFFE8785F), Color(0xFFF2B880), Color(0xFFFFF0C2), 0.74f, 88f, 15f),
    Day(Color(0xFF2F6FC4), Color(0xFF7FB4E6), Color(0xFFFFFBE8), 0.72f, 78f, 12f),
    Evening(Color(0xFF3B2466), Color(0xFFC4506A), Color(0xFFFFB36B), 0.76f, 94f, 15f),
    Night(Color(0xFF0B1440), Color(0xFF242A66), Color(0xFFE8ECFF), 0.73f, 80f, 10f, hasStars = true)
}

internal fun skyTimeFor(hour: Int): SkyTime = when (hour) {
    in 5..10 -> SkyTime.Morning
    in 11..17 -> SkyTime.Day
    in 18..20 -> SkyTime.Evening
    else -> SkyTime.Night
}

@Composable
internal fun rememberSkyTime(): SkyTime = remember {
    skyTimeFor(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour)
}

/** Fixed star spots, as fractions of the width and of the sky's height. */
private val STARS = listOf(
    0.08f to 0.20f, 0.19f to 0.09f, 0.33f to 0.16f, 0.47f to 0.07f, 0.58f to 0.22f,
    0.66f to 0.11f, 0.84f to 0.28f, 0.92f to 0.13f, 0.12f to 0.46f, 0.88f to 0.50f)

/** Draws a still sky over the top of the page, so it costs nothing while nothing moves. */
internal fun DrawScope.drawSky(time: SkyTime, background: Color, darkPage: Boolean, alpha: Float) {
    val strength = if (darkPage) 0.6f else 0.38f
    val height = 300.dp.toPx().coerceAtMost(size.height)
    drawRect(
        brush = Brush.verticalGradient(
            0f to lerp(background, time.top, strength),
            0.55f to lerp(background, time.bottom, strength * 0.7f),
            1f to Color.Transparent,
            endY = height),
        size = Size(size.width, height),
        alpha = 0.75f * alpha)

    if (time.hasStars && darkPage) {
        STARS.forEachIndexed { index, (x, y) ->
            drawCircle(
                color = Color.White,
                radius = (if (index % 3 == 0) 1.4f else 0.9f).dp.toPx(),
                center = Offset(x * size.width, y * height),
                alpha = 0.55f * alpha)
        }
    }

    val center = Offset(time.orbX * size.width, time.orbY.dp.toPx())
    val radius = time.orbRadius.dp.toPx()
    drawCircle(
        brush = Brush.radialGradient(
            listOf(time.orb.copy(alpha = 0.45f), Color.Transparent), center, radius * 3f),
        radius = radius * 3f,
        center = center,
        alpha = alpha)
    drawCircle(color = time.orb, radius = radius, center = center, alpha = 0.9f * alpha)
}
