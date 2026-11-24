package com.mushi.app.modules.library.artist

import com.mushi.app.resources.iconsax.User
import com.mushi.app.resources.iconsax.IconsaxSearch
import com.mushi.app.resources.iconsax.Iconsax
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import com.mushi.app.core.ui.component.ArtistCard
import com.mushi.app.core.ui.component.ErrorDisplay
import com.mushi.app.core.ui.component.cards.AvatarCard
import com.mushi.app.modules.library.LibraryMediaCollection
import com.mushi.app.modules.library.LibraryViewMode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.mushi.app.generated.resources.*

@Composable
fun LibraryArtistsScreen(
    modifier: Modifier = Modifier,
    viewMode: LibraryViewMode = LibraryViewMode.Grid,
    viewModel: LibraryArtistsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val current = state) {
        is LibraryArtistsState.Error -> Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ErrorDisplay(errorMessage = current.message, onRetry = viewModel::refresh)
        }

        else -> {
            val data = current as? LibraryArtistsState.Data
            if (data != null && data.items.isEmpty() && current !is LibraryArtistsState.Data.LoadingMore) {
                com.mushi.app.core.ui.component.cards.MediaEmptyState(
                    icon = if (data.query.isNotBlank()) com.mushi.app.core.ui.theming.MushiIcons.Search
                        else com.mushi.app.core.ui.theming.MushiIcons.User,
                    title = if (data.query.isNotBlank()) stringResource(Res.string.library_artists_no_results, data.query)
                        else stringResource(Res.string.library_artists_empty),
                    description = if (data.query.isBlank()) stringResource(Res.string.library_artists_empty_hint) else null)
            } else LibraryMediaCollection<MetadataArtist.Basic>(
                items = data?.items.orEmpty(), itemKey = { it.id }, viewMode = viewMode,
                loading = current is LibraryArtistsState.Loading,
                loadingMore = current is LibraryArtistsState.Data.LoadingMore,
                onViewportChanged = viewModel::onItemsVisible,
                itemContent = { item, list ->
                    ArtistCard(
                        item,
                        Modifier.fillMaxWidth(),
                        listMode = list
                    )
                },
                skeleton = { list ->
                    AvatarCard(
                        stringResource(Res.string.media_preview_subtitle),
                        stringResource(Res.string.media_artist),
                        modifier = Modifier.fillMaxWidth(),
                        listMode = list
                    )
                },
                modifier = modifier
            )
        }
    }
}
