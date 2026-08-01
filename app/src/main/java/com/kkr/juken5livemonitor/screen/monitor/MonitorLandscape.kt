package com.kkr.juken5livemonitor.screen.monitor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.screen.monitor.classic.ClassicFooter
import com.kkr.juken5livemonitor.screen.monitor.classic.ClassicHeader
import com.kkr.juken5livemonitor.screen.monitor.classic.ClassicRpmBar
import com.kkr.juken5livemonitor.screen.monitor.classic.ClassicTpsBar
import com.kkr.juken5livemonitor.screen.monitor.components.SettingsDialog
import com.kkr.juken5livemonitor.screen.monitor.kkrxag.KkrxagMonitor
import com.kkr.juken5livemonitor.screen.monitor.modern.MonitorLandscapeModern
import com.kkr.juken5livemonitor.screen.monitor.racing.PerformanceSummaryOverlay
import com.kkr.juken5livemonitor.screen.monitor.racing.RacingMonitor
import com.kkr.juken5livemonitor.ui.components.GlassBackground
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel
import com.kkr.juken5livemonitor.screen.monitor.center.CenterMode

import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun MonitorLandscape(
    navController: NavHostController,
    liveData: ECULiveData,
    vm: MonitorViewModel,
) {

    val currentLocation by vm.currentLocation.collectAsState()
    val bearing by vm.bearing.collectAsState()
    val status by vm.status.collectAsState()
    val bikeName by vm.bikeName.collectAsState()
    val selectedUi by vm.selectedUi.collectAsState()
    val centerMode by vm.centerMode.collectAsState()

    val vehicleWeight by vm.vehicleWeight.collectAsState()
    val riderWeight by vm.riderWeight.collectAsState()
    val drivetrainType by vm.drivetrainType.collectAsState()
    
    val zeroToHundredStatus by vm.zeroToHundredStatus.collectAsState()
    val countdownValue by vm.countdownValue.collectAsState()

    var showSettings by remember { mutableStateOf(value = false) }

    if (showSettings) {
        SettingsDialog(
            currentBikeName = bikeName,
            currentUi = selectedUi,
            currentVehicleWeight = vehicleWeight,
            currentRiderWeight = riderWeight,
            currentDrivetrain = drivetrainType,
            showUiSelection = true,
            onDismiss = { showSettings = false }
        ) { name, ui, vWeight, rWeight, drive ->
            vm.updateBikeName(name)
            vm.updateSelectedUi(ui)
            vm.updateVehicleSettings(vWeight, rWeight, drive)
            showSettings = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Main Dashboard UI ---
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedUi) {
                1 -> {
                    MonitorLandscapeModern(
                        liveData = liveData,
                        vm = vm,
                        onDisconnect = {
                            vm.disconnect()
                            navController.popBackStack()
                        }
                    ) { showSettings = true }
                }
                2 -> {
                    RacingMonitor(
                        liveData = liveData,
                        vm = vm,
                        onDisconnect = {
                            vm.disconnect()
                            navController.popBackStack()
                        }
                    ) { showSettings = true }
                }
                3 -> {
                    KkrxagMonitor(
                        liveData = liveData,
                        vm = vm,
                        onDisconnect = {
                            vm.disconnect()
                            navController.popBackStack()
                        }
                    ) { showSettings = true }
                }
                else -> {
                    GlassBackground {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        ) {
                            ClassicHeader(
                                liveData = liveData,
                                bikeName = bikeName,
                                ecuConnected = status == "Connected",
                                gpsConnected = true,
                                bluetoothConnected = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(68.dp)
                                        .fillMaxHeight()
                                ) {
                                    ClassicRpmBar(
                                        rpm = liveData.rpm
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )

                                Box(
                                    modifier = Modifier
                                        .width(68.dp)
                                        .fillMaxHeight()
                                ) {
                                    ClassicTpsBar(
                                        tps = liveData.tps
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            ClassicFooter(
                                liveData = liveData,
                                onDisconnect = {
                                    vm.disconnect()
                                    navController.popBackStack()
                                },
                                onSettings = { showSettings = true }
                            )
                        }
                    }
                }
            }
        }

        if ((zeroToHundredStatus == MonitorViewModel.TimerStatus.COUNTDOWN) || countdownValue.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = countdownValue,
                    color = Color.White,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

    }
}
