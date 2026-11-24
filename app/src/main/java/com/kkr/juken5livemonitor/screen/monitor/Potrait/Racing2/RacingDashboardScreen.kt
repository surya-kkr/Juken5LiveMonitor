package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.components.*
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.model.DashboardData
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardBlack
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.RacingDashboardTheme

@Composable
fun RacingDashboardScreen(
    data: DashboardData,
    onDisconnect: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    RacingDashboardTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DashboardBlack
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                DashboardHeader(
                    onSettingsClick = onSettings,
                    onPowerClick = onDisconnect
                )

                RpmGauge(
                    rpm = data.rpm,
                    modifier = Modifier.offset(y = (-30).dp).size(280.dp)
                )

                // --- Main Body Group (Speed, Sensors, TPS) ---
                // Adjust this offset to move the entire lower section up/down
                Column(
                    modifier = Modifier.offset(y = (-130).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeedCard(
                        speed = data.speed
                    )

                    SensorGrid(
                        data = data
                    )

                    TpsBar(
                        tps = data.tps,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
