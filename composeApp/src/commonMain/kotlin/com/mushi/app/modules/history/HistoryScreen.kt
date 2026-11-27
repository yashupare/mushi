package com.mushi.app.modules.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import com.mushi.app.core.ui.component.AdaptiveDialogBottomSheet
import com.mushi.app.core.ui.component.cards.MediaArtwork
import com.mushi.app.core.ui.component.cards.MediaEmptyState
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val inset = LocalAppShellBottomInset.current

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (!state.isLoading && state.days.isEmpty()) {
            MediaEmptyState(
                com.mushi.app.core.ui.theming.MushiIcons.MusicNote,
                stringResource(Res.string.history_empty),
                Modifier.padding(bottom = inset),
                description = stringResource(Res.string.history_empty_hint))
        } else {
            LazyColumn(
                Modifier.widthIn(max = 960.dp).fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = inset + 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (state.days.isNotEmpty()) item(key = "clear") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        QuietAction(stringResource(Res.string.history_clear), viewModel::askToClear)
                    }
                }
                state.days.forEach { day ->
                    item(key = "day-${day.date}") {
                        Text(
                            text = when (day.label) {
                                HistoryDayLabel.Today -> stringResource(Res.string.history_today)
                                HistoryDayLabel.Yesterday -> stringResource(Res.string.history_yesterday)
                                HistoryDayLabel.Date -> day.date
                            },
                            style = AppTheme.typography.titleSmall,
                            color = AppTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
                    }
                    items(day.rows, key = { it.key }) { row ->
                        HistorySongRow(row, onClick = { viewModel.play(row) })
                    }
                }
            }
        }
    }

    if (state.confirmingClear) {
        AdaptiveDialogBottomSheet(
            onDismiss = viewModel::cancelClear,
            dialogTitle = stringResource(Res.string.history_clear_title),
            title = {
                Text(stringResource(Res.string.history_clear_title), style = AppTheme.typography.titleMedium)
            },
            actions = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuietAction(stringResource(Res.string.common_cancel), viewModel::cancelClear)
                    MillenniumButton(onClick = viewModel::clear) {
                        Text(stringResource(Res.string.history_clear))
                    }
                }
            }) {
            Text(
                stringResource(Res.string.history_clear_message),
                style = AppTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun QuietAction(label: String, onClick: () -> Unit) {
    MillenniumButton(
        onClick = onClick,
        style = MillenniumButtonStyle.Quiet,
        foreground = AppTheme.colors.primary) {
        Text(label, style = AppTheme.typography.labelLarge)
    }
}

@Composable
private fun HistorySongRow(row: HistoryRow, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MediaArtwork(row.entry.artworkModel, row.title, Modifier.size(52.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                row.title,
                style = AppTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(
                row.artists,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
        if (row.playCount > 1) {
            Text(
                pluralStringResource(Res.plurals.history_play_count, row.playCount, row.playCount),
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.onSurfaceVariant)
        }
    }
}
