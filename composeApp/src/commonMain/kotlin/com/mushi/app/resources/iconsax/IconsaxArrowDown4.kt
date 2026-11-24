package com.mushi.app.resources.iconsax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Iconsax.IconsaxArrowDown4: ImageVector
    get() {
        if (_IconsaxArrowDown4 != null) {
            return _IconsaxArrowDown4!!
        }
        _IconsaxArrowDown4 = ImageVector.Builder(
            name = "IconsaxArrowDown4",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.01f, 12.85f)
                lineTo(13.39f, 15.47f)
                curveTo(12.62f, 16.24f, 11.36f, 16.24f, 10.59f, 15.47f)
                lineTo(4.08f, 8.95f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.92f, 8.95f)
                lineTo(18.88f, 9.99f)
            }
        }.build()

        return _IconsaxArrowDown4!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxArrowDown4: ImageVector? = null
