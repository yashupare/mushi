package com.mushi.app.modules.library.playlist

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.mushi.app.core.playlistimport.track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ImportPlaylistViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val created = mutableListOf<String>()
    private val batches = mutableListOf<List<String>>()
    private var failAdd = false
    private var searchDown = false

    private fun viewModel() = ImportPlaylistViewModel(
        search = { query ->
            if (searchDown) error("offline")
            if (query.startsWith("Missing")) emptyList()
            else listOf(track(query.removeSuffix(" Band"), "Band"))
        },
        createPlaylist = { name ->
            created += name
            "list-1"
        },
        addTracks = { _, ids ->
            if (failAdd && batches.size == 1) error("offline")
            batches += ids
        },
        defaultName = { "Imported playlist" })

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun ImportPlaylistViewModel.review() = assertIs<ImportUiState.Review>(uiState.value)

    @Test
    fun `lists what was found and what was not, and saves the ticked songs`() = runTest(dispatcher) {
        val model = viewModel()
        model.songsText.setTextAndPlaceCursorAtEnd("One - Band\nMissing - Band\nTwo - Band")
        model.findSongs()
        advanceUntilIdle()

        assertEquals(listOf("One · Band", "Two · Band"), model.review().found.map { it.wanted })
        assertEquals(listOf("Missing · Band"), model.review().missing.map { it.wanted })

        model.toggle(model.review().found.first().index)
        model.save()
        advanceUntilIdle()

        assertEquals(ImportUiState.Saved("list-1", "Imported playlist", 1), model.uiState.value)
        assertEquals(listOf(listOf("Two")), batches)
    }

    @Test
    fun `saving again carries on with the same playlist`() = runTest(dispatcher) {
        val model = viewModel()
        model.songsText.setTextAndPlaceCursorAtEnd((1..150).joinToString("\n") { "Song$it - Band" })
        model.findSongs()
        advanceUntilIdle()

        failAdd = true
        model.save()
        advanceUntilIdle()
        assertEquals(true, model.review().saveFailed)

        failAdd = false
        model.save()
        advanceUntilIdle()

        assertEquals(1, created.size)
        assertEquals(listOf(100, 50), batches.map { it.size })
        assertIs<ImportUiState.Saved>(model.uiState.value)
    }

    @Test
    fun `stops when search keeps failing, keeping the list`() = runTest(dispatcher) {
        val model = viewModel()
        searchDown = true
        model.songsText.setTextAndPlaceCursorAtEnd("A\nB\nC\nD")
        model.findSongs()
        advanceUntilIdle()

        assertEquals(ImportUiState.Input(ImportError.SearchFailed), model.uiState.value)
        assertEquals("A\nB\nC\nD", model.songsText.text.toString())
    }

    @Test
    fun `an empty list is reported`() = runTest(dispatcher) {
        val model = viewModel()
        model.findSongs()

        assertEquals(ImportUiState.Input(ImportError.NoSongs), model.uiState.value)
    }
}
