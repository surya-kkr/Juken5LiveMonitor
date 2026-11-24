package com.kkr.juken5livemonitor.screen.monitor.Landscape.modern

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
fun MonitorLandscapeModern(
    liveData: ECULiveData,
    vm: MonitorViewModel,
    onDisconnect: () -> Unit,
    onSettings: () -> Unit = {},
) {
    val status by vm.status.collectAsState()
    val bikeName by vm.bikeName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(4.dp),
    ) {
        ModernHeader(
            bikeName = bikeName,
            liveData = liveData,
            ecuConnected = status == "Live Connected" || status == "Demo Dashboard",
            gpsConnected = true,
            bluetoothConnected = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            ModernRpmBar(
                rpm = liveData.rpm
            )

            ModernCentralDashboard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                vm = vm
            )

            ModernTpsBar(
                tps = liveData.tps
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        ModernFooter(
            liveData = liveData,
            onDisconnect = onDisconnect,
            onSettings = onSettings
        )
    }
}
