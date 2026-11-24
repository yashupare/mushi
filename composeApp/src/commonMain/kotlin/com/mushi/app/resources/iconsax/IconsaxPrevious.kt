package com.mushi.app.resources.iconsax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Iconsax.IconsaxPrevious: ImageVector
    get() {
        if (_IconsaxPrevious != null) {
            return _IconsaxPrevious!!
        }
        _IconsaxPrevious = ImageVector.Builder(
            name = "IconsaxPrevious",
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
                    moveTo(20.24f, 7.22f)
                    verticalLineTo(16.79f)
                    curveTo(20.24f, 18.75f, 18.11f, 19.98f, 16.41f, 19f)
                    lineTo(12.26f, 16.61f)
                    lineTo(8.11f, 14.21f)
                    curveTo(6.41f, 13.23f, 6.41f, 10.78f, 8.11f, 9.8f)
                    lineTo(12.26f, 7.4f)
                    lineTo(16.41f, 5.01f)
                    curveTo(18.11f, 4.03f, 20.24f, 5.25f, 20.24f, 7.22f)
                    close()
                }
                path(fill = SolidColor(Color.White)) {
                    moveTo(3.76f, 18.93f)
                    curveTo(3.35f, 18.93f, 3.01f, 18.59f, 3.01f, 18.18f)
                    verticalLineTo(5.82f)
                    curveTo(3.01f, 5.41f, 3.35f, 5.07f, 3.76f, 5.07f)
                    curveTo(4.17f, 5.07f, 4.51f, 5.41f, 4.51f, 5.82f)
                    verticalLineTo(18.18f)
                    curveTo(4.51f, 18.59f, 4.17f, 18.93f, 3.76f, 18.93f)
                    close()
                }
            }
        }.build()

        return _IconsaxPrevious!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxPrevious: ImageVector? = null
