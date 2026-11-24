package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardBlack
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardCardBorder

@Composable
fun TpsBar(
    tps: Float,
    modifier: Modifier = Modifier,
    segmentCount: Int = 50
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = "TPS",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${tps.toInt()}%",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(DashboardBlack, RoundedCornerShape(12.dp))
                .border(2.dp, DashboardCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spacing = 3.dp.toPx()
                val totalWidth = size.width
                val tickWidth = (totalWidth - (segmentCount - 1) * spacing) / segmentCount
                
                val majorTickHeight = size.height * 0.8f
                val minorTickHeight = size.height * 0.5f

                for (i in 0 until segmentCount) {
                    val progress = i.toFloat() / segmentCount
                    val isActive = progress <= (tps / 100f)
                    val isRedline = progress >= 0.8f

                    val color = when {
                        isActive && isRedline -> DashboardCardBorder
                        isActive -> Color.White
                        else -> Color.White.copy(alpha = 0.15f)
                    }

                    val isMajorTick = i % 5 == 0
                    val currentTickHeight = if (isMajorTick) majorTickHeight else minorTickHeight
                    val yOffset = (size.height - currentTickHeight) / 2

                    drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(i * (tickWidth + spacing) + tickWidth/2, yOffset),
                        end = androidx.compose.ui.geometry.Offset(i * (tickWidth + spacing) + tickWidth/2, yOffset + currentTickHeight),
                        strokeWidth = if (isActive) 5.dp.toPx() else 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
