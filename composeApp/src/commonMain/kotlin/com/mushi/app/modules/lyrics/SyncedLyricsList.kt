package com.mushi.app.modules.lyrics

import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import org.spotube.plugin_interfaces.plugin_apis.lyrics.LyricsLine
import com.mushi.app.core.ui.theming.AppTheme
import org.spotube.millennium.theme.MillenniumTheme

private const val LYRICS_BLANK_LINE = "\u266A"

/**
 * Time-synced lyrics list that keeps the active line vertically centered.
 *
 * With [onLineClick] it renders Millennium list tiles (the active one is the selected tile);
 * without it, it renders a passive compact preview.
 */
@Composable
fun SyncedLyricsList(
    lines: List<LyricsLine>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    onLineClick: ((LyricsLine) -> Unit)? = null,
    /** Progress through the current line (0 to 1) for the karaoke fill; read while drawing. */
    currentLineProgress: (() -> Float)? = null
) {
    val listState = rememberLazyListState()
    val colors = MillenniumTheme.colors

    BoxWithConstraints(modifier = modifier) {
        // Padding lets the first and last lines reach the resting spot too. The immersive view
        // rests the current line a third of the way down (like Apple Music); the compact
        // preview keeps it centred.
        val immersive = onLineClick != null
        val topPadding = if (immersive) maxHeight / 3 else maxHeight / 2
        val bottomPadding = if (immersive) maxHeight * 2 / 3 else maxHeight / 2

        LaunchedEffect(currentIndex, lines.size) {
            if (currentIndex !in lines.indices) return@LaunchedEffect
            val visible = listState.layoutInfo.visibleItemsInfo
            val itemHeight = visible.firstOrNull { it.index == currentIndex }?.size
                ?: visible.firstOrNull()?.size
                ?: 0
            listState.animateScrollToItem(index = currentIndex, scrollOffset = if (immersive) 0 else itemHeight / 2)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().then(
                if (onLineClick != null) Modifier.fadingEdges(maxHeight * 0.18f) else Modifier
            ),
            contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(if (onLineClick != null) 4.dp else 2.dp)
        ) {
            itemsIndexed(lines, key = { index, line -> "$index-${line.time}" }) { index, line ->
                val isCurrent = index == currentIndex
                val text = line.text.ifBlank { LYRICS_BLANK_LINE }
                val color by animateColorAsState(
                    when {
                        isCurrent -> colors.accent
                        index < currentIndex -> colors.muted.copy(alpha = 0.55f)
                        else -> colors.muted
                    }
                )
                if (onLineClick != null) {
                    ImmersiveLyricLine(
                        text = text,
                        distance = kotlin.math.abs(index - currentIndex),
                        isCurrent = isCurrent,
                        fillProgress = if (isCurrent) currentLineProgress else null,
                        onClick = { onLineClick(line) }
                    )
                } else {
                    Text(
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        style = AppTheme.typography.bodyMedium.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = color
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun ImmersiveLyricLine(
    text: String,
    distance: Int,
    isCurrent: Boolean,
    fillProgress: (() -> Float)?,
    onClick: () -> Unit
) {
    val colors = MillenniumTheme.colors
    // Karaoke fill: the sung part of the current line in full colour, the rest dimmed. The
    // player reports its position in steps of about 250 ms, so the fill eases between them.
    val reduceMotion = com.mushi.app.core.ui.LocalReduceMotion.current
    val fill = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(fillProgress != null) {
        val source = fillProgress ?: return@LaunchedEffect
        fill.snapTo(source())
        snapshotFlow { source() }.collect { target ->
            if (target < fill.value || reduceMotion) {
                fill.snapTo(target)
            } else {
                launch {
                    fill.animateTo(target, tween(260, easing = androidx.compose.animation.core.LinearEasing))
                }
            }
        }
    }
    var layout by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val targetAlpha = when (distance) {
        0 -> 1f
        1 -> 0.7f
        2 -> 0.5f
        else -> 0.32f
    }
    val alpha by animateFloatAsState(targetAlpha, tween(300))
    val scale by animateFloatAsState(if (isCurrent) 1f else 0.9f, tween(300))
    // Every line is the text colour: the current one at full strength, the rest dimmed by the
    // distance-based alpha above. Large, bold and left-aligned, shrinking from the left edge.
    val color = if (fillProgress != null) colors.text.copy(alpha = 0.38f) else colors.text
    val hoverBackground by animateColorAsState(
        if (hovered && !isCurrent) colors.text.copy(alpha = 0.08f) else Color.Transparent
    )

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                this.alpha = if (hovered) maxOf(alpha, 0.85f) else alpha
            }
            .clip(RoundedCornerShape(14.dp))
            .background(hoverBackground)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .drawWithContent {
                drawContent()
                val textLayout = layout ?: return@drawWithContent
                if (fillProgress == null) return@drawWithContent
                drawKaraokeFill(textLayout, fill.value, colors.text)
            },
        onTextLayout = { layout = it },
        style = AppTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            color = color
        ),
        textAlign = TextAlign.Start
    )
}

/**
 * Redraws the sung part of [layout] in [color]: wrapped lines fill in reading order, so a
 * two-line lyric fills its first line before its second.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKaraokeFill(
    layout: androidx.compose.ui.text.TextLayoutResult,
    progress: Float,
    color: Color
) {
    if (progress <= 0f) return
    val widths = (0 until layout.lineCount).map { layout.getLineRight(it) - layout.getLineLeft(it) }
    var remaining = progress.coerceIn(0f, 1f) * widths.sum()
    for (line in 0 until layout.lineCount) {
        if (remaining <= 0f) break
        val left = layout.getLineLeft(line)
        val width = widths[line]
        clipRect(
            left = left,
            top = layout.getLineTop(line),
            right = left + minOf(width, remaining),
            bottom = layout.getLineBottom(line)
        ) {
            drawText(layout, color = color)
        }
        remaining -= width
    }
}

/** Fades the top and bottom [fade] of the content to transparent. */
private fun Modifier.fadingEdges(fade: Dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val fraction = (fade.toPx() / size.height).coerceIn(0f, 0.5f)
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                fraction to Color.Black,
                1f - fraction to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }
