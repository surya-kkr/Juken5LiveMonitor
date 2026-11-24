package com.kkr.juken5livemonitor.screen.monitor.center

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kkr.juken5livemonitor.screen.monitor.center.androidauto.AndroidAutoPanel
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun CenterPanel(
    mode: CenterMode,
    vm: MonitorViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
    ) {
        when (mode) {
            CenterMode.AndroidAuto -> {
                AndroidAutoPanel(vm = vm)
            }
            else -> {
                // Placeholder for other modes if needed
            }
        }
    }
}