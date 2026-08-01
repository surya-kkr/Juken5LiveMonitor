package com.kkr.juken5livemonitor.screen.monitor.kkrxag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KkrRacingLeftGauge(temp: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(30.dp)
    ) {
        Text("H", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(2.dp))
        
        val progress = ((temp - 40) / 100).coerceIn(0.0, 1.0)
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            for (i in 12 downTo 1) {
                val active = (i.toFloat() / 12f) <= progress
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(8.dp)
                        .background(if (active) Color.White else Color.White.copy(alpha = 0.1f))
                )
            }
        }
        
        Spacer(Modifier.height(4.dp))
        Icon(Icons.Default.Thermostat, null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(2.dp))
        Text("C", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun KkrRacingRightGauge(fuel: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(30.dp)
    ) {
        Text("F", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(2.dp))
        
        val progress = (fuel / 100f).coerceIn(0f, 1f)
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            for (i in 12 downTo 1) {
                val active = (i.toFloat() / 12f) <= progress
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(8.dp)
                        .background(if (active) Color.White else Color.White.copy(alpha = 0.1f))
                )
            }
        }
        
        Spacer(Modifier.height(4.dp))
        Icon(Icons.Default.LocalGasStation, null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(2.dp))
        Text("E", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
