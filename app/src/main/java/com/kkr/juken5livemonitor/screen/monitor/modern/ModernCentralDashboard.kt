package com.kkr.juken5livemonitor.screen.monitor.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun ModernCentralDashboard(
    modifier: Modifier = Modifier,
    vm: MonitorViewModel
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(borderStroke(), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    )
}

private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
