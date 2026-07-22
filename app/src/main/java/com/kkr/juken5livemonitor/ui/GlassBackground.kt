package com.kkr.juken5livemonitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GlassBackground(
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF050505),
                        Color(0xFF101010),
                        Color(0xFF1A1A1A)
                    )
                )
            )
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55E53935),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.55f,
                center = Offset(
                    x = size.width * 0.15f,
                    y = size.height * 0.15f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x22FFFFFF),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.45f,
                center = Offset(
                    x = size.width * 0.90f,
                    y = size.height * 0.10f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x22E53935),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.65f,
                center = Offset(
                    x = size.width * 0.80f,
                    y = size.height * 0.85f
                )
            )

        }

        content()

    }

}