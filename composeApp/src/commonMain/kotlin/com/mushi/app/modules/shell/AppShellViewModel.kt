package com.mushi.app.modules.shell


import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.min

data class CompactSheetUiState(
    val progress: Float,
    val showExpandedPlayer: Boolean,
    val expandedPlayerAlpha: Float,
    val floatingPlayerAlpha: Float,
    val bottomBarAlpha: Float
)

data class LyricsOverlayState(
    val isVisible: Boolean = false
)

data class CompactChromeUiState(val measuredBottomBarHeight: Dp? = null)

/**
 * @param audioPlayerQueue hides the mini player while nothing is queued. Without one (tests),
 *   the player is always shown.
 */
class AppShellViewModel(audioPlayerQueue: AudioPlayerQueue? = null) : ViewModel() {
    /** False while the queue is empty: the mini player and its bottom inset are hidden then. */
    val hasPlayerContent: StateFlow<Boolean> =
        audioPlayerQueue?.queueFlow
            ?.map { it.isNotEmpty() }
            ?.stateIn(viewModelScope, SharingStarted.Eagerly, audioPlayerQueue.queueFlow.value.isNotEmpty())
            ?: MutableStateFlow(true)

    val sidebarMinWidth = 840.dp
    val bottomBarHeight = 80.dp
    // The background bleed overlaps the bottom bar and is not part of the peek height.
    val floatingPlayerHeight = FloatingPlayerContentHeight
    val largePlayerInset = 100.dp
    val floatingPlayerDismissProgress = 0.7f
    val expandedPlayerVisibilityThreshold = 0.001f

    private val lyricsOverlayVisible = MutableStateFlow(false)
    private val compactChrome = MutableStateFlow(CompactChromeUiState())
    val compactChromeUiState = compactChrome.asStateFlow()

    fun onBottomBarMeasured(height: Dp) {
        if (height > 0.dp) compactChrome.value = CompactChromeUiState(height)
    }

    val isLyricsOverlayVisible: StateFlow<Boolean> = lyricsOverlayVisible.asStateFlow()

    @Composable
    fun useSidebar(maxWidth: Dp? = null): Boolean {
        if (maxWidth != null) {
            return maxWidth >= sidebarMinWidth
        }
        return currentWindowWidthBreakpoint() isAtLeast WindowBreakpoints.Expanded
    }

    fun bottomOverlayInset(
        useSidebar: Boolean,
        navigationBarInset: Dp,
        chrome: CompactChromeUiState,
        hasPlayerContent: Boolean = true
    ): Dp =
        if (useSidebar) (if (hasPlayerContent) largePlayerInset else 0.dp) + navigationBarInset
        else (chrome.measuredBottomBarHeight ?: (bottomBarHeight + navigationBarInset)) +
                (if (hasPlayerContent) floatingPlayerHeight else 0.dp)

    fun sheetProgress(sheetOffsetPx: Float, sheetHeightPx: Float): Float =
        if (sheetHeightPx > 0f) {
            (sheetOffsetPx / sheetHeightPx).coerceIn(0f, 1f)
        } else {
            0f
        }

    fun floatingPlayerAlpha(progress: Float): Float {
        val normalized = min(progress / floatingPlayerDismissProgress, 1f)
        return 1f - normalized
    }

    fun compactSheetUiState(progress: Float): CompactSheetUiState {
        return CompactSheetUiState(
            progress = progress,
            showExpandedPlayer = progress > expandedPlayerVisibilityThreshold,
            expandedPlayerAlpha = progress,
            floatingPlayerAlpha = floatingPlayerAlpha(progress),
            bottomBarAlpha = 1f - progress
        )
    }

    fun showLyricsOverlay() {
        lyricsOverlayVisible.value = true
    }

    fun hideLyricsOverlay() {
        lyricsOverlayVisible.value = false
    }

    fun toggleLyricsOverlay() {
        lyricsOverlayVisible.update { !it }
    }
}
