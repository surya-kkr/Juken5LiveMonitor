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
fun ClassicTpsBar(
    modifier: Modifier = Modifier,
    tps: Int = 0,
) {
    val totalBars = 16

    val targetProgress = (tps / 100f).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 300f
        ),
        label = "ClassicTpsProgress"
    )

    val activeBars = when {
        tps >= 100 -> totalBars
        tps >= 1 -> maxOf(1, (animatedProgress * totalBars).toInt())
        else -> 0
    }.coerceIn(0, totalBars)

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
            text = "TPS",
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
                val color = when {
                    i >= 12 -> Color(0xFF00E676)
                    i >= 9 -> Color(0xFF43A047)
                    else -> Color(0xFF81C784)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.85f)
                        .background(
                            if (i <= activeBars)
                                color
                            else
                                Color.DarkGray.copy(alpha = 0.35f),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "$tps%",
            fontSize = 16.sp,
            lineHeight = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}
