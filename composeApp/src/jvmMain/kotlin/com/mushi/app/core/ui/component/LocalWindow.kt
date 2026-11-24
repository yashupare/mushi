package com.mushi.app.core.ui.component

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.window.WindowState
import dev.nucleusframework.application.NucleusDecoratedWindowScope

// LocalWindow is a LocalComposition that provides access to the current Window instance in the composition hierarchy.
val LocalWindowScope = compositionLocalOf<NucleusDecoratedWindowScope> {
    error("No Window found in LocalWindowScope")
}
val LocalWindowState = compositionLocalOf<WindowState> {
    error("No WindowState found in LocalWindowState")
}