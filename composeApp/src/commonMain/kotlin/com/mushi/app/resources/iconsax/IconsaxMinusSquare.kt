package com.mushi.app.resources.iconsax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Iconsax.IconsaxMinusSquare: ImageVector
    get() {
        if (_IconsaxMinusSquare != null) {
            return _IconsaxMinusSquare!!
        }
        _IconsaxMinusSquare = ImageVector.Builder(
            name = "IconsaxMinusSquare",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
                path(
                    fill = SolidColor(Color.White),
                    fillAlpha = 0.4f,
                    strokeAlpha = 0.4f
                ) {
                    moveTo(16.19f, 2f)
                    horizontalLineTo(7.81f)
                    curveTo(4.17f, 2f, 2f, 4.17f, 2f, 7.81f)
                    verticalLineTo(16.18f)
                    curveTo(2f, 19.83f, 4.17f, 22f, 7.81f, 22f)
                    horizontalLineTo(16.18f)
                    curveTo(19.82f, 22f, 21.99f, 19.83f, 21.99f, 16.19f)
                    verticalLineTo(7.81f)
                    curveTo(22f, 4.17f, 19.83f, 2f, 16.19f, 2f)
                    close()
                }
                path(fill = SolidColor(Color.White)) {
                    moveTo(16f, 12.75f)
                    horizontalLineTo(8f)
                    curveTo(7.59f, 12.75f, 7.25f, 12.41f, 7.25f, 12f)
                    curveTo(7.25f, 11.59f, 7.59f, 11.25f, 8f, 11.25f)
                    horizontalLineTo(16f)
                    curveTo(16.41f, 11.25f, 16.75f, 11.59f, 16.75f, 12f)
                    curveTo(16.75f, 12.41f, 16.41f, 12.75f, 16f, 12.75f)
                    close()
                }
            }
        }.build()

        return _IconsaxMinusSquare!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxMinusSquare: ImageVector? = null
