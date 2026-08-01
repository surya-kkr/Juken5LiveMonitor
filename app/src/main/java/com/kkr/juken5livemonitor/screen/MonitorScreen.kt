package com.kkr.juken5livemonitor.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.basicMarquee
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
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.kkr.juken5livemonitor.screen.monitor.MonitorLandscape
import com.kkr.juken5livemonitor.screen.monitor.components.SettingsDialog

@Composable
fun MonitorScreen(
    navController: NavHostController,
    demoMode: Boolean = false,
    vm: MonitorViewModel = viewModel()
) {

    val liveData by vm.liveData.collectAsState()
    val status by vm.status.collectAsState()
    val bikeName by vm.bikeName.collectAsState()
    val selectedUi by vm.selectedUi.collectAsState()
    val vehicleWeight by vm.vehicleWeight.collectAsState()
    val riderWeight by vm.riderWeight.collectAsState()
    val drivetrainType by vm.drivetrainType.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                vm.startGPS(context)
            }

        }

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

        } else {
            vm.startDemoMode()
        }

    }

    DisposableEffect(Unit) {

        onDispose {

            vm.stopGPS()

        }

    }

    if (showSettings) {
        SettingsDialog(
            currentBikeName = bikeName,
            currentUi = selectedUi,
            currentVehicleWeight = vehicleWeight,
            currentRiderWeight = riderWeight,
            currentDrivetrain = drivetrainType,
            showUiSelection = false,
            onDismiss = { showSettings = false }
        ) { name, ui, vWeight, rWeight, drive ->
            vm.updateBikeName(name)
            vm.updateSelectedUi(ui)
            vm.updateVehicleSettings(vWeight, rWeight, drive)
            showSettings = false
        }
    }

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        MonitorLandscape(
            navController = navController,
            liveData = liveData,
            vm = vm
        )

        return

    }

    GlassBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            GlassTopBar(
                connected = status.contains("Live", true) || status == "Connected" || demoMode,
                ecuName = bikeName,
                onSettingsClick = { showSettings = true }
            )


            Spacer(modifier = Modifier.height(10.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

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
                    title = "Speed GPS",
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
                    title = "Core Active",
                    value = when (liveData.fuel) {
                        "0" -> "1"
                        "1" -> "2"
                        else -> liveData.fuel
                    }
                )

                SensorCard(
                    modifier = Modifier.weight(1f),
                    title = "Base Map",
                    value = liveData.baseMap
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

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
                color = Color.White,
                modifier = Modifier.basicMarquee()
            )

        }

    }

}
