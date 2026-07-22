package com.kkr.juken5livemonitor.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.ui.components.GlassBackground
import com.kkr.juken5livemonitor.ui.components.GlassButton
import com.kkr.juken5livemonitor.ui.components.GlassCard
import com.kkr.juken5livemonitor.ui.components.GlassTopBar
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@Composable
fun MonitorScreen(
    navController: NavHostController,
    demoMode: Boolean = false,
    vm: MonitorViewModel = viewModel()
) {

    val realLiveData by vm.liveData.collectAsState()
    val realStatus by vm.status.collectAsState()
    val context = LocalContext.current
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                vm.startGPS(context)
            }

        }
    val liveData =
        if (demoMode) {

            com.kkr.juken5livemonitor.model.ECULiveData(
                rpm = 4250,
                tps = 34,
                battery = 13.92,
                afr = 13.8,
                eot = 86.5,
                iat = 30.4,
                map = 42,
                ignition = 18.5,
                injector = "2.35 ms",
                fuel = "Bensin Rakyat Jelata",
                baseMap = "Map 2"
            )

        } else {

            realLiveData

        }

    val status =
        if (demoMode)
            "Demo Dashboard"
        else
            realStatus

    LaunchedEffect(Unit) {

        if (!demoMode) {

            vm.startMonitor()

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                vm.startGPS(context)

            } else {

                locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

            }

        }

    }

    DisposableEffect(Unit) {

        onDispose {

            vm.stopGPS()

        }

    }

    GlassBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            GlassTopBar(
                connected = status.contains("Live", true)
            )

            Spacer(modifier = Modifier.height(20.dp))
            GlassCard {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "Connection Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = status,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            status.contains("Live", true) -> Color(0xFF00E676)
                            status.contains("Handshake", true) -> Color(0xFFFFD54F)
                            status.contains("Disconnected", true) -> Color(0xFFFF5252)
                            else -> Color.LightGray
                        }
                    )

                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "ENGINE RPM",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = liveData.rpm.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "RPM",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE53935)
                    )

                }

            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "TPS",
                    value = "${liveData.tps}%"
                )

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "AFR",
                    value = "%.1f".format(liveData.afr)
                )

            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Battery",
                    value = "%.2f V".format(liveData.battery)
                )

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Speed",
                    value = "${liveData.speed.toInt()} km/h"
                )

            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Engine Temp",
                    value = "%.1f °C".format(liveData.eot)
                )

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Air Temp",
                    value = "%.1f °C".format(liveData.iat)
                )

            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Ignition",
                    value = "%.1f°".format(liveData.ignition)
                )

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Injector",
                    value = liveData.injector
                )

            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Fuel",
                    value = liveData.fuel
                )

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Base Map",
                    value = liveData.baseMap
                )

            }

            Spacer(modifier = Modifier.height(28.dp))

            GlassButton(
                text = "DISCONNECT",
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    vm.disconnect()

                    navController.popBackStack()

                }
            )

            Spacer(modifier = Modifier.height(20.dp))

        }

    }

}
@Composable
private fun SensorCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {

    GlassCard(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.LightGray
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

        }

    }

}