package com.mushi.app.modules.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseGenre
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseItem
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseSection
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import com.mushi.app.core.di.injectLogger
import com.mushi.app.modules.plugin.PluginManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import com.mushi.app.core.i18n.getString
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.common_unknown_error

sealed interface HomeScreenState {
    data object Loading : HomeScreenState

    sealed interface Data : HomeScreenState {
        val featuredItems: List<MetadataBrowseItem>
        val genres: List<MetadataBrowseGenre>
        val selectedGenreId: String?
        val browseSections: Map<String, List<MetadataBrowseSection>?>
        val paginationStrategies: Map<String, PaginationStrategy?>

        data class Loaded(
            override val featuredItems: List<MetadataBrowseItem>,
            override val genres: List<MetadataBrowseGenre>,
            override val selectedGenreId: String?,
            override val browseSections: Map<String, List<MetadataBrowseSection>?>,
            override val paginationStrategies: Map<String, PaginationStrategy?>) : Data {
            fun toLoadingMore(): LoadingMore = LoadingMore(
                featuredItems = featuredItems,
                genres = genres,
                selectedGenreId = selectedGenreId,
                browseSections = browseSections,
                paginationStrategies = paginationStrategies)
        }

        data class LoadingMore(
            override val featuredItems: List<MetadataBrowseItem>,
            override val genres: List<MetadataBrowseGenre>,
            override val selectedGenreId: String?,
            override val browseSections: Map<String, List<MetadataBrowseSection>?>,
            override val paginationStrategies: Map<String, PaginationStrategy?>) : Data
    }

    data class Error(val message: String) : HomeScreenState
}

private const val MIN_SHELF_ITEMS = 3

/** Sections for the selected genre, or null while they're still loading. */
internal val HomeScreenState.Data.currentSections: List<MetadataBrowseSection>?
    get() = selectedGenreId?.let { browseSections[it] }

/** The top grid: up to 8 tiles from the first section, shown only when there are at least 2. */
internal val HomeScreenState.Data.quickPicks: List<QuickPick>
    get() = currentSections?.firstOrNull()?.items
        ?.mapNotNull { it.asQuickPick() }
        ?.take(8)
        ?.takeIf { it.size >= 2 }
        .orEmpty()

/**
 * Shelves to render below the grid. Items already in the grid are removed from the first
 * shelf, then any shelf with fewer than [MIN_SHELF_ITEMS] items is dropped so the page
 * doesn't show sparse one- or two-card rows.
 */
internal val HomeScreenState.Data.shelves: List<MetadataBrowseSection>?
    get() {
        val sections = currentSections ?: return null
        val shown = quickPicks.map { it.item }.toSet()
        return sections
            .mapIndexed { index, section ->
                if (index == 0 && shown.isNotEmpty()) {
                    section.copy(items = section.items.filterNot { it in shown })
                } else {
                    section
                }
            }
            .filter { it.items.size >= MIN_SHELF_ITEMS }
    }

// A shelf whose items the app can't show (e.g. podcast episodes) arrives empty; drop it
// rather than render a bare heading.
private fun List<MetadataBrowseSection>?.nonEmptySections(): List<MetadataBrowseSection> =
    this?.filter { it.items.isNotEmpty() } ?: emptyList()

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    private val repository: HomeScreenRepository, private val pluginManager: PluginManager
) : ViewModel(), KoinComponent {
    private val logger by injectLogger<HomeScreenViewModel>()

    private val state = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val uiState = state.asStateFlow()

    init {
        viewModelScope.launch {
            pluginManager.selectedMetadataPlugin
                .filterNotNull()
                .flatMapLatest { it.loggedInFlow }
                .distinctUntilChanged().collect {
                    loadInitialData()
                }
        }
    }

    private suspend fun loadInitialData() = runCatching {
        state.value = HomeScreenState.Loading
        val featuredItems = repository.featuredItems() ?: emptyList()
        val genres = repository.genres() ?: emptyList()
        val firstGenreId = genres.firstOrNull()?.id
        val browseSections = mutableMapOf<String, List<MetadataBrowseSection>?>()
        val paginationStrategies = mutableMapOf<String, PaginationStrategy?>()
        if (firstGenreId != null) {
            val result = repository.list(firstGenreId)
            browseSections[firstGenreId] = result?.items.nonEmptySections()
            paginationStrategies[firstGenreId] = result?.nextPagination
        }
        state.value = HomeScreenState.Data.Loaded(
            featuredItems = featuredItems,
            genres = genres,
            selectedGenreId = firstGenreId,
            browseSections = browseSections,
            paginationStrategies = paginationStrategies)
    }.onFailure { e ->
        logger.e(e) { "Failed to load home screen data" }
        state.value = HomeScreenState.Error(e.message ?: getString(Res.string.common_unknown_error))
    }

    fun selectGenre(genreId: String) {
        val currentState = state.value
        if (currentState is HomeScreenState.Data) {
            state.value = when (currentState) {
                is HomeScreenState.Data.Loaded -> currentState.copy(selectedGenreId = genreId)
                is HomeScreenState.Data.LoadingMore -> currentState.copy(selectedGenreId = genreId)
            }
            if (currentState.browseSections[genreId] == null) {
                viewModelScope.launch {
                    val result = repository.list(genreId)
                    val current = state.value
                    if (current is HomeScreenState.Data) {
                        state.value = HomeScreenState.Data.Loaded(
                            featuredItems = current.featuredItems,
                            genres = current.genres,
                            selectedGenreId = genreId,
                            browseSections = current.browseSections + (genreId to result?.items.nonEmptySections()),
                            paginationStrategies = current.paginationStrategies + (genreId to result?.nextPagination))
                    }
                }
            }
        }
    }

    suspend fun loadMoreData() = runCatching {
        val currentState = state.value
        if (currentState is HomeScreenState.Data.Loaded) {
            val genreId = currentState.selectedGenreId ?: return@runCatching
            val pagination = currentState.paginationStrategies[genreId] ?: return@runCatching
            state.value = currentState.toLoadingMore()
            val result = repository.list(genreId, pagination)
            val existingSections = currentState.browseSections[genreId] ?: emptyList()
            state.value = HomeScreenState.Data.Loaded(
                featuredItems = currentState.featuredItems,
                genres = currentState.genres,
                selectedGenreId = genreId,
                browseSections = currentState.browseSections + (genreId to (existingSections + result?.items.nonEmptySections())),
                paginationStrategies = currentState.paginationStrategies + (genreId to result?.nextPagination))
        }
    }.onFailure { e ->
        logger.e(e) { "Failed to load more home screen data" }
        state.value = HomeScreenState.Error(e.message ?: getString(Res.string.common_unknown_error))
    }

    fun refresh() {
        viewModelScope.launch {
            repository.invalidateCaches()
            loadInitialData()
        }
    }
}
