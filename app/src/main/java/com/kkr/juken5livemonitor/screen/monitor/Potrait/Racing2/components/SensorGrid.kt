package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.model.DashboardData
import java.util.Locale

@Composable
fun SensorGrid(
    data: DashboardData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                name = "AFR",
                value = String.format(Locale.US, "%.1f", data.afr),
                unit = "",
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )
            SensorCard(
                name = "CORE ACTIVE",
                value = data.coreActive.toString(),
                unit = "",
                icon = Icons.Default.Memory,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                name = "BATTERY",
                value = String.format(Locale.US, "%.1f", data.battery),
                unit = "V",
                icon = Icons.Default.BatteryChargingFull,
                modifier = Modifier.weight(1f)
            )
            SensorCard(
                name = "Engine Temp",
                value = data.eot.toString(),
                unit = "°C",
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                name = "AIR TEMP",
                value = data.ait.toString(),
                unit = "°C",
                icon = Icons.Default.Air,
                modifier = Modifier.weight(1f)
            )
            SensorCard(
                name = "BASE MAP",
                value = data.mapName,
                unit = "",
                icon = Icons.Default.Compress,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
