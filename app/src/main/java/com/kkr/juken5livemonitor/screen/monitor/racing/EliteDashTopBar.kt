package com.kkr.juken5livemonitor.screen.monitor.racing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.R
import com.kkr.juken5livemonitor.model.ECULiveData

@Composable
fun EliteDashTopBar(
    liveData: ECULiveData,
    onDisconnect: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp), // Fixed height removed to let weight handle it
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left: TPS & AFR ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val tpsColor = when {
                liveData.tps < 75 -> Color.Green
                liveData.tps <= 90 -> Color.Yellow
                else -> Color.Red
            }

            TopIndicator(
                label = "TPS",
                valueText = "${liveData.tps}%",
                progress = liveData.tps / 100f,
                color = tpsColor
            )

            val afrColor = when {
                liveData.afr < 11.5 || liveData.afr > 16.0 -> Color.Red
                liveData.afr < 12.5 || liveData.afr > 15.0 -> Color.Yellow
                else -> Color.Green
            }

            TopIndicator(
                label = "AFR",
                valueText = "%.1f Ratio".format(liveData.afr),
                progress = ((liveData.afr.toFloat() - 10f) / 10f).coerceIn(0f, 1f),
                color = afrColor
            )
        }

        // --- Center: Power, Logo, Settings ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onDisconnect,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Disconnect",
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.logo_kkr_ag),
                contentDescription = "KKR AG Logo",
                modifier = Modifier.height(45.dp).padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // --- Right: Battery & Engine Temp ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val batteryColor = when {
                liveData.battery < 12.0 -> Color.Red
                liveData.battery <= 14.5 -> Color.Green
                else -> Color.Red
            }

            TopIndicator(
                label = "BATTERY VOLTAGE",
                valueText = "%.1f V".format(liveData.battery),
                progress = ((liveData.battery.toFloat() - 9f) / 7f).coerceIn(0f, 1f),
                color = batteryColor
            )

            val tempColor = when {
                liveData.eot < 85 -> Color.Green
                liveData.eot <= 95 -> Color.Yellow
                else -> Color.Red
            }

            TopIndicator(
                label = "ENGINE TEMP",
                valueText = "${liveData.eot.toInt()} °C",
                progress = ((liveData.eot.toFloat() - 40f) / 100f).coerceIn(0f, 1f),
                color = tempColor
            )
        }
    }
}

@Composable
private fun TopIndicator(
    label: String,
    valueText: String,
    progress: Float,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.Red,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(10.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(1.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(1.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = valueText,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
