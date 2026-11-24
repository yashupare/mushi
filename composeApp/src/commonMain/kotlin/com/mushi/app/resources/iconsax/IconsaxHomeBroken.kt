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

val Iconsax.IconsaxHomeBroken: ImageVector
    get() {
        if (_IconsaxHomeBroken != null) {
            return _IconsaxHomeBroken!!
        }
        _IconsaxHomeBroken = ImageVector.Builder(
            name = "IconsaxHomeBroken",
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
                    moveTo(12f, 18f)
                    verticalLineTo(15f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(20.64f, 19.24f)
                    curveTo(20.4f, 20.65f, 19.03f, 21.81f, 17.6f, 21.81f)
                    horizontalLineTo(6.4f)
                    curveTo(4.96f, 21.81f, 3.6f, 20.66f, 3.36f, 19.24f)
                    lineTo(2.03f, 11.28f)
                    curveTo(1.86f, 10.3f, 2.36f, 8.99f, 3.14f, 8.37f)
                    lineTo(10.07f, 2.82f)
                    curveTo(11.13f, 1.97f, 12.86f, 1.97f, 13.93f, 2.83f)
                    lineTo(20.86f, 8.37f)
                    curveTo(21.63f, 8.99f, 22.13f, 10.3f, 21.97f, 11.28f)
                    lineTo(21.35f, 15f)
                }
            }
        }.build()

        return _IconsaxHomeBroken!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxHomeBroken: ImageVector? = null
