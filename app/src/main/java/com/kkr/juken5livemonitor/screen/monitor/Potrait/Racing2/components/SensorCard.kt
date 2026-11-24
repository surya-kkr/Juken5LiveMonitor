package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardBlack
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardCardBorder

@Composable
fun SensorCard(
    name: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp)
            .background(DashboardBlack, RoundedCornerShape(12.dp))
            .border(1.2.dp, DashboardCardBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        // 1. Label (Top Left)
        Text(
            text = name.uppercase(),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // 2. Body (Icon + Value + Unit)
        Row(
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Value + Unit (Horizontal)
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alignByBaseline()
                )
                
                Spacer(modifier = Modifier.width(3.dp))
                
                Text(
                    text = unit,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.alignByBaseline().padding(bottom = 4.dp)
                )
            }
        }
    }
}
