package com.kkr.juken5livemonitor.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

    val SparkPlug: ImageVector
        get() = _sparkPlug ?: ImageVector.Builder(
            name = "SparkPlug",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 3f)
                lineTo(19f, 6f)
                lineTo(15f, 10f)
                lineTo(13f, 8f)
                close()
                moveTo(13f, 8f)
                lineTo(15f, 10f)
                lineTo(11f, 14f)
                lineTo(9f, 12f)
                close()
                moveTo(10f, 13f)
                lineTo(12f, 15f)
                lineTo(9f, 18f)
                lineTo(7f, 16f)
                close()
                moveTo(8f, 17f)
                lineTo(9f, 18f)
                lineTo(7f, 20f)
                lineTo(6f, 19f)
                close()
                moveTo(4f, 22f)
                lineTo(6f, 18f)
                lineTo(8f, 21f)
                lineTo(4f, 22f)
                close()
            }
        }.build().also { _sparkPlug = it }

    private var _sparkPlug: ImageVector? = null

    val Injector: ImageVector
        get() = _injector ?: ImageVector.Builder(
            name = "Injector",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10.5f, 1.5f)
                horizontalLineTo(13.5f)
                verticalLineTo(4f)
                horizontalLineTo(10.5f)
                close()
                
                moveTo(9f, 4f)
                horizontalLineTo(15f)
                verticalLineTo(16.5f)
                horizontalLineTo(9f)
                close()

                moveTo(9f, 6f)
                lineTo(5.5f, 8.5f)
                lineTo(7f, 11f)
                lineTo(9f, 9.5f)
                close()

                moveTo(11f, 16.5f)
                horizontalLineTo(13f)
                verticalLineTo(19.5f)
                horizontalLineTo(11f)
                close()

                moveTo(10f, 20f)
                lineTo(8f, 23.5f)
                moveTo(12f, 20f)
                verticalLineTo(24f)
                moveTo(14f, 20f)
                lineTo(16f, 23.5f)
            }
        }.build().also { _injector = it }

    private var _injector: ImageVector? = null

    val Engine: ImageVector
        get() = _engine ?: ImageVector.Builder(
            name = "Engine",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 4f)
                verticalLineTo(6f)
                horizontalLineTo(10f)
                verticalLineTo(8f)
                horizontalLineTo(7f)
                lineTo(5f, 10f)
                verticalLineTo(13f)
                horizontalLineTo(3f)
                verticalLineTo(10f)
                horizontalLineTo(1f)
                verticalLineTo(18f)
                horizontalLineTo(3f)
                verticalLineTo(15f)
                horizontalLineTo(5f)
                verticalLineTo(18f)
                horizontalLineTo(8f)
                lineTo(10f, 20f)
                horizontalLineTo(18f)
                lineTo(20f, 18f)
                verticalLineTo(10f)
                lineTo(18f, 8f)
                horizontalLineTo(15f)
                verticalLineTo(6f)
                horizontalLineTo(18f)
                verticalLineTo(4f)
                horizontalLineTo(7f)
                close()
            }
        }.build().also { _engine = it }

    private var _engine: ImageVector? = null

    val Processor: ImageVector
        get() = _processor ?: ImageVector.Builder(
            name = "Processor",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 2f)
                verticalLineTo(4f)
                horizontalLineTo(7f)
                verticalLineTo(2f)
                horizontalLineTo(5f)
                verticalLineTo(4f)
                curveTo(3.9f, 4f, 3f, 4.9f, 3f, 6f)
                verticalLineTo(8f)
                horizontalLineTo(1f)
                verticalLineTo(10f)
                horizontalLineTo(3f)
                verticalLineTo(12f)
                horizontalLineTo(1f)
                verticalLineTo(14f)
                horizontalLineTo(3f)
                verticalLineTo(16f)
                horizontalLineTo(1f)
                verticalLineTo(18f)
                horizontalLineTo(3f)
                verticalLineTo(20f)
                curveTo(3f, 21.1f, 3.9f, 22f, 5f, 22f)
                verticalLineTo(24f)
                horizontalLineTo(7f)
                verticalLineTo(22f)
                horizontalLineTo(9f)
                verticalLineTo(24f)
                horizontalLineTo(11f)
                verticalLineTo(22f)
                horizontalLineTo(13f)
                verticalLineTo(24f)
                horizontalLineTo(15f)
                verticalLineTo(22f)
                horizontalLineTo(17f)
                verticalLineTo(24f)
                horizontalLineTo(19f)
                verticalLineTo(22f)
                curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
                verticalLineTo(18f)
                horizontalLineTo(23f)
                verticalLineTo(16f)
                horizontalLineTo(21f)
                verticalLineTo(14f)
                horizontalLineTo(23f)
                verticalLineTo(12f)
                horizontalLineTo(21f)
                verticalLineTo(10f)
                horizontalLineTo(23f)
                verticalLineTo(8f)
                horizontalLineTo(21f)
                verticalLineTo(6f)
                curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
                verticalLineTo(2f)
                horizontalLineTo(17f)
                verticalLineTo(4f)
                horizontalLineTo(15f)
                verticalLineTo(2f)
                horizontalLineTo(13f)
                verticalLineTo(4f)
                horizontalLineTo(11f)
                verticalLineTo(2f)
                horizontalLineTo(9f)
                moveTo(7f, 7f)
                horizontalLineTo(17f)
                verticalLineTo(17f)
                horizontalLineTo(7f)
                verticalLineTo(7f)
                close()
            }
        }.build().also { _processor = it }

    private var _processor: ImageVector? = null
}
