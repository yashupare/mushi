package com.mushi.app.core.ui.coverflow

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberCoverflowState(
    initialIndex: Int = 0,
    onSelectHandler: (Int) -> Unit = {}): CoverflowState {
    val lazyListState: LazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex + 1)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    return remember {
        CoverflowState(
            lazyListState,
            coroutineScope,
            onSelectHandler,
            initialIndex)
    }
}

class CoverflowState internal constructor(
    internal val lazyListState: LazyListState,
    private val coroutineScope: CoroutineScope,
    private val onSelectHandler: (Int) -> Unit = {},
    initialIndex: Int = 0) {
    internal var geometry: Geometry? = null
    internal var initialScrollDone: Boolean = false

    var selectedIndex: Int = initialIndex
        internal set(it) {
            field = it
            onSelectHandler(it)
        }

    fun scrollToItem(index: Int) {
        val geometryCopy = geometry
        if (geometryCopy != null) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(
                    index + 1,
                    -geometryCopy.spacerWidth)
            }
        }
    }
}
