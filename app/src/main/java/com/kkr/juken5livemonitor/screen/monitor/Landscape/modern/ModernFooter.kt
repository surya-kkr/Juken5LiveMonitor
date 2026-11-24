package com.kkr.juken5livemonitor.screen.monitor.Landscape.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.ui.icons.AppIcons

@Composable
fun ModernFooter(
    liveData: ECULiveData,
    onDisconnect: () -> Unit = {},
    onSettings: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxHeight().weight(1.4f), // Maximum width for safety
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(50.dp),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Icon(Icons.Default.PowerSettingsNew, null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text("Disconnect", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
        }

        ModernInfoCard(
            label = "AFR",
            value = "%.1f".format(liveData.afr),
            unit = "",
            icon = Icons.Default.Speed,
            modifier = Modifier.weight(1f)
        )
        ModernInfoCard(
            label = "Batt",
            value = "%.1f".format(liveData.battery),
            unit = "V",
            icon = Icons.Default.BatteryChargingFull,
            modifier = Modifier.weight(1f)
        )
        ModernInfoCard(
            label = "Oil",
            value = liveData.eot.toInt().toString(),
            unit = "°C",
            icon = Icons.Default.Thermostat,
            modifier = Modifier.weight(1f)
        )
        ModernInfoCard(
            label = "Air",
            value = liveData.iat.toInt().toString(),
            unit = "°C",
            icon = Icons.Default.Air,
            modifier = Modifier.weight(1f)
        )
        ModernInfoCard(
            label = "Ign",
            value = "%.1f".format(liveData.ignition),
            unit = "°",
            icon = AppIcons.SparkPlug,
            modifier = Modifier.weight(1f)
        )
        ModernInfoCard(
            label = "Inj",
            value = liveData.injector.replace(" ms", ""),
            unit = "",
            icon = AppIcons.Injector,
            modifier = Modifier.weight(1f)
        )
        ModernInfoCard(
            label = "Map",
            value = liveData.baseMap,
            unit = "",
            icon = AppIcons.Engine,
            modifier = Modifier.weight(1.1f)
        )

        Button(
            onClick = onSettings,
            modifier = Modifier.fillMaxHeight().weight(0.9f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(50.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Settings, null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text("Settings", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ModernInfoCard(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Black, RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(13.dp)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 15.sp
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(
                            text = unit,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = label,
                color = Color.Red,
                fontSize = 11.sp, // Maximized for readability
                fontWeight = FontWeight.Black,
                lineHeight = 11.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
