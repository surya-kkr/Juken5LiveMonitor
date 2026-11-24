package com.kkr.juken5livemonitor.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.kkr.juken5livemonitor.screen.monitor.MonitorLandscape
import com.kkr.juken5livemonitor.screen.monitor.components.SettingsDialog
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.RacingDashboardScreen
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.model.DashboardData
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing1.Racing1Dashboard
import com.kkr.juken5livemonitor.model.ECULiveData

@Composable
fun MonitorScreen(
    navController: NavHostController,
    demoMode: Boolean = false,
    vm: MonitorViewModel = viewModel()
) {

    val liveData by vm.liveData.collectAsState()
    val status by vm.status.collectAsState()
    val bikeName by vm.bikeName.collectAsState()
    val selectedUiPortrait by vm.selectedUiPortrait.collectAsState()
    val vehicleWeight by vm.vehicleWeight.collectAsState()
    val riderWeight by vm.riderWeight.collectAsState()
    val drivetrainType by vm.drivetrainType.collectAsState()
    val aaDpi by vm.aaDpi.collectAsState()
    val isRecording by vm.isRecording.collectAsState()
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

    LaunchedEffect(liveData.eot, overheatThreshold) {
        if (liveData.eot < overheatThreshold) {
            vm.resetOverheatDismiss()
        }
    }

    if (showSettings) {
        SettingsDialog(
            currentBikeName = bikeName,
            currentUi = selectedUiPortrait,
            currentDpi = aaDpi,
            currentVehicleWeight = vehicleWeight,
            currentRiderWeight = riderWeight,
            currentDrivetrain = drivetrainType,
            currentOverheatThreshold = overheatThreshold,
            currentAutoReconnect = autoReconnectEnabled,
            showUiSelection = true,
            allowedUis = listOf(0, 2),
            customLabels = mapOf(0 to "Racing 1", 2 to "Racing 2"),
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
            vm.updateSelectedUi(ui, false)
            vm.updateVehicleSettings(vWeight, rWeight, drive)
            vm.updateAaDpi(dpi)
            vm.updateOverheatThreshold(overheat)
            vm.updateAutoReconnect(autoRec)
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

    } else {
        // Automatically stop Android Auto when in Portrait (Themes Racing 1/2)
        LaunchedEffect(Unit) {
            vm.stopAndroidAuto()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Main Dashboard UI ---
        when (selectedUiPortrait) {
            2 -> {
                RacingDashboardScreen(
                    data = mapToDashboardData(liveData),
                    onDisconnect = {
                        vm.stopAndroidAuto()
                        vm.disconnect()
                        navController.popBackStack()
                    },
                    onSettings = { showSettings = true }
                )
            }
            else -> {
                Racing1Dashboard(
                    liveData = liveData,
                    status = status,
                    bikeName = bikeName,
                    demoMode = demoMode,
                    onSettingsClick = { showSettings = true },
                    onDisconnect = {
                        vm.stopAndroidAuto()
                        vm.disconnect()
                        navController.popBackStack()
                    }
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
                        .fillMaxWidth(0.7f)
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
                        .fillMaxWidth(0.7f)
                        .wrapContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync, // Using Sync as placeholder for BluetoothSearching
                            contentDescription = null,
                            tint = Color(0xFFFFA000), // Amber for reconnecting
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
                                .fillMaxWidth()
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

        // --- Start Recording Notification (Center Portrait) ---
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

        // --- Stop Recording Notification (Center Portrait) ---
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

        // --- Recording Indicator Overlay (Portrait) ---
        AnimatedVisibility(
            visible = isRecording,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
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
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = recordingDuration,
                            color = Color.Red,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            lineHeight = 16.sp
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
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(56.dp)
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

private fun mapToDashboardData(ecuData: ECULiveData): DashboardData {
    val injectorValue = try {
        ecuData.injector.replace(" ms", "").toFloat()
    } catch (e: Exception) {
        0f
    }

    return DashboardData(
        rpm = ecuData.rpm,
        speed = ecuData.speed.toInt(),
        afr = ecuData.afr.toFloat(),
        battery = ecuData.battery.toFloat(),
        eot = ecuData.eot.toInt(),
        ait = ecuData.iat.toInt(),
        map = ecuData.map,
        ignition = ecuData.ignition.toFloat(),
        injector = injectorValue,
        coreActive = when (ecuData.fuel) {
            "0" -> 1
            "1" -> 2
            else -> 1
        },
        tps = ecuData.tps.toFloat(),
        mapName = ecuData.baseMap
    )
}

