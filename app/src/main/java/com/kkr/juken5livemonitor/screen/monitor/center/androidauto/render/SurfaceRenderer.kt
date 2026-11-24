package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SurfaceContainer(
    modifier: Modifier = Modifier,
    // Parameter metrics bisa dihapus atau diabaikan jika tidak ada overlay UI Compose lain
    content: @Composable () -> Unit
) {
    // Sekarang hanya menjadi wadah murni untuk AndroidAutoSurface
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        content()
    }
}