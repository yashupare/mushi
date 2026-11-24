package com.mushi.app.core.ui

import androidx.compose.runtime.Composable

/** Desktop platforms expose no common reduced-motion setting, so animations stay on. */
@Composable
actual fun rememberSystemReduceMotion(): Boolean = false
