package com.mushi.app.modules.library.local_tracks

import com.mushi.app.core.ui.listItemMotion
import com.mushi.app.core.ui.component.MushiButton
import com.mushi.app.core.ui.misc.AppSpinner
import org.spotube.millennium.composables.MillenniumSpokeSpinner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import com.mushi.app.core.ui.component.cards.MediaArtwork
import com.mushi.app.core.ui.component.cards.MediaEmptyState
import com.mushi.app.core.ui.component.cards.PlayableListTile
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.modules.library.local_tracks.media.rememberLocalMediaPermissionState
import com.mushi.app.modules.settings.LocalMediaFoldersViewModel
import com.mushi.app.modules.settings.components.LocalMediaFoldersDialogHost
import com.mushi.app.resources.iconsax.IconsaxFolderAdd
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxFolderOpen
import com.mushi.app.resources.iconsax.IconsaxPlay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumListTile
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import com.mushi.app.generated.resources.*

@Composable
fun LibraryLocalTracksScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryLocalTracksViewModel = koinViewModel(),
    foldersViewModel: LocalMediaFoldersViewModel = koinViewModel(),
    navigationCommands: NavigationCommands = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberLocalMediaPermissionState()
    // Platform permission events only; the ViewModel decides whether/when to index media.
    LaunchedEffect(permission.isGranted) { viewModel.onPermissionChanged(permission.isGranted) }
    LocalTracksContent(
        state,
        onOpenFolder = { navigationCommands.navigateTo(Routes.LocalFolder(it)) },
        onPlayTrack = {}, onNavigateUp = {}, onPlayFolder = {},
        onRequestPermission = permission.requestPermission, modifier = modifier,
        onManageFolders = foldersViewModel::open
    )
    LocalMediaFoldersDialogHost(foldersViewModel)
}

@Composable
internal fun LocalTracksContent(
    state: LibraryLocalTracksState,
    onOpenFolder: (String) -> Unit,
    onPlayTrack: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onPlayFolder: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    onManageFolders: () -> Unit = {}
) {
    val inset = LocalAppShellBottomInset.current
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 960.dp).fillMaxSize()
        ) {
            LocalTracksBreadcrumb(state, onNavigateUp, onPlayFolder, onManageFolders = onManageFolders)
            when {
                !state.canAccessMedia -> MediaEmptyState(
                    com.mushi.app.core.ui.theming.MushiIcons.Folder,
                    stringResource(Res.string.library_local_permission_title),
                    Modifier.padding(bottom = inset),
                    stringResource(Res.string.library_local_permission_description),
                    action = {
                        MushiButton(onClick = onRequestPermission) {
                            Text(
                                stringResource(
                                    Res.string.library_local_grant_access
                                )
                            )
                        }
                    })

                state.isRefreshing && state.visibleFolders.isEmpty() && state.visibleTracks.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(bottom = inset),
                    contentAlignment = Alignment.Center
                ) {
                    AppSpinner(size = 32.dp)
                }

                state.currentFolderPath == null && state.visibleFolders.isNotEmpty() -> LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = inset + 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.visibleFolders, key = { it.path }) { folder ->
                        Box(listItemMotion()) { LocalFolderRow(folder) { onOpenFolder(folder.path) } }
                    }
                }

                state.currentFolderPath != null && state.visibleTracks.isNotEmpty() -> LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = inset + 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.visibleTracks, key = { it.path }) { track ->
                        Box(listItemMotion()) {
                        PlayableListTile(
                            track.title,
                            track.subtitle ?: stringResource(Res.string.media_unknown_artist),
                            imageModel = track.artwork,
                            // The row itself plays; a separate play button on every row is just noise.
                            onClick = { onPlayTrack(track.path) },
                            compact = true,
                            trailingContent = {
                                track.durationLabel?.let {
                                    Text(
                                        it,
                                        style = AppTheme.typography.labelSmall,
                                        color = AppTheme.colors.onSurfaceVariant
                                    )
                                }
                            })
                        }
                    }
                }

                else -> {
                    val noFolders = state.currentFolderPath == null && state.query.isBlank()
                    MediaEmptyState(
                        com.mushi.app.core.ui.theming.MushiIcons.Folder,
                        state.error ?: stringResource(
                            when {
                                state.query.isNotBlank() -> Res.string.library_local_no_results
                                state.currentFolderPath == null -> Res.string.library_local_empty_folders
                                else -> Res.string.library_local_empty_tracks
                            }
                        ), Modifier.padding(bottom = inset),
                        description = if (noFolders && state.error == null)
                            stringResource(Res.string.library_local_empty_folders_description) else null,
                        action = if (noFolders) ({
                            MushiButton(onClick = onManageFolders) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MillenniumIcon(
                                        com.mushi.app.core.ui.theming.MushiIcons.FolderAdd, null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(stringResource(Res.string.library_local_add_folder))
                                }
                            }
                        }) else null
                    )
                }
            }
        }
    }
}

@Composable
internal fun LocalTracksBreadcrumb(
    state: LibraryLocalTracksState,
    onNavigateUp: () -> Unit,
    onPlayFolder: () -> Unit,
    modifier: Modifier = Modifier,
    onManageFolders: (() -> Unit)? = null
) {
    val backDescription = stringResource(Res.string.library_local_back)
    Row(
        modifier.fillMaxWidth().height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MillenniumIcon(
                com.mushi.app.core.ui.theming.MushiIcons.Folder,
                null,
                modifier = Modifier.size(14.dp),
                tint = AppTheme.colors.onSurfaceVariant
            )
            if (state.currentFolderPath != null) {
                MillenniumButton(
                    onClick = onNavigateUp,
                    style = MillenniumButtonStyle.Quiet,
                    modifier = Modifier.height(28.dp)
                        .semantics { contentDescription = backDescription },
                    padding = PaddingValues(0.dp),
                    minSize = DpSize(0.dp, 28.dp),
                    foreground = AppTheme.colors.primary
                ) {
                    Text(
                        stringResource(Res.string.library_local_folders),
                        style = AppTheme.typography.labelSmall
                    )
                }
                MillenniumIcon(
                    FeatherIcons.ChevronRight,
                    null,
                    modifier = Modifier.size(12.dp),
                    tint = AppTheme.colors.onSurfaceVariant
                )
                Text(
                    state.currentFolder?.name ?: stringResource(Res.string.library_local_folder),
                    style = AppTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            } else Text(
                stringResource(Res.string.library_local_folders),
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.onSurfaceVariant
            )
        }
        Text(
            if (state.currentFolderPath != null) pluralStringResource(
                Res.plurals.library_local_tracks_count,
                state.visibleTracks.size,
                state.visibleTracks.size
            )
            else stringResource(
                Res.string.library_local_summary,
                pluralStringResource(
                    Res.plurals.library_local_folders_count,
                    state.totalFolderCount,
                    state.totalFolderCount
                ),
                pluralStringResource(
                    Res.plurals.library_local_tracks_count,
                    state.totalTrackCount,
                    state.totalTrackCount
                )
            ),
            style = AppTheme.typography.labelSmall, color = AppTheme.colors.onSurfaceVariant,
            maxLines = 1
        )
        if (state.isRefreshing) com.mushi.app.core.ui.component.MushiLoader(size = 14.dp)
        if (state.currentFolderPath == null && onManageFolders != null) MillenniumIconButton(
            onClick = onManageFolders,
            modifier = Modifier.size(40.dp), style = MillenniumIconButtonStyle.Quiet
        ) {
            MillenniumIcon(
                com.mushi.app.core.ui.theming.MushiIcons.FolderAdd,
                stringResource(Res.string.library_local_add_folder),
                modifier = Modifier.size(22.dp)
            )
        }
        if (state.currentFolderPath != null) MillenniumIconButton(
            onClick = onPlayFolder, enabled = state.canPlayFolder,
            modifier = Modifier.size(40.dp), style = MillenniumIconButtonStyle.Quiet
        ) {
            MillenniumIcon(
                com.mushi.app.core.ui.theming.MushiIcons.Play,
                stringResource(Res.string.library_local_play_folder),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
internal fun LocalFolderRow(folder: LocalFolderUi, onClick: () -> Unit) {
    MillenniumListTile(
        onClick = onClick,
        background = null,
        shape = RoundedCornerShape(12.dp),
        borderColor = null,
        borderWidth = 0.dp,
        contentPadding = PaddingValues(8.dp),
        minimumHeight = 52.dp,
        leading = {
            MediaArtwork(
                folder.artwork,
                folder.name,
                Modifier.size(36.dp),
                placeholderIcon = com.mushi.app.core.ui.theming.MushiIcons.Folder
            )
        },
        title = {
            Text(
                folder.name,
                style = AppTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = {
            Text(
                pluralStringResource(
                    Res.plurals.library_local_tracks_count,
                    folder.trackCount,
                    folder.trackCount
                ), style = AppTheme.typography.bodySmall
            )
        },
        trailing = {
            MillenniumIcon(
                FeatherIcons.ChevronRight,
                null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}
