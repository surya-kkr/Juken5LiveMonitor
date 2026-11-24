package com.kkr.juken5livemonitor.screen.monitor

import androidx.compose.foundation.clickable
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.screen.monitor.Landscape.classic.ClassicFooter
import com.kkr.juken5livemonitor.screen.monitor.Landscape.classic.ClassicHeader
import com.kkr.juken5livemonitor.screen.monitor.Landscape.classic.ClassicRpmBar
import com.kkr.juken5livemonitor.screen.monitor.Landscape.classic.ClassicTpsBar
import com.kkr.juken5livemonitor.screen.monitor.center.CenterPanel
import com.kkr.juken5livemonitor.screen.monitor.components.SettingsDialog
import com.kkr.juken5livemonitor.screen.monitor.Landscape.kkrxag.KkrxagMonitor
import com.kkr.juken5livemonitor.screen.monitor.Landscape.modern.MonitorLandscapeModern
import com.kkr.juken5livemonitor.screen.monitor.Landscape.racing.RacingMonitor
import com.kkr.juken5livemonitor.ui.components.GlassBackground
import com.kkr.juken5livemonitor.ui.components.GlassCard
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun MonitorLandscape(
    navController: NavHostController,
    liveData: ECULiveData,
    vm: MonitorViewModel,
) {

    val status by vm.status.collectAsState()
    val bikeName by vm.bikeName.collectAsState()
    val selectedUiLandscape by vm.selectedUiLandscape.collectAsState()
    val aaDpi by vm.aaDpi.collectAsState()
    val phoneName by vm.connectedPhoneName.collectAsState()
    val centerMode by vm.centerMode.collectAsState()

    val vehicleWeight by vm.vehicleWeight.collectAsState()
    val riderWeight by vm.riderWeight.collectAsState()
    val drivetrainType by vm.drivetrainType.collectAsState()
    val isRecording by vm.isRecording.collectAsState()
    
    val zeroToHundredStatus by vm.zeroToHundredStatus.collectAsState()
    val countdownValue by vm.countdownValue.collectAsState()
    val recordingDuration by vm.recordingDuration.collectAsState()
    val showRecordStartPopup by vm.showRecordStartPopup.collectAsState()
    val showRecordStopPopup by vm.showRecordStopPopup.collectAsState()
    val lastSavedLogTitle by vm.lastSavedLogTitle.collectAsState()
    val syncStatus by vm.syncStatus.collectAsState()
    val reconnectingStatus by vm.reconnectingStatus.collectAsState()
    val showDownloadFailed by vm.showDownloadFailed.collectAsState()
    val overheatThreshold by vm.overheatThreshold.collectAsState()
    val isOverheatDismissed by vm.isOverheatDismissed.collectAsState()
    val autoReconnectEnabled by vm.autoReconnectEnabled.collectAsState()
    val showDisconnectedPopup by vm.showDisconnectedPopup.collectAsState()

    var showSettings by remember { mutableStateOf(value = false) }

    LaunchedEffect(liveData.eot, overheatThreshold) {
        if (liveData.eot < overheatThreshold) {
            vm.resetOverheatDismiss()
        }
    }

    LaunchedEffect(selectedUiLandscape) {
        if (selectedUiLandscape == 2 || selectedUiLandscape == 3) {
            Log.i("AA_LIFECYCLE", "Switching to non-AA theme ($selectedUiLandscape). Stopping Projection.")
            vm.stopAndroidAuto()
        }
    }

    if (showSettings) {
        SettingsDialog(
            currentBikeName = bikeName,
            currentUi = selectedUiLandscape,
            currentDpi = aaDpi,
            currentVehicleWeight = vehicleWeight,
            currentRiderWeight = riderWeight,
            currentDrivetrain = drivetrainType,
            currentOverheatThreshold = overheatThreshold,
            currentAutoReconnect = autoReconnectEnabled,
            showUiSelection = true,
            isRecording = isRecording,
            onStartRecording = { vm.startRecording() },
            onStopRecording = { vm.stopRecording() },
            onDismiss = { showSettings = false },
            onViewLogs = {
                showSettings = false
                navController.navigate("log_history")
            }
        ) { name, ui, vWeight, rWeight, drive, dpi, overheat, autoRec ->
            vm.updateBikeName(name)
            vm.updateSelectedUi(ui, true)
            vm.updateVehicleSettings(vWeight, rWeight, drive)
            vm.updateAaDpi(dpi)
            vm.updateOverheatThreshold(overheat)
            vm.updateAutoReconnect(autoRec)
            showSettings = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Main Dashboard UI ---
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedUiLandscape) {
                1 -> {
                    MonitorLandscapeModern(
                        liveData = liveData,
                        vm = vm,
                        onDisconnect = {
                            vm.stopAndroidAuto()
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
                            vm.stopAndroidAuto()
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
                            vm.stopAndroidAuto()
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
                                phoneName = phoneName,
                                ecuConnected = status == "Live Connected" || status == "Demo Dashboard",
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
                                ) {
                                    CenterPanel(
                                        mode = centerMode,
                                        vm = vm
                                    )
                                }

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
                                    vm.stopAndroidAuto()
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
                Text(
                    text = countdownValue,
                    color = Color.White,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // --- Sync Loading Popup ---
        AnimatedVisibility(
            visible = syncStatus != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .wrapContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(40.dp)
                        )

                        Text(
                            text = "Connecting to ECU",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Tunggu proses ini selesai",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        syncStatus?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color(0xFF00E676),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }

        // --- Reconnecting Popup ---
        AnimatedVisibility(
            visible = reconnectingStatus != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .wrapContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(40.dp)
                        )

                        Text(
                            text = "Reconnecting to ECU",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        reconnectingStatus?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color(0xFFFFA000),
                            strokeWidth = 3.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { vm.cancelReconnect() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(38.dp)
                        ) {
                            Text(
                                text = "BATALKAN RECONNECT",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- Start Recording Notification (Center) ---
        AnimatedVisibility(
            visible = showRecordStartPopup,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GlassCard(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Start Recording Log",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Record Started",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // --- Stop Recording Notification (Center) ---
        AnimatedVisibility(
            visible = showRecordStopPopup,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GlassCard(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SdCard,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Log Saved!",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = lastSavedLogTitle,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // --- Recording Indicator Overlay ---
        AnimatedVisibility(
            visible = isRecording,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                // Shift to the left on Racing/KKRXAG themes to appear near disconnect button
                .offset(x = if (selectedUiLandscape == 2 || selectedUiLandscape == 3) (-165).dp else 0.dp)
                .clickable { vm.stopRecording() }
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(12.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Recording Log",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = recordingDuration,
                            color = Color.Red,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // --- Download Failed Popup ---
        AnimatedVisibility(
            visible = showDownloadFailed,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { vm.dismissDownloadFailed() },
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .wrapContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Download Data Gagal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Koneksi terputus atau ECU tidak merespon saat pengambilan data.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { vm.dismissDownloadFailed() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TUTUP", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Engine Overheat Warning Popup ---
        val isOverheat = liveData.eot >= overheatThreshold && !isOverheatDismissed && (status == "Live Connected" || status == "Demo Dashboard")
        com.kkr.juken5livemonitor.screen.monitor.components.OverheatWarningDialog(
            visible = isOverheat,
            currentTemp = liveData.eot,
            thresholdTemp = overheatThreshold,
            onDismiss = { vm.dismissOverheatWarning() }
        )

        // --- ECU Disconnected Manual Reconnect Popup ---
        com.kkr.juken5livemonitor.screen.monitor.components.EcuDisconnectedDialog(
            visible = showDisconnectedPopup,
            onReconnect = { vm.triggerManualReconnect() },
            onDismiss = { vm.dismissDisconnectedPopup() }
        )

    }
}
