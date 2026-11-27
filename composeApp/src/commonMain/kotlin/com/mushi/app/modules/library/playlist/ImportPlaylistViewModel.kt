package com.mushi.app.modules.library.playlist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mushi.app.core.playlistimport.ImportedSong
import com.mushi.app.core.playlistimport.bestMatch
import com.mushi.app.core.playlistimport.parseSongList
import com.mushi.app.core.playlistimport.searchQuery
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

internal const val IMPORT_MAX_SONGS = 500
private const val IMPORT_MAX_FILE_BYTES = 2_000_000L
private const val SEARCH_PAUSE_MS = 150L
private const val SEARCH_RETRY_MS = 2_000L
private const val FAILURES_BEFORE_STOPPING = 3
private const val ADD_BATCH = 100

enum class ImportError { NoSongs, BadFile, SearchFailed }

data class ImportRow(
    val index: Int,
    val wanted: String,
    val match: MetadataTrack?,
    val included: Boolean = match != null)

sealed interface ImportUiState {
    data class Input(val error: ImportError? = null) : ImportUiState
    data class Matching(val done: Int, val total: Int) : ImportUiState
    data class Review(
        val found: List<ImportRow>,
        val missing: List<ImportRow>,
        /** The list was longer than [IMPORT_MAX_SONGS] and the rest was left out. */
        val truncated: Boolean = false,
        val saving: Boolean = false,
        val saveFailed: Boolean = false) : ImportUiState {
        val selected: Int get() = found.count { it.included }
    }

    data class Saved(val playlistId: String, val name: String, val count: Int) : ImportUiState
}

/** Turns a pasted list or a file of song names into a playlist. */
class ImportPlaylistViewModel(
    private val search: suspend (String) -> List<MetadataTrack>,
    private val createPlaylist: suspend (String) -> String?,
    private val addTracks: suspend (String, List<String>) -> Unit,
    private val defaultName: suspend () -> String) : ViewModel() {

    private val logger = Logger.withTag("PlaylistImport")

    val songsText = TextFieldState()
    val nameText = TextFieldState()

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Input())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private var matching: Job? = null

    // Kept so that saving again after a failure carries on instead of making a second playlist
    private var createdId: String? = null
    private var added = 0

    fun onFilePicked(file: PlatformFile) {
        viewModelScope.launch {
            try {
                require(file.size() <= IMPORT_MAX_FILE_BYTES) { "File too large" }
                songsText.setTextAndPlaceCursorAtEnd(file.readString())
                nameText.setTextAndPlaceCursorAtEnd(file.name.substringBeforeLast('.'))
                _uiState.value = ImportUiState.Input()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Failed to read the file" }
                _uiState.value = ImportUiState.Input(ImportError.BadFile)
            }
        }
    }

    fun findSongs() {
        if (matching?.isActive == true) return
        val all = parseSongList(songsText.text.toString())
        if (all.isEmpty()) {
            _uiState.value = ImportUiState.Input(ImportError.NoSongs)
            return
        }
        val songs = all.take(IMPORT_MAX_SONGS)
        createdId = null
        added = 0
        _uiState.value = ImportUiState.Matching(0, songs.size)

        matching = viewModelScope.launch {
            val rows = mutableListOf<ImportRow>()
            var failures = 0
            for ((index, song) in songs.withIndex()) {
                val results = searchWithRetry(searchQuery(song))
                if (results == null) {
                    // One song the search chokes on is only "not found"; several in a row means
                    // the connection or the service is the problem
                    if (++failures >= FAILURES_BEFORE_STOPPING) {
                        _uiState.value = ImportUiState.Input(ImportError.SearchFailed)
                        return@launch
                    }
                } else {
                    failures = 0
                }
                rows += ImportRow(index, song.label(), results?.let { bestMatch(song, it) })
                _uiState.value = ImportUiState.Matching(rows.size, songs.size)
                delay(SEARCH_PAUSE_MS)
            }
            _uiState.value = ImportUiState.Review(
                found = rows.filter { it.match != null },
                missing = rows.filter { it.match == null },
                truncated = all.size > songs.size)
        }
    }

    private suspend fun searchWithRetry(query: String): List<MetadataTrack>? {
        repeat(2) { attempt ->
            try {
                return search(query)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.w(e) { "Search failed" }
                if (attempt == 0) delay(SEARCH_RETRY_MS)
            }
        }
        return null
    }

    private fun ImportedSong.label(): String = if (artist == null) title else "$title · $artist"

    /** Stops matching, or leaves the results, and goes back to the list. */
    fun backToList() {
        matching?.cancel()
        matching = null
        _uiState.value = ImportUiState.Input()
    }

    fun toggle(index: Int) {
        _uiState.update { state ->
            if (state !is ImportUiState.Review || state.saving) state
            else state.copy(
                found = state.found.map { if (it.index == index) it.copy(included = !it.included) else it })
        }
    }

    fun save() {
        val review = _uiState.value as? ImportUiState.Review ?: return
        if (review.saving || review.selected == 0) return
        val ids = review.found.filter { it.included }.mapNotNull { it.match?.id }.distinct()
        _uiState.value = review.copy(saving = true, saveFailed = false)

        viewModelScope.launch {
            try {
                val name = nameText.text.toString().trim().ifEmpty { defaultName() }
                val id = createdId ?: checkNotNull(createPlaylist(name)) { "Not signed in" }
                createdId = id
                while (added < ids.size) {
                    val batch = ids.subList(added, minOf(added + ADD_BATCH, ids.size))
                    addTracks(id, batch)
                    added += batch.size
                }
                songsText.setTextAndPlaceCursorAtEnd("")
                nameText.setTextAndPlaceCursorAtEnd("")
                createdId = null
                added = 0
                _uiState.value = ImportUiState.Saved(id, name, ids.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Failed to save the imported playlist" }
                _uiState.value = review.copy(saving = false, saveFailed = true)
            }
        }
    }

    fun startOver() {
        _uiState.value = ImportUiState.Input()
    }
}
