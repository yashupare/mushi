package com.mushi.app.core.ui.theming

import androidx.compose.runtime.Composable
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode

// The Tao window never feeds Compose's LocalSystemTheme, so ask the OS directly
@Composable
actual fun isSystemDarkTheme(): Boolean = isSystemInDarkMode()
