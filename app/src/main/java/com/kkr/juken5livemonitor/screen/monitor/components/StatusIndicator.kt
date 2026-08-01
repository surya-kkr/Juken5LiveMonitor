package com.kkr.juken5livemonitor.screen.monitor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusIndicator(
    title: String,
    connected: Boolean
) {

    val indicatorColor =
        if (connected)
            Color(0xFF00E676)
        else
            Color(0xFFFF5252)

    Row(
        modifier = Modifier
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.10f),
                RoundedCornerShape(50)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.Center

    ) {

        Box(
            modifier = Modifier
                .size(9.dp)
                .background(
                    indicatorColor,
                    CircleShape
                )
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

    }

}