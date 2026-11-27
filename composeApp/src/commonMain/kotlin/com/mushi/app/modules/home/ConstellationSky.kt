package com.mushi.app.modules.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import com.mushi.app.core.ui.LocalReduceMotion
import com.mushi.app.core.ui.component.AdaptiveDialogBottomSheet
import com.mushi.app.core.ui.component.cards.MediaArtwork
import com.mushi.app.core.ui.drawSparkle
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.core.ui.theming.rememberArtworkTint
import com.mushi.app.generated.resources.*
import kotlin.time.Duration
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumTextField

/** The sky is night in either theme. */
private val SKY_TOP = Color(0xFF080C26)
private val SKY_MIDDLE = Color(0xFF161A40)
private val SKY_BOTTOM = Color(0xFF241B3F)
private val STAR_COLORS = listOf(
    Color(0xFFFF9A7A), Color(0xFF5FE3C0), Color(0xFFFF6FA8), Color(0xFF7FD0FF), Color(0xFFFFD166), Color(0xFFB49BFF))

private val SKY_HEIGHT = 320.dp
private val HIT_RADIUS = 26.dp

/** The kept moments as stars, what's selected below them, and the saved constellations. */
@Composable
internal fun ConstellationSection(viewModel: SkyViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (!state.visible) return
    val position = viewModel.position.collectAsState()

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Sky(state, position, viewModel)
        SkyCard(state, viewModel)
        state.saved.forEach { saved ->
            SavedRow(saved, onPlay = { viewModel.playSaved(saved.id) }, onDelete = { viewModel.delete(saved.id) })
        }
    }

    if (state.naming) NameDialog(onSave = viewModel::save, onDismiss = viewModel::cancelName)
}

@Composable
private fun Sky(state: SkyUiState, position: State<Duration>, viewModel: SkyViewModel) {
    val reduceMotion = LocalReduceMotion.current
    // Each star takes its cover's colour
    val colors = state.stars.mapIndexed { index, star ->
        key(star.id) {
            val tint = rememberArtworkTint(star.artworkUrl)
            tint?.let { lerp(it, Color.White, 0.4f) } ?: STAR_COLORS[index % STAR_COLORS.size]
        }
    }
    // A played path draws itself once
    val drawn = remember { Animatable(1f) }
    LaunchedEffect(state.playing) {
        if (state.playing.size > 1 && !reduceMotion) {
            drawn.snapTo(0f)
            drawn.animateTo(1f, tween(900))
        }
    }
    val latest by rememberUpdatedState(state)

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(SKY_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(SKY_TOP, SKY_MIDDLE, SKY_BOTTOM)))
            .pointerInput(Unit) {
                // Only a press on a star is taken; anywhere else the page scrolls as usual
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    fun starAt(at: Offset): StarUi? = latest.stars.minByOrNull {
                        (Offset(it.x * size.width, it.y * size.height) - at).getDistance()
                    }?.takeIf {
                        (Offset(it.x * size.width, it.y * size.height) - at).getDistance() <= HIT_RADIUS.toPx()
                    }

                    val first = starAt(down.position)
                    if (first == null) {
                        var moved = false
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
                            if (!change.pressed) {
                                if (!moved && !change.isConsumed) viewModel.select(null)
                                break
                            }
                        }
                        return@awaitEachGesture
                    }

                    down.consume()
                    var linking = false
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        change.consume()
                        if (!linking && (change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                            linking = true
                            viewModel.startLink(first.id)
                        }
                        if (linking) starAt(change.position)?.let { viewModel.link(it.id) }
                        if (!change.pressed) break
                    }
                    if (linking) viewModel.endLink() else viewModel.select(first.id)
                }
            }
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        Canvas(Modifier.fillMaxSize()) {
            // Stardust
            for (i in 0 until 60) {
                drawCircle(
                    Color.White,
                    radius = (if (i % 9 == 0) 1.3f else 0.7f).dp.toPx(),
                    center = Offset((i * 0.377f % 1f) * size.width, (i * 0.613f % 1f) * size.height),
                    alpha = 0.2f + (i % 5) / 10f)
            }

            val spots = state.stars.associate { it.id to Offset(it.x * size.width, it.y * size.height) }
            val line = state.path.ifEmpty { state.playing }.mapNotNull(spots::get)
            if (line.size > 1) {
                val segments = (line.size - 1) * drawn.value
                for (i in 0 until line.size - 1) {
                    val part = (segments - i).coerceIn(0f, 1f)
                    if (part <= 0f) break
                    val end = line[i] + (line[i + 1] - line[i]) * part
                    drawLine(Color(0x4DAAB6FF), line[i], end, strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(Color(0xD9FFFFFF), line[i], end, strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
                }
            }

            // As the sleep timer runs, stars go out one by one
            val gone = ((state.sleepFraction ?: 0f) * state.stars.size).toInt()
            state.stars.forEachIndexed { index, star ->
                if (index < gone) return@forEachIndexed
                val center = spots.getValue(star.id)
                val color = colors[index]
                val big = star.id == state.selected?.id || star.id in state.path
                val radius = (if (!star.kept) 7f else if (big) 14f else 10f).dp.toPx()
                if (star.kept) {
                    drawCircle(
                        Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), center, radius * 2.4f),
                        radius * 2.4f, center)
                }
                drawSparkle(center, radius, color.copy(alpha = if (star.kept) 1f else 0.45f))
                if (star.kept) drawCircle(Color.White, radius * 0.18f, center)
            }

            // A light travels from the playing star towards the next one. The position is read
            // here, so only drawing repeats, and only while the sky is on screen.
            val playingId = state.playing.getOrNull(state.playingIndex)
            val from = playingId?.let(spots::get)
            if (from != null) {
                val nextId = state.playing.getOrNull(state.playingIndex + 1)
                val star = state.stars.first { it.id == playingId }
                val left = (star.durationMs - star.positionMs).coerceAtLeast(1)
                val progress = if (reduceMotion) 0f
                else ((position.value.inWholeMilliseconds - star.positionMs).toFloat() / left).coerceIn(0f, 1f)
                // Along the drawn line, which can pass stars that don't play on their own
                val at = pointAlong(lightRoute(state.path.ifEmpty { state.playing }, playingId, nextId)
                    .mapNotNull(spots::get).ifEmpty { listOf(from) }, progress)
                drawCircle(Brush.radialGradient(listOf(Color(0xCCFFFFFF), Color.Transparent), at, 14.dp.toPx()), 14.dp.toPx(), at)
                drawCircle(Color.White, 3.5.dp.toPx(), at)
            }

            state.sleepFraction?.let { drawRect(Color.Black, alpha = 0.6f * it) }
        }

        // Path numbers, and a target for screen readers on every star
        state.stars.forEach { star ->
            val order = state.path.indexOf(star.id)
            val label = if (star.kept) "${star.title}, ${star.time}" else star.title
            val half = with(density) { 22.dp.toPx() }
            Box(
                Modifier
                    .offset { IntOffset((star.x * width - half).toInt(), (star.y * height - half).toInt()) }
                    .size(44.dp)
                    .semantics {
                        contentDescription = label
                        role = Role.Button
                        onClick { viewModel.select(star.id); true }
                    },
                contentAlignment = Alignment.BottomCenter) {
                if (order >= 0) Text(
                    "${order + 1}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold)
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.sky_title),
                    color = Color.White,
                    style = AppTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text(
                    text = when {
                        state.momentCount == 0 -> stringResource(Res.string.sky_hint_new)
                        else -> listOfNotNull(
                            state.month,
                            pluralStringResource(Res.plurals.sky_moment_count, state.momentCount, state.momentCount))
                            .joinToString(" · ")
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    style = AppTheme.typography.bodySmall)
            }
            if (state.hasEarlier || state.hasLater) {
                MonthStep("‹", stringResource(Res.string.sky_previous_month), state.hasEarlier) { viewModel.showMonth(-1) }
                MonthStep("›", stringResource(Res.string.sky_next_month), state.hasLater) { viewModel.showMonth(1) }
            }
        }
    }
}

/** The stars the light passes on its way from [fromId] to [nextId] along [line]. */
internal fun lightRoute(line: List<String>, fromId: String, nextId: String?): List<String> {
    if (nextId == null) return listOf(fromId)
    val start = line.indexOf(fromId)
    val end = line.indexOf(nextId)
    return if (start >= 0 && end > start) line.subList(start, end + 1) else listOf(fromId, nextId)
}

/** The point [fraction] of the way along a line of [points]. */
internal fun pointAlong(points: List<Offset>, fraction: Float): Offset {
    val lengths = points.zipWithNext { a, b -> (b - a).getDistance() }
    var left = lengths.sum() * fraction
    lengths.forEachIndexed { i, length ->
        if (left <= length && length > 0f) return points[i] + (points[i + 1] - points[i]) * (left / length)
        left -= length
    }
    return points.last()
}

@Composable
private fun MonthStep(symbol: String, label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center) {
        Text(symbol, color = Color.White.copy(alpha = if (enabled) 1f else 0.3f), fontSize = 22.sp)
    }
}

@Composable
private fun CardRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.07f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content)
}

@Composable
private fun SkyCard(state: SkyUiState, viewModel: SkyViewModel) {
    val selected = state.selected
    when {
        state.path.size > 1 -> CardRow {
            Text(
                pluralStringResource(Res.plurals.sky_star_count, state.path.size, state.path.size),
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(start = 4.dp))
            QuietButton(stringResource(Res.string.sky_clear)) { viewModel.select(null) }
            QuietButton(stringResource(Res.string.sky_save), viewModel::askName)
            MillenniumButton(onClick = viewModel::playPath) { Text(stringResource(Res.string.sky_play_path)) }
        }

        selected != null -> CardRow {
            MediaArtwork(selected.artworkModel, selected.title, Modifier.size(48.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    selected.title,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(
                    if (selected.kept) stringResource(Res.string.sky_kept_at, selected.time) else selected.artists,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
            if (selected.kept) {
                MillenniumIconButton(onClick = viewModel::removeSelected, style = MillenniumIconButtonStyle.Quiet) {
                    MillenniumIcon(
                        com.mushi.app.core.ui.theming.MushiIcons.Trash,
                        contentDescription = stringResource(Res.string.sky_remove_moment),
                        tint = AppTheme.colors.onSurfaceVariant)
                }
            }
            MillenniumButton(onClick = viewModel::playSelected) { Text(stringResource(Res.string.sky_play)) }
        }

        state.momentCount > 0 -> Text(
            stringResource(Res.string.sky_hint_link),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun QuietButton(label: String, onClick: () -> Unit) {
    MillenniumButton(onClick = onClick, style = MillenniumButtonStyle.Quiet, foreground = AppTheme.colors.primary) {
        Text(label, style = AppTheme.typography.labelLarge)
    }
}

@Composable
private fun SavedRow(saved: ConstellationUi, onPlay: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.05f))
            .clickable(onClick = onPlay)
            .padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MillenniumIcon(
            com.mushi.app.core.ui.theming.MushiIcons.Play,
            contentDescription = null,
            tint = AppTheme.colors.primary,
            modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                saved.name,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(
                pluralStringResource(Res.plurals.sky_star_count, saved.stars, saved.stars),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant)
        }
        MillenniumIconButton(onClick = onDelete, style = MillenniumIconButtonStyle.Quiet) {
            MillenniumIcon(
                com.mushi.app.core.ui.theming.MushiIcons.Close,
                contentDescription = stringResource(Res.string.sky_delete_constellation, saved.name),
                tint = AppTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun NameDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val name = rememberTextFieldState("")
    AdaptiveDialogBottomSheet(
        onDismiss = onDismiss,
        dialogTitle = stringResource(Res.string.sky_name_title),
        title = { Text(stringResource(Res.string.sky_name_title), style = AppTheme.typography.titleMedium) },
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuietButton(stringResource(Res.string.common_cancel), onDismiss)
                MillenniumButton(
                    onClick = { onSave(name.text.toString()) },
                    enabled = name.text.isNotBlank()) {
                    Text(stringResource(Res.string.sky_save))
                }
            }
        }) {
        MillenniumTextField(
            state = name,
            label = "",
            placeholder = stringResource(Res.string.sky_name_placeholder),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
    }
}
