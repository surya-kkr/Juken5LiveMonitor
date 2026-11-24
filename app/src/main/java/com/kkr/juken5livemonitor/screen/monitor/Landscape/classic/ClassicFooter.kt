package com.kkr.juken5livemonitor.screen.monitor.Landscape.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.screen.monitor.components.InfoCard
import com.kkr.juken5livemonitor.ui.icons.AppIcons

@Composable
fun ClassicFooter(
    liveData: ECULiveData,
    onDisconnect: () -> Unit = {},
    onSettings: () -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.10f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 4.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = onDisconnect,
                modifier = Modifier.height(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE00000)
                )
            ) {

                Icon(
                    Icons.Default.PowerSettingsNew,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(
                    text = "Disconnect",
                    style = MaterialTheme.typography.labelSmall
                )

            }

            Spacer(modifier = Modifier.width(4.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                InfoCard(
                    title = "AFR",
                    value = "%.1f".format(liveData.afr),
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Batt",
                    value = "%.1fV".format(liveData.battery),
                    icon = Icons.Default.BatteryChargingFull,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Oil",
                    value = "${liveData.eot.toInt()}°C",
                    icon = Icons.Default.Thermostat,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Air",
                    value = "${liveData.iat.toInt()}°C",
                    icon = Icons.Default.Air,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Ign",
                    value = "%.1f°".format(liveData.ignition),
                    icon = AppIcons.SparkPlug,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Inj",
                    value = liveData.injector,
                    icon = AppIcons.Injector,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Map",
                    value = liveData.baseMap,
                    icon = AppIcons.Engine,
                    modifier = Modifier.weight(1f)
                )

            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = onSettings,
                modifier = Modifier.height(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE00000)
                )
            ) {

                Icon(
                    Icons.Default.Settings,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelSmall
                )

            }

        }

    }

}
