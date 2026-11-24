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

val Iconsax.IconsaxAddSquare: ImageVector
    get() {
        if (_IconsaxAddSquare != null) {
            return _IconsaxAddSquare!!
        }
        _IconsaxAddSquare = ImageVector.Builder(
            name = "IconsaxAddSquare",
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
                    moveTo(14.99f, 12f)
                    horizontalLineTo(16f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(8f, 12f)
                    horizontalLineTo(11.81f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(12f, 16f)
                    verticalLineTo(8f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(2f, 13.04f)
                    verticalLineTo(15f)
                    curveTo(2f, 20f, 4f, 22f, 9f, 22f)
                    horizontalLineTo(15f)
                    curveTo(20f, 22f, 22f, 20f, 22f, 15f)
                    verticalLineTo(9f)
                    curveTo(22f, 4f, 20f, 2f, 15f, 2f)
                    horizontalLineTo(9f)
                    curveTo(4f, 2f, 2f, 4f, 2f, 9f)
                }
            }
        }.build()

        return _IconsaxAddSquare!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxAddSquare: ImageVector? = null
