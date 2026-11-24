package com.mushi.app.core.ui.component

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.mushi.app.getPlatform
import com.mushi.app.isDesktop

@Composable
expect fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier)

fun Modifier.dragScrollable(rowState: LazyListState): Modifier =
    if (getPlatform().isDesktop()) pointerInput(rowState) {
        detectDragGestures { change, dragAmount ->
            // Invert drag direction so content follows direct-manipulation behavior.
            rowState.dispatchRawDelta(-dragAmount.x)
        }
    } else Modifier
