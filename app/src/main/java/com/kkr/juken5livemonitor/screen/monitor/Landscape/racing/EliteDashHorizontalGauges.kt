package com.kkr.juken5livemonitor.screen.monitor.Landscape.racing

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // --- Top: Horizontal Rulers & Fuel Mode ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            // --- Left: Speed Ruler (0-200) ---
            RulerGauge(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-8).dp),
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
                    else -> "1"
                }
                Text(
                    modifier = Modifier.offset(y = (-18).dp),
                    text = fuelDisplay,
                    color = Color(0xFF00FF00),
                    fontSize = 84.sp, 
                    fontWeight = FontWeight.Black
                )
            }

            // --- Right: RPM Ruler (0-12) ---
            RulerGauge(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-8).dp),
                label = "RPM (x1000)",
                max = 12,
                value = smoothRpm,
                steps = 12,
                reverse = false
            )
        }

        // --- Bottom: Segmented LED TPS Bar ---
        RacingCenterTpsBar(
            tps = liveData.tps,
            modifier = Modifier.fillMaxWidth()
        )

        // --- Label THROTTLE ---
        Text(
            text = "THROTTLE",
            color = Color.Red,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun RacingCenterTpsBar(
    tps: Int,
    modifier: Modifier = Modifier
) {
    val smoothTps by animateFloatAsState(
        targetValue = tps.toFloat().coerceIn(0f, 100f),
        label = "SmoothTpsBar"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalBlocks = 32
        val halfBlocks = totalBlocks / 2
        val gap = 3.dp.toPx()
        val totalGap = gap * (totalBlocks - 1)
        val blockWidth = (width - totalGap) / totalBlocks
        val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

        for (i in 0 until totalBlocks) {
            val left = i * (blockWidth + gap)

            val distance = if (i < halfBlocks) {
                halfBlocks - 1 - i
            } else {
                i - halfBlocks
            }

            val posFraction = (distance + 1).toFloat() / halfBlocks
            val minThreshold = (distance.toFloat() / halfBlocks) * 100f

            val active = smoothTps > 0f && smoothTps >= minThreshold

            val blockColor = when {
                posFraction <= 0.70f -> Color(0xFF00FF00) // Green (0-70%)
                posFraction <= 0.90f -> Color(0xFFFFFF00) // Yellow (70-90%)
                else -> Color(0xFFFF0000)               // Red (90-100%)
            }

            // Draw unlit block background
            drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                topLeft = Offset(left, 0f),
                size = Size(blockWidth, height),
                cornerRadius = cornerRadius
            )

            // Draw active LED block
            if (active) {
                drawRoundRect(
                    color = blockColor,
                    topLeft = Offset(left, 0f),
                    size = Size(blockWidth, height),
                    cornerRadius = cornerRadius
                )
            }
        }
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
                        size = Size(width * progress.coerceIn(0f, 1f), height)
                    )
                } else {
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(width * (1f - progress.coerceIn(0f, 1f)), 0f),
                        size = Size(width * progress.coerceIn(0f, 1f), height)
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
