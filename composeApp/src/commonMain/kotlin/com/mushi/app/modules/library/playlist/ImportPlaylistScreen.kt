package com.mushi.app.modules.library.playlist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import com.mushi.app.core.ui.component.ApplicationMainBar
import com.mushi.app.core.ui.component.cards.MediaArtwork
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumProgress
import org.spotube.millennium.composables.MillenniumTextField

@Composable
fun ImportPlaylistScreen(viewModel: ImportPlaylistViewModel, onOpenPlaylist: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ApplicationMainBar(title = { Text(stringResource(Res.string.import_title)) })
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            // Room for the mini player, or for the keyboard where that is taller, not both
            val inset = LocalAppShellBottomInset.current
            Column(
                Modifier.widthIn(max = 720.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = inset)
                    .consumeWindowInsets(PaddingValues(bottom = inset))
                    .imePadding()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (val current = state) {
                    is ImportUiState.Input -> InputStep(current, viewModel)
                    is ImportUiState.Matching -> MatchingStep(current, viewModel::backToList)
                    is ImportUiState.Review -> ReviewStep(current, viewModel)
                    is ImportUiState.Saved -> SavedStep(
                        current,
                        onOpen = {
                            viewModel.startOver()
                            onOpenPlaylist(current.playlistId)
                        },
                        onAnother = viewModel::startOver)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.InputStep(state: ImportUiState.Input, viewModel: ImportPlaylistViewModel) {
    val picker = rememberFilePickerLauncher(type = FileKitType.File("csv", "txt", "m3u", "m3u8")) { file ->
        file?.let(viewModel::onFilePicked)
    }

    Hint(stringResource(Res.string.import_hint))
    SongListField(viewModel.songsText, Modifier.weight(1f))
    state.error?.let {
        Text(
            stringResource(
                when (it) {
                    ImportError.NoSongs -> Res.string.import_error_no_songs
                    ImportError.BadFile -> Res.string.import_error_file
                    ImportError.SearchFailed -> Res.string.import_error_search
                }),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.error)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        QuietButton(stringResource(Res.string.import_choose_file), onClick = { picker.launch() })
        MillenniumButton(onClick = viewModel::findSongs) {
            Text(stringResource(Res.string.import_find))
        }
    }
}

@Composable
private fun SongListField(state: TextFieldState, modifier: Modifier) {
    val style = AppTheme.typography.bodyMedium
    BasicTextField(
        state = state,
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.06f))
            .padding(12.dp),
        textStyle = style.copy(color = AppTheme.colors.onSurface),
        cursorBrush = SolidColor(AppTheme.colors.primary),
        lineLimits = TextFieldLineLimits.MultiLine(),
        decorator = { field ->
            Box {
                if (state.text.isEmpty()) {
                    Text(
                        stringResource(Res.string.import_placeholder),
                        style = style,
                        color = AppTheme.colors.onSurfaceVariant)
                }
                field()
            }
        })
}

@Composable
private fun MatchingStep(state: ImportUiState.Matching, onCancel: () -> Unit) {
    Text(
        stringResource(Res.string.import_matching, state.done, state.total),
        style = AppTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 24.dp))
    MillenniumProgress(
        progress = state.done / state.total.toFloat(),
        modifier = Modifier.fillMaxWidth().height(4.dp))
    QuietButton(stringResource(Res.string.common_cancel), onClick = onCancel)
}

@Composable
private fun ColumnScope.ReviewStep(state: ImportUiState.Review, viewModel: ImportPlaylistViewModel) {
    val total = state.found.size + state.missing.size
    MillenniumTextField(
        state = viewModel.nameText,
        label = stringResource(Res.string.library_playlist_form_name_label),
        modifier = Modifier.fillMaxWidth(),
        placeholder = stringResource(Res.string.import_default_name))
    Hint(stringResource(Res.string.import_found_count, state.found.size, total))
    if (state.truncated) Hint(stringResource(Res.string.import_truncated, IMPORT_MAX_SONGS))

    LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
        items(state.found, key = { it.index }) { row ->
            FoundRow(row, onToggle = { viewModel.toggle(row.index) })
        }
        if (state.missing.isNotEmpty()) {
            item(key = "missing") {
                Text(
                    stringResource(Res.string.import_not_found),
                    style = AppTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            }
            items(state.missing, key = { it.index }) { row ->
                Text(
                    row.wanted,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }

    if (state.saveFailed) {
        Text(
            stringResource(Res.string.import_error_save),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.error)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        QuietButton(stringResource(Res.string.common_back), onClick = viewModel::backToList)
        MillenniumButton(onClick = viewModel::save, enabled = !state.saving && state.selected > 0) {
            Text(
                if (state.saving) stringResource(Res.string.import_saving)
                else pluralStringResource(Res.plurals.import_save, state.selected, state.selected))
        }
    }
}

@Composable
private fun FoundRow(row: ImportRow, onToggle: () -> Unit) {
    val track = row.match ?: return
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = row.included, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MediaArtwork((track.album?.thumbnails ?: track.thumbnails)?.firstOrNull()?.url, track.title, Modifier.size(48.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(track.title, style = AppTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                track.artists.joinToString { it.name },
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            // What the list asked for, so a wrong match is easy to spot
            Text(
                stringResource(Res.string.import_asked_for, row.wanted),
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
        CheckMark(row.included)
    }
}

@Composable
private fun CheckMark(checked: Boolean) {
    val fill = AppTheme.colors.primary
    val tick = AppTheme.colors.onPrimary
    Box(
        Modifier.size(22.dp)
            .clip(CircleShape)
            .then(
                if (checked) Modifier.background(fill)
                else Modifier.border(2.dp, AppTheme.colors.onSurfaceVariant, CircleShape))) {
        if (checked) Canvas(Modifier.fillMaxSize()) {
            val width = 2.dp.toPx()
            val corner = Offset(size.width * 0.43f, size.height * 0.67f)
            drawLine(tick, Offset(size.width * 0.27f, size.height * 0.51f), corner, width, StrokeCap.Round)
            drawLine(tick, corner, Offset(size.width * 0.74f, size.height * 0.35f), width, StrokeCap.Round)
        }
    }
}

@Composable
private fun SavedStep(state: ImportUiState.Saved, onOpen: () -> Unit, onAnother: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            pluralStringResource(Res.plurals.import_saved, state.count, state.name, state.count),
            style = AppTheme.typography.titleMedium,
            textAlign = TextAlign.Center)
        MillenniumButton(onClick = onOpen) {
            Text(stringResource(Res.string.import_open))
        }
        QuietButton(stringResource(Res.string.import_another), onClick = onAnother)
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, style = AppTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
}

@Composable
private fun QuietButton(label: String, onClick: () -> Unit) {
    MillenniumButton(
        onClick = onClick,
        style = MillenniumButtonStyle.Quiet,
        foreground = AppTheme.colors.primary) {
        Text(label, style = AppTheme.typography.labelLarge)
    }
}
