package com.kkr.juken5livemonitor.screen.monitor.racing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun EliteDashTelemetryGrid(
    liveData: ECULiveData,
    vm: MonitorViewModel,
    modifier: Modifier = Modifier
) {
    val engineRuntime by vm.engineRuntime.collectAsState("00:00:00")
    val zeroToHundredStatus by vm.zeroToHundredStatus.collectAsState()
    val zeroToHundredResult by vm.zeroToHundredResult.collectAsState()
    val horsePower by vm.estHorsePower.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(top = 4.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            // Row 1: Battery, RPM, IAT, BASE MAP, Ignition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GridItem("BATTERY", "%.1f".format(liveData.battery), "V", Modifier.weight(1f))
                GridItem("ENGINE RPM", liveData.rpm.toString(), "RPM", Modifier.weight(1f))
                GridItem("SPEED", liveData.speed.toInt().toString(), "KM/H", Modifier.weight(1f))
                GridItem("BASE MAP", liveData.baseMap, "", Modifier.weight(1f))
                GridItem("IGNITION", "%.1f".format(liveData.ignition), "°", Modifier.weight(1f))
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

            // Row 2: 0-100, HP, Engine Runtime, Top Speed, Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 0-100 Item (Clickable)
                val timerColor = when(zeroToHundredStatus) {
                    MonitorViewModel.TimerStatus.READY -> Color.Yellow
                    MonitorViewModel.TimerStatus.RUNNING -> Color.Green
                    MonitorViewModel.TimerStatus.COOLDOWN -> Color.Gray
                    else -> Color.White
                }
                
                val timerValue = when(zeroToHundredStatus) {
                    MonitorViewModel.TimerStatus.READY -> "READY"
                    else -> zeroToHundredResult
                }
                
                GridItem(
                    label = "0-100 KM/H", 
                    value = timerValue, 
                    unit = "", 
                    modifier = Modifier.weight(1f).clickable { vm.armZeroToHundred() },
                    valueColor = timerColor
                )
                
                val displayedHp = if (zeroToHundredStatus == MonitorViewModel.TimerStatus.FINISHED) {
                    "%.1f".format(horsePower)
                } else {
                    "0.0"
                }

                GridItem("EST. HP", displayedHp, "HP", Modifier.weight(1f))
                GridItem("RUNTIME", engineRuntime, "", Modifier.weight(1f))
                GridItem("MAP", liveData.map.toString(), "KPA", Modifier.weight(1f))
                GridItem("INTAKE AIR", "${liveData.iat.toInt()}", "°C", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GridItem(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Column(
        modifier = modifier.padding(top = 0.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 32.sp
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(unit, color = Color.Gray, fontSize = 9.sp)
            }
        }
    }
}
