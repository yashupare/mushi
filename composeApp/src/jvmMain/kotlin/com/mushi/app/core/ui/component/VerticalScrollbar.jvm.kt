package com.mushi.app.core.ui.component

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.theming.AppTheme

@Composable
actual fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier
) {
    val colors = AppTheme.colors
    CompositionLocalProvider(
        LocalScrollbarStyle provides ScrollbarStyle(
            minimalHeight = 16.dp,
            thickness = 8.dp,
            shape = RoundedCornerShape(4.dp),
            hoverDurationMillis = 300,
            unhoverColor = colors.onSurface.copy(alpha = 0.18f),
            hoverColor = colors.onSurface.copy(alpha = 0.4f))) {
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = modifier.fillMaxHeight()
                .padding(end = 2.dp)
                .padding(vertical = 16.dp)
                .width(12.dp))
    }
}