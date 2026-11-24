package com.mushi.app.resources.iconsax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Iconsax.IconsaxSidebarRightBroken: ImageVector
    get() {
        if (_IconsaxSidebarRightBroken != null) {
            return _IconsaxSidebarRightBroken!!
        }
        _IconsaxSidebarRightBroken = ImageVector.Builder(
            name = "IconsaxSidebarRightBroken",
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
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(1.97f, 12.98f)
                    verticalLineTo(15f)
                    curveTo(1.97f, 20f, 3.97f, 22f, 8.97f, 22f)
                    horizontalLineTo(14.97f)
                    curveTo(19.97f, 22f, 21.97f, 20f, 21.97f, 15f)
                    verticalLineTo(9f)
                    curveTo(21.97f, 4f, 19.97f, 2f, 14.97f, 2f)
                    horizontalLineTo(8.97f)
                    curveTo(3.97f, 2f, 1.97f, 4f, 1.97f, 9f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(14.97f, 2f)
                    verticalLineTo(22f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(7.97f, 9.439f)
                    lineTo(10.53f, 12f)
                    lineTo(7.97f, 14.559f)
                }
            }
        }.build()

        return _IconsaxSidebarRightBroken!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxSidebarRightBroken: ImageVector? = null
