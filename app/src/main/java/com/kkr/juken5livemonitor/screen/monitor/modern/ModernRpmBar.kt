package com.kkr.juken5livemonitor.screen.monitor.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernRpmBar(
    rpm: Int = 0,
    maxRpm: Int = 14000
) {
    val totalBars = 32
    val activeBars = ((rpm.toFloat() / maxRpm) * totalBars).toInt().coerceIn(0, totalBars)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(68.dp) // Original width for better bar coverage
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RPM",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in totalBars downTo 1) {
                    val fraction = (i - 1).toFloat() / (totalBars - 1)
                    val color = lerp(
                        start = Color(0xFF2196F3),
                        stop = Color(0xFFF44336),
                        fraction = fraction
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                            .background(
                                if (i <= activeBars) color else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = rpm.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "x1000",
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }
        }
    }
}
