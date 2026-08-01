package com.kkr.juken5livemonitor.screen.monitor.kkrxag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.screen.monitor.racing.EliteDashBottomTelemetry
import com.kkr.juken5livemonitor.screen.monitor.racing.EliteDashRpmGauge
import com.kkr.juken5livemonitor.screen.monitor.racing.EliteDashTopBar
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun KkrxagMonitor(
    liveData: ECULiveData,
    vm: MonitorViewModel,
    onDisconnect: () -> Unit,
    onSettings: () -> Unit
) {
    val engineRuntime by vm.engineRuntime.collectAsState("00:00:00")
    val topSpeed by vm.topSpeed.collectAsState()
    val avgSpeed by vm.avgSpeed.collectAsState()
    val zeroToHundredResult by vm.zeroToHundredResult.collectAsState()
    val zeroToHundredStatus by vm.zeroToHundredStatus.collectAsState()
    val horsePower by vm.estHorsePower.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Outer padding removed for true fullscreen look
    ) {
        // --- 1. Top Section: Indicators & Logo ---
        EliteDashTopBar(
            liveData = liveData,
            onDisconnect = onDisconnect,
            onSettings = onSettings,
            modifier = Modifier.weight(0.8f) // Reduced to move RPM up
        )

        // --- 2. Middle Section: Curved RPM Arc & Runtime ---
        EliteDashRpmGauge(
            liveData = liveData,
            runtime = engineRuntime,
            modifier = Modifier.weight(1.8f) // Increased for more room
        )

        // --- 3. Bottom Section: Speed & Performance Grid ---
        EliteDashBottomTelemetry(
            liveData = liveData,
            topSpeed = topSpeed,
            avgSpeed = avgSpeed,
            zeroToHundred = zeroToHundredResult,
            zeroToHundredStatus = zeroToHundredStatus,
            estHp = horsePower,
            onArmTimer = { vm.armZeroToHundred() },
            modifier = Modifier.weight(2.6f)
        )
    }
}
