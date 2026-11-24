package com.mushi.app.resources.iconsax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Iconsax.IconsaxCd: ImageVector
    get() {
        if (_IconsaxCd != null) {
            return _IconsaxCd!!
        }
        _IconsaxCd = ImageVector.Builder(
            name = "IconsaxCd",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(20f)
                    verticalLineToRelative(20f)
                    horizontalLineToRelative(-20f)
                    close()
                }
            ) {
                path(
                    fill = SolidColor(Color.White),
                    fillAlpha = 0.4f,
                    strokeAlpha = 0.4f
                ) {
                    moveTo(10f, 20f)
                    curveTo(15.523f, 20f, 20f, 15.523f, 20f, 10f)
                    curveTo(20f, 4.477f, 15.523f, 0f, 10f, 0f)
                    curveTo(4.477f, 0f, 0f, 4.477f, 0f, 10f)
                    curveTo(0f, 15.523f, 4.477f, 20f, 10f, 20f)
                    close()
                }
                path(fill = SolidColor(Color.White)) {
                    moveTo(10f, 12.5f)
                    curveTo(11.381f, 12.5f, 12.5f, 11.381f, 12.5f, 10f)
                    curveTo(12.5f, 8.619f, 11.381f, 7.5f, 10f, 7.5f)
                    curveTo(8.619f, 7.5f, 7.5f, 8.619f, 7.5f, 10f)
                    curveTo(7.5f, 11.381f, 8.619f, 12.5f, 10f, 12.5f)
                    close()
                }
            }
        }.build()

        return _IconsaxCd!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxCd: ImageVector? = null
