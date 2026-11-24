package com.mushi.app.modules.shell

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AppShellBottomInsetTest {
    private val viewModel = AppShellViewModel()
    private val chrome = CompactChromeUiState(measuredBottomBarHeight = 96.dp)

    @Test
    fun compactInsetIncludesMiniPlayerOnlyWhileSomethingIsQueued() {
        assertEquals(96.dp + viewModel.floatingPlayerHeight,
            viewModel.bottomOverlayInset(false, 24.dp, chrome, hasPlayerContent = true))
        assertEquals(96.dp,
            viewModel.bottomOverlayInset(false, 24.dp, chrome, hasPlayerContent = false))
    }

    @Test
    fun sidebarInsetDropsLargePlayerWhileNothingIsQueued() {
        assertEquals(viewModel.largePlayerInset + 24.dp,
            viewModel.bottomOverlayInset(true, 24.dp, chrome, hasPlayerContent = true))
        assertEquals(24.dp,
            viewModel.bottomOverlayInset(true, 24.dp, chrome, hasPlayerContent = false))
    }

    @Test
    fun withoutAQueueThePlayerIsShown() {
        assertEquals(true, viewModel.hasPlayerContent.value)
    }
}
