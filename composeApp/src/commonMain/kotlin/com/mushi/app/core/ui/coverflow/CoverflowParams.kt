package com.mushi.app.core.ui.coverflow

import androidx.compose.runtime.Immutable

@Immutable
data class CoverflowParams(
    val size: Float = 0.5f,
    val offset: Float = 0.4f,
    val angle: Float = 45f,
    val shift: Float = 0.4f,
    val zoom: Float = 0.8f,
    val mirror: Boolean = false)
