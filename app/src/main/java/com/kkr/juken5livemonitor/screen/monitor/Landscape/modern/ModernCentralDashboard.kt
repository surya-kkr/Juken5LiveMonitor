package com.kkr.juken5livemonitor.screen.monitor.Landscape.modern

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kkr.juken5livemonitor.screen.monitor.center.CenterMode
import com.kkr.juken5livemonitor.screen.monitor.center.CenterPanel
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun ModernCentralDashboard(
    modifier: Modifier = Modifier,
    vm: MonitorViewModel
) {
    val centerMode by vm.centerMode.collectAsState()

    Box(modifier = modifier) {
        CenterPanel(
            mode = centerMode,
            vm = vm
        )
    }
}