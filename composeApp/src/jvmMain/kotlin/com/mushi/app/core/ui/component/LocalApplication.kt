package com.mushi.app.core.ui.component

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ApplicationScope
import dev.nucleusframework.application.NucleusApplicationScope

val LocalApplicationScope = staticCompositionLocalOf< NucleusApplicationScope> {
    error("No ApplicationScope found in LocalApplicationScope")
}