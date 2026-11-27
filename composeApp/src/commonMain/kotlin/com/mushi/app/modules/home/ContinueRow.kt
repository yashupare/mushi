package com.mushi.app.modules.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.ui.component.cards.MediaArtwork
import com.mushi.app.core.ui.component.dragScrollable
import com.mushi.app.core.ui.pressScale
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.home_continue_time_left
import com.mushi.app.generated.resources.home_continue_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContinueRow(cards: List<ContinueCard>, onResume: (ContinueCard) -> Unit) {
    val rowState = rememberLazyListState()
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(Res.string.home_continue_title),
            style = AppTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp))
        LazyRow(
            state = rowState,
            modifier = Modifier.dragScrollable(rowState),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cards, key = { it.key }) { card ->
                ContinueTile(card, onClick = { onResume(card) })
            }
        }
    }
}

@Composable
private fun ContinueTile(card: ContinueCard, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(260.dp)
            .pressScale(0.97f)
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.07f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MediaArtwork(card.entry.artworkModel, card.title, Modifier.size(52.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = card.title,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(
                text = stringResource(Res.string.home_continue_time_left, card.timeLeft),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant)
            Box(
                Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp))
                    .background(AppTheme.colors.onSurface.copy(alpha = 0.14f))) {
                Box(
                    Modifier.fillMaxWidth(card.progress.coerceIn(0f, 1f)).fillMaxHeight()
                        .background(AppTheme.colors.primary))
            }
        }
    }
}
