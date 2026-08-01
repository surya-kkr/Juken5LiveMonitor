package com.kkr.juken5livemonitor.screen.monitor.classic

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ClassicTpsBar(
    tps: Int = 0
) {

    val totalBars = 14

    val activeBars =
        ((tps / 100f) * totalBars)
            .toInt()
            .coerceIn(0, totalBars)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(18.dp)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.10f),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "TPS",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
                        .fillMaxWidth(0.82f)
                        .height(9.dp)
                        .background(
                            if (i <= activeBars)
                                color
                            else
                                Color.DarkGray.copy(alpha = 0.35f),
                            RoundedCornerShape(5.dp)
                        )
                )

            }

        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "$tps%",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

    }

}
