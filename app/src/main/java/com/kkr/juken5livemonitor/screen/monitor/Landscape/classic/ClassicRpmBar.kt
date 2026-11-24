package com.kkr.juken5livemonitor.screen.monitor.Landscape.classic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClassicRpmBar(
    modifier: Modifier = Modifier,
    rpm: Int = 0,
    maxRpm: Int = 12000,
) {
    val totalBars = 16
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
        label = "ClassicRpmProgress"
    )

    val activeBars = when {
        rpm >= maxRpm -> totalBars
        rpm >= 500 -> {
            1 + (animatedProgress * (totalBars - 1)).toInt()
        }
        rpm >= 1 -> 1
        else -> 0
    }.coerceIn(0, totalBars)

    val activeColor = when {
        rpm >= 9000 -> Color(0xFFE53935)
        rpm >= 6000 -> Color(0xFFFF9800)
        else -> Color(0xFF00E676)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.10f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RPM",
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in totalBars downTo 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.85f)
                        .background(
                            if (i <= activeBars)
                                activeColor
                            else
                                Color.DarkGray.copy(alpha = 0.35f),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = rpm.toString(),
            fontSize = if (rpm >= 10000) 14.sp else 16.sp,
            lineHeight = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}
