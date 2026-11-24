package com.mushi.app.modules.home

import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseGenre
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseItem
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseSection
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import com.mushi.app.modules.plugin.PluginManager
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenRepository(
    private val pluginManager: PluginManager
) {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val plugin
        get() = pluginManager.selectedMetadataPlugin.value

    private val featuredItemCache = Cache.Builder<String, List<MetadataBrowseItem>>().build()
    private val genresCache = Cache.Builder<String, List<MetadataBrowseGenre>>().build()
    private val browseItemCache =
        Cache.Builder<Pair<String, PaginationStrategy>, PaginationResult<MetadataBrowseSection>>()
            .build()
    private val sublistItemCache =
        Cache.Builder<Pair<String, PaginationStrategy>, PaginationResult<MetadataBrowseItem>>()
            .build()

    init {
        scope.launch {
            pluginManager.selectedMetadataPlugin
                .filterNotNull()
                .flatMapLatest { it.loggedInFlow }
                .distinctUntilChanged()
                .collect {
                    invalidateCaches()
                }
        }
    }

    fun invalidateCaches() {
        featuredItemCache.invalidateAll()
        genresCache.invalidateAll()
        browseItemCache.invalidateAll()
        sublistItemCache.invalidateAll()
    }

    suspend fun featuredItems() = plugin?.let { plugin ->
        featuredItemCache.get("featured_items") {
            pluginManager.withScope {
                plugin.use {
                    metadataBrowseAPI.featured()
                }
            }
        }
    }

    suspend fun genres() = plugin?.let { plugin ->
        genresCache.get("genres") {
            pluginManager.withScope {
                plugin.use {
                    metadataBrowseAPI.genres()
                }
            }
        }
    }

    suspend fun list(genreId: String, paginationStrategy: PaginationStrategy? = null) =
        plugin?.let { plugin ->
            browseItemCache.get(
                key = genreId to (paginationStrategy ?: PaginationStrategy.Offset(0, 20))
            ) {
                pluginManager.withScope {
                    plugin.use {
                        metadataBrowseAPI.list(genreId, paginationStrategy)
                    }
                }
            }
        }

    suspend fun sublist(
        genreId: String,
        parentId: String,
        paginationStrategy: PaginationStrategy? = null
    ) = plugin?.let { plugin ->
        sublistItemCache.get(
            "$genreId/$parentId" to (paginationStrategy ?: PaginationStrategy.Offset(
                0,
                20
            ))
        ) {
            pluginManager.withScope {
                plugin.use {
                    metadataBrowseAPI.sublist(genreId, parentId, paginationStrategy)
                }
            }
        }
    }
}