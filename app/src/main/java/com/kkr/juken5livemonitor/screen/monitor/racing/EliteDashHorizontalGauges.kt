package com.kkr.juken5livemonitor.screen.monitor.racing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData

@Composable
fun EliteDashHorizontalGauges(
    liveData: ECULiveData,
    modifier: Modifier = Modifier
) {
    // Smooth Animation for Speed and RPM
    val smoothSpeed by animateFloatAsState(
        targetValue = liveData.speed,
        label = "SmoothSpeed"
    )
    
    val smoothRpm by animateFloatAsState(
        targetValue = (liveData.rpm.toFloat() / 1000f).coerceIn(0f, 12f),
        label = "SmoothRpm"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp) // Large height restored
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left: Speed Ruler (0-200) ---
        RulerGauge(
            modifier = Modifier.weight(1f),
            label = "KM/H",
            max = 200,
            value = smoothSpeed,
            steps = 10,
            reverse = true
        )

        // --- Center: Fuel Mode (Giant) ---
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val fuelDisplay = when (liveData.fuel) {
                "0" -> "1"
                "1" -> "2"
                else -> liveData.fuel.ifEmpty { "1" }
            }
            Text(
                text = fuelDisplay,
                color = Color(0xFF00FF00),
                fontSize = 84.sp, 
                fontWeight = FontWeight.Black
            )
        }

        // --- Right: RPM Ruler (0-12) ---
        RulerGauge(
            modifier = Modifier.weight(1f),
            label = "RPM (x1000)",
            max = 12,
            value = smoothRpm,
            steps = 12,
            reverse = false
        )
    }
}

@Composable
private fun RulerGauge(
    modifier: Modifier = Modifier,
    label: String,
    max: Int,
    value: Float,
    steps: Int,
    reverse: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (reverse) Alignment.End else Alignment.Start
    ) {
        // Numbers row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val stepVal = max / steps
            if (!reverse) {
                for (i in 0..steps) {
                    Text(
                        text = (i * stepVal).toString(),
                        color = Color.White,
                        fontSize = 11.sp, // Large font restored
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                for (i in steps downTo 0) {
                    Text(
                        text = (i * stepVal).toString(),
                        color = Color.White,
                        fontSize = 11.sp, // Large font restored
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Ruler bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp) // Large bar thickness restored
                .background(Color.Red.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val progress = value / max
                
                // Draw red fill
                if (!reverse) {
                    drawRect(
                        color = Color.Red,
                        size = androidx.compose.ui.geometry.Size(width * progress.coerceIn(0f, 1f), height)
                    )
                } else {
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(width * (1f - progress.coerceIn(0f, 1f)), 0f),
                        size = androidx.compose.ui.geometry.Size(width * progress.coerceIn(0f, 1f), height)
                    )
                }

                // Draw ticks
                val tickCount = steps * 2 
                for (i in 0..tickCount) {
                    val x = (i.toFloat() / tickCount) * width
                    val tickHeight = if (i % 2 == 0) height else height * 0.4f
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(x, 0f),
                        end = Offset(x, tickHeight),
                        strokeWidth = 2f // Thick ticks restored
                    )
                }
            }
        }

        Text(
            text = label,
            color = Color.Red,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
