package com.kkr.juken5livemonitor.screen.monitor.Landscape.racing

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

@Composable
fun RacingMainStats(
    speed: Int,
    rpm: Int,
    gear: String = "4"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Speed
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = speed.toString(),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 64.sp
            )
            Text(
                text = "KM/H",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Center: RPM (Digital)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = rpm.toString(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Text(
                text = "RPM",
                color = Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right: Gear (Giant)
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(Color.Black, RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gear,
                color = Color.White,
                fontSize = 84.sp,
                fontWeight = FontWeight.Black
            )
            // Label Gear
            Text(
                text = "GEAR",
                color = Color.Red,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            )
        }
    }
}
