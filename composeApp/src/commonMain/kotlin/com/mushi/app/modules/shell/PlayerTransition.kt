@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.mushi.app.modules.shell

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * How far the player sheet is between the mini player (0) and the full player (1), read lazily
 * so a drag updates the morph every frame without recomposing anything. The full player uses
 * it to grow its cover out of the mini player's cover.
 */
internal val LocalPlayerTransition = compositionLocalOf<() -> Float> { { 1f } }

/** Where the mini player's cover sits inside the sheet, and how big it is. */
internal val MiniCoverStartX = 20.dp
internal val MiniCoverStartY = 12.dp
internal val MiniCoverSize = 42.dp
