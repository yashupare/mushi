package com.mushi.app.resources.iconsax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Iconsax.IconsaxSearchBroken: ImageVector
    get() {
        if (_IconsaxSearchBroken != null) {
            return _IconsaxSearchBroken!!
        }
        _IconsaxSearchBroken = ImageVector.Builder(
            name = "IconsaxSearchBroken",
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
                moveTo(11.5f, 2f)
                curveTo(16.75f, 2f, 21f, 6.25f, 21f, 11.5f)
                curveTo(21f, 16.75f, 16.75f, 21f, 11.5f, 21f)
                curveTo(6.25f, 21f, 2f, 16.75f, 2f, 11.5f)
                curveTo(2f, 7.8f, 4.11f, 4.6f, 7.2f, 3.03f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(22f, 22f)
                lineTo(20f, 20f)
            }
        }.build()

        return _IconsaxSearchBroken!!
    }

@Suppress("ObjectPropertyName")
private var _IconsaxSearchBroken: ImageVector? = null
