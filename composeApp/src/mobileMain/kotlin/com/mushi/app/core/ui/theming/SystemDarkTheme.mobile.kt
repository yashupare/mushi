package com.mushi.app.core.ui.theming

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun isSystemDarkTheme(): Boolean = isSystemInDarkTheme()
