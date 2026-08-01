package com.kkr.juken5livemonitor.screen.monitor.kkrxag

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.R
import com.kkr.juken5livemonitor.model.ECULiveData

@Composable
fun KkrRacingTopBar(
    liveData: ECULiveData,
    onDisconnect: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left: TPS & AFR ---
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IndicatorBlock("TPS", "${liveData.tps}%", liveData.tps / 100f)
            IndicatorBlock("AFR", "%.1f".format(liveData.afr), ((liveData.afr.toFloat() - 10f) / 10f).coerceIn(0f, 1f))
        }

        // --- Center: Logo ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDisconnect, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color.Red, modifier = Modifier.size(22.dp))
            }
            
            Image(
                painter = painterResource(id = R.drawable.logo_kkr_ag),
                contentDescription = null,
                modifier = Modifier.height(42.dp).padding(horizontal = 4.dp)
            )

            IconButton(onClick = onSettings, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Settings, null, tint = Color.Red, modifier = Modifier.size(22.dp))
            }
        }

        // --- Right: BATT & TEMP ---
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IndicatorBlock("BATT", "%.1f V".format(liveData.battery), ((liveData.battery.toFloat() - 9f) / 7f).coerceIn(0f, 1f))
            IndicatorBlock("TEMP", "${liveData.eot.toInt()}°C", ((liveData.eot.toFloat() - 40f) / 100f).coerceIn(0f, 1f))
        }
    }
}

@Composable
private fun IndicatorBlock(label: String, value: String, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Red, fontSize = 7.sp, fontWeight = FontWeight.Black)
        Box(
            modifier = Modifier
                .width(65.dp)
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color.White)
            )
        }
        Text(value, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
