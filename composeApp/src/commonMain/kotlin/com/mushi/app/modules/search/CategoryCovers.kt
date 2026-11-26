package com.mushi.app.modules.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Cover images for the browse category tiles, found one category at a time as tiles come on
 * screen. A found cover is kept; a failed lookup is tried again the next time the tile shows.
 * Only [ParallelLookups] run at once, so opening Search doesn't flood the metadata plugin.
 */
class CategoryCovers(
    private val findCover: suspend (String) -> String?,
    private val scope: CoroutineScope
) {
    private val _covers = MutableStateFlow<Map<String, String>>(emptyMap())
    val covers: StateFlow<Map<String, String>> = _covers.asStateFlow()

    private val requested = mutableSetOf<String>()
    private val lookups = Semaphore(ParallelLookups)

    /** Call from the main thread. */
    fun load(category: String) {
        if (!requested.add(category)) return
        scope.launch {
            val cover = try {
                lookups.withPermit { findCover(category) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (cover == null) requested.remove(category)
            else _covers.update { it + (category to cover) }
        }
    }

    private companion object {
        const val ParallelLookups = 2
    }
}
