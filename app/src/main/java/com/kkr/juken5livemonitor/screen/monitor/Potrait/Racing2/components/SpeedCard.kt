package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardBlack
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardCardBorder

@Composable
fun SpeedCard(
    speed: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(130.dp)
            .background(DashboardBlack, RoundedCornerShape(12.dp))
            .border(1.5.dp, DashboardCardBorder, RoundedCornerShape(12.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = speed.toString(),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "SPEED",
                color = DashboardCardBorder,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
