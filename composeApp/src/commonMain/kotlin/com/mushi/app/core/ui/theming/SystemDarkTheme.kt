package com.mushi.app.core.ui.theming

import androidx.compose.runtime.Composable

/** Whether the OS is in dark mode, observed live; desktop can't rely on Compose's own check under Tao. */
@Composable
expect fun isSystemDarkTheme(): Boolean
