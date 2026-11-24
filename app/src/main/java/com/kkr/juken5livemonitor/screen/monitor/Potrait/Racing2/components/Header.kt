package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kkr.juken5livemonitor.R
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardPrimary

@Composable
fun DashboardHeader(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPowerClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPowerClick) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Power",
                tint = DashboardPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.logo_kkr_ag),
            contentDescription = "KKRXAG Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(60.dp)
                .clickable { onSettingsClick() }
        )

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
