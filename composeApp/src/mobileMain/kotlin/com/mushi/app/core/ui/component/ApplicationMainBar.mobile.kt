package com.mushi.app.core.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import org.spotube.millennium.composables.MillenniumTopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

@Composable
actual fun ApplicationMainBar(
    title: @Composable (() -> Unit),
    subtitle: @Composable () -> Unit,
    actions: @Composable (RowScope.() -> Unit),
    backButton: Boolean,
    transparent: Boolean) {
    return MillenniumTopAppBar(
        title = title,
        subtitle = subtitle,
        actions = actions,
        windowInsets = WindowInsets.statusBars,
        navigationIcon = {
            if (backButton) {
                ApplicationBackButton()
            }
        }
    )
}
