package com.kkr.juken5livemonitor.screen.monitor.Landscape.modern

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernRpmBar(
    modifier: Modifier = Modifier,
    rpm: Int = 0,
    maxRpm: Int = 14000,
) {
    val totalBars = 32
    val targetProgress = when {
        rpm >= maxRpm -> 1f
        rpm >= 500 -> {
            val range = (maxRpm - 500).toFloat()
            ((rpm - 500).toFloat() / range).coerceIn(0f, 1f)
        }
        rpm >= 1 -> 0.01f
        else -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 300f
        ),
        label = "ModernRpmProgress"
    )

    val activeBars = when {
        rpm >= maxRpm -> totalBars
        rpm >= 500 -> {
            1 + (animatedProgress * (totalBars - 1)).toInt()
        }
        rpm >= 1 -> 1
        else -> 0
    }.coerceIn(0, totalBars)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(52.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 5.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RPM",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.5.dp),
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
                            .weight(1f)
                            .fillMaxWidth(0.88f)
                            .background(
                                if (i <= activeBars) color else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = rpm.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (rpm >= 10000) 13.sp else 14.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "x1000",
                    color = Color.Gray,
                    fontSize = 8.sp,
                    lineHeight = 8.sp
                )
            }
        }
    }
}

