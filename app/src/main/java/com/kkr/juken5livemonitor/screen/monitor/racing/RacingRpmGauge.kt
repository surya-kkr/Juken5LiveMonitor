package com.kkr.juken5livemonitor.screen.monitor.racing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RacingRpmGauge(
    rpm: Int,
    maxRpm: Int = 14000
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Shift Lights (Row of LEDs)
        Row(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(24.dp)
                .background(Color.Black, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val progress = rpm.toFloat() / maxRpm
            val lightCount = 10
            
            for (i in 1..lightCount) {
                val threshold = i.toFloat() / lightCount
                val active = progress >= threshold
                val color = when {
                    i <= 3 -> Color.White
                    i <= 6 -> Color(0xFFFFA000) // Orange
                    i <= 8 -> Color.Red
                    else -> Color(0xFFE91E63) // Pink/Magenta
                }
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (active) color else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (active) color.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Angled RPM Bar (Modern Style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val activeWidth = (rpm.toFloat() / maxRpm) * width

                // Draw Path for the RPM "Fill" (Trapezoid/Angled look)
                val path = Path().apply {
                    moveTo(0f, height)
                    lineTo(activeWidth, height)
                    lineTo(activeWidth, height * 0.15f) // More aggressive angle
                    lineTo(0f, height * 0.35f)
                    close()
                }
                
                drawPath(path, color = Color.Red.copy(alpha = 0.7f))
                
                // Draw Grid/Notches
                for (i in 0..14) {
                    val x = (i.toFloat() / 14) * width
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(x, height),
                        end = Offset(x, 0f),
                        strokeWidth = 1.5f
                    )
                }
            }

            // RPM Numbers (1 to 14) - White
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..14) {
                    Text(
                        text = i.toString(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
