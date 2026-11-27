package com.mushi.app.core.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** The four-pointed star of kept moments. */
internal fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color, filled: Boolean = true) {
    val (x, y) = center
    val waist = radius * 0.18f
    val path = Path().apply {
        moveTo(x, y - radius)
        quadraticTo(x + waist, y - waist, x + radius, y)
        quadraticTo(x + waist, y + waist, x, y + radius)
        quadraticTo(x - waist, y + waist, x - radius, y)
        quadraticTo(x - waist, y - waist, x, y - radius)
        close()
    }
    drawPath(path, color, style = if (filled) Fill else Stroke(1.6.dp.toPx()))
}
