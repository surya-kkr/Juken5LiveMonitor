package com.kkr.juken5livemonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GlassTopBar(

    connected: Boolean = false,
    ecuName: String = "BRT Juken 5"

) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),

        verticalAlignment = Alignment.CenterVertically

    ) {

        Box(

            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (connected)
                        Color(0xFF00C853)
                    else
                        Color(0xFFE53935)
                ),

            contentAlignment = Alignment.Center

        ) {

            Icon(

                imageVector = Icons.Rounded.Bluetooth,

                contentDescription = null,

                tint = Color.White,

                modifier = Modifier.size(32.dp)

            )

        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(

                text = ecuName,

                style = MaterialTheme.typography.headlineSmall,

                color = Color.White,

                fontWeight = FontWeight.Bold

            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(

                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(

                            if (connected)
                                Color(0xFF00E676)
                            else
                                Color(0xFFFF5252)

                        )

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    text =
                        if (connected)
                            "CONNECTED"
                        else
                            "DISCONNECTED",

                    color =
                        if (connected)
                            Color(0xFF00E676)
                        else
                            Color.LightGray,

                    style = MaterialTheme.typography.bodyMedium

                )

            }

        }

    }

}