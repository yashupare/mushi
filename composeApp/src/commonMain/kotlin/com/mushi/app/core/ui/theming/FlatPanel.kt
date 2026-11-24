package com.mushi.app.core.ui.theming

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A plain panel: the theme's surface colour, rounded corners, and nothing else. No gradient,
 * border, glow or shadow. Use it wherever a screen used to draw its own glossy panel.
 */
fun Modifier.flatPanel(shape: Shape = RoundedCornerShape(14.dp)): Modifier = composed {
    val fill = AppTheme.colors.surface
    this.clip(shape).background(fill)
}
