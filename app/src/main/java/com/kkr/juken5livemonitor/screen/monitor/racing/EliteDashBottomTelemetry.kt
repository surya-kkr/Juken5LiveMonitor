package com.kkr.juken5livemonitor.screen.monitor.racing

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun EliteDashBottomTelemetry(
    liveData: ECULiveData,
    topSpeed: Float,
    avgSpeed: Float,
    zeroToHundred: String,
    zeroToHundredStatus: MonitorViewModel.TimerStatus,
    estHp: Double,
    onArmTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val smoothSpeed by animateFloatAsState(
        targetValue = liveData.speed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow * 0.5f
        ),
        label = "SmoothSpeed"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left Block: 2x2 Stats ---
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Red, Color.Transparent)))
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    StatGridItem("IGNITION", "%.1f".format(liveData.ignition), "°")
                    Spacer(Modifier.height(8.dp))
                    StatGridItem("OIL TEMP", "%.1f".format(liveData.eot), "°C")
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    StatGridItem("INJECTOR", liveData.injector, "")
                    Spacer(Modifier.height(8.dp))
                    StatGridItem("AIR TEMP", "${liveData.iat.toInt()}", "°C")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .width(1.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent)))
        )

        // --- Center Block: SPEED ---
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .width(220.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Text("SPEED", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Text(
                text = "%03d".format(smoothSpeed.toInt()),
                color = Color.White,
                fontSize = 80.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .width(1.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent)))
        )

        // --- Right Block: 2x2 Performance Stats ---
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Red)))
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    // 0-100 logic
                    val timerColor = when(zeroToHundredStatus) {
                        MonitorViewModel.TimerStatus.READY -> Color.Yellow
                        MonitorViewModel.TimerStatus.RUNNING -> Color.Green
                        MonitorViewModel.TimerStatus.COOLDOWN -> Color.Gray
                        else -> Color.White
                    }
                    val timerValue = if (zeroToHundredStatus == MonitorViewModel.TimerStatus.READY) "READY" else zeroToHundred
                    
                    StatGridItem(
                        label = "0-100 KM/H", 
                        value = timerValue, 
                        unit = if (zeroToHundredStatus == MonitorViewModel.TimerStatus.READY) "" else "s",
                        valueColor = timerColor,
                        modifier = Modifier.clickable { onArmTimer() }
                    )
                    Spacer(Modifier.height(8.dp))
                    StatGridItem("TOP SPEED", "${topSpeed.toInt()}", "kpj")
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    // HP logic
                    val displayedHp = if (zeroToHundredStatus == MonitorViewModel.TimerStatus.FINISHED) {
                        "%.1f".format(estHp)
                    } else {
                        "0.0"
                    }
                    StatGridItem("EST. HP", displayedHp, "HP")
                    Spacer(Modifier.height(8.dp))
                    StatGridItem("AVG SPEED", "${avgSpeed.toInt()}", "kpj")
                }
            }
        }
    }
}

@Composable
private fun StatGridItem(
    label: String,
    value: String,
    unit: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(label, color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 28.sp
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit, 
                    color = Color.White.copy(alpha = 0.6f), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}
