package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SurfaceContainer(
    modifier: Modifier = Modifier,
    metrics: SurfaceMetrics,
    content: @Composable () -> Unit
) {

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        content()

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.65f),
                    RoundedCornerShape(14.dp)
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                )
        ) {

            Text(
                text =
                    """
Android Auto Engine

Surface :
${if(metrics.created) "READY" else "NOT READY"}

Resolution :
${metrics.width} × ${metrics.height}

Status :
Waiting Engine
                """.trimIndent(),

                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

        }

    }

}