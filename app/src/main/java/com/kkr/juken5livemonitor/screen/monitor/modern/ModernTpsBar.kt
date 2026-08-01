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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernTpsBar(
    tps: Int = 0
) {
    val totalBars = 32
    val activeBars = ((tps / 100f) * totalBars).toInt().coerceIn(0, totalBars)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(68.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TPS",
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                            .background(
                                if (i <= activeBars) Color.Red else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tps.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "%",
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }
    }
}
