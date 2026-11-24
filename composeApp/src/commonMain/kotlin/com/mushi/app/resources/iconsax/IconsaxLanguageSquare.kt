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

val Iconsax.IconsaxLanguageSquare: ImageVector
    get() {
        if (_IconsaxLanguageSquare != null) {
            return _IconsaxLanguageSquare!!
        }
        _IconsaxLanguageSquare = ImageVector.Builder(
            name = "IconsaxLanguageSquare",
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
                    moveTo(16.99f, 8.96f)
                    horizontalLineTo(7.01f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(12f, 7.28f)
                    verticalLineTo(8.96f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(14.5f, 8.94f)
                    curveTo(14.5f, 13.24f, 11.14f, 16.72f, 7f, 16.72f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(17f, 16.72f)
                    curveTo(15.2f, 16.72f, 13.6f, 15.76f, 12.45f, 14.25f)
                }
                path(
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(9f, 22f)
                    horizontalLineTo(15f)
                    curveTo(20f, 22f, 22f, 20f, 22f, 15f)
                    verticalLineTo(9f)
                    curveTo(22f, 4f, 20f, 2f, 15f, 2f)
                    horizontalLineTo(9f)
                    curveTo(4f, 2f, 2f, 4f, 2f, 9f)
                    verticalLineTo(15f)
                    curveTo(2f, 20f, 4f, 22f, 9f, 22f)
                    close()
                }
            }
        }.build()

        return _IconsaxLanguageSquare!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxLanguageSquare: ImageVector? = null
