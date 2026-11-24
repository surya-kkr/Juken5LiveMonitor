package com.kkr.juken5livemonitor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.R

@Composable
fun GlassTopBar(

    connected: Boolean = false,
    ecuName: String = "BRT Juken 5",
    subtitle: String? = null,
    inlineSubtitle: Boolean = false,
    onSettingsClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null

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
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            if (inlineSubtitle && subtitle != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = ecuName,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 19.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            } else {
                Text(
                    text = ecuName,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 19.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

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

        if (onSettingsClick != null) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (onHelpClick != null) {
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                    contentDescription = "Help",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

    }

}