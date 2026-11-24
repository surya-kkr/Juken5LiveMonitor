package com.kkr.juken5livemonitor.screen.monitor.Landscape.racing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkr.juken5livemonitor.model.ECULiveData

import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun RacingMonitor(
    liveData: ECULiveData,
    vm: MonitorViewModel,
    onDisconnect: () -> Unit,
    onSettings: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(4.dp)
    ) {
        // --- 1. Top Section: Indicators & Logo (Large) ---
        EliteDashTopBar(
            liveData = liveData,
            onDisconnect = onDisconnect,
            onSettings = onSettings,
            modifier = Modifier.weight(1.1f)
        )

        // --- 2. Middle Section: Horizontal Rulers & Fuel Mode (Large) ---
        EliteDashHorizontalGauges(
            liveData = liveData,
            modifier = Modifier.weight(1.4f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // --- 3. Bottom Section: 10-Item Telemetry Grid (2 Rows) ---
        EliteDashTelemetryGrid(
            liveData = liveData,
            vm = vm,
            modifier = Modifier.weight(2.0f)
        )
    }
}
