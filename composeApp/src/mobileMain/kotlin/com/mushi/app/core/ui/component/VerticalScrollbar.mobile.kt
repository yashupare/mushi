package com.mushi.app.core.ui.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier
) {
    // No-op on mobile targets.
}