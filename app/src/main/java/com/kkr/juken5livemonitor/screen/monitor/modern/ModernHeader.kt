package com.kkr.juken5livemonitor.screen.monitor.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.ui.icons.AppIcons
import androidx.compose.material.icons.filled.AccessTime
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ModernHeader(
    bikeName: String,
    liveData: ECULiveData,
    ecuConnected: Boolean,
    gpsConnected: Boolean,
    bluetoothConnected: Boolean,
) {
    var currentTime by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp) // Compromise height to prevent cutting
            .padding(start = 4.dp, end = 64.dp), // Shifted further left
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = bikeName.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "BRT JUKEN 5++",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.sp,
                    lineHeight = 7.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1.8f) // Increased room for metrics
        ) {
            HeaderMetricItem(
                icon = Icons.Default.Speed,
                label = "SPEED",
                value = liveData.speed.toInt().toString(),
                unit = "km/h"
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            HeaderMetricItem(
                icon = AppIcons.Processor,
                label = "FUEL",
                value = when (liveData.fuel) {
                    "0" -> "1"
                    "1" -> "2"
                    else -> liveData.fuel
                },
                unit = ""
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1.3f)
        ) {
            Icon(Icons.Default.AccessTime, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            ModernStatusItem("GPS", gpsConnected)
            Spacer(modifier = Modifier.width(6.dp))
            ModernStatusItem("ECU", ecuConnected)
            Spacer(modifier = Modifier.width(6.dp))
            ModernStatusItem("BT", bluetoothConnected)
        }
    }
}

@Composable
private fun HeaderMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.basicMarquee()
    ) {
        Icon(
            imageVector = icon,
            null,
            tint = Color.Red,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.width(1.dp))
                Text(unit, color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp)
            }
            Text(
                text = label, 
                color = Color.Gray, 
                fontSize = 7.sp, 
                fontWeight = FontWeight.Bold,
                lineHeight = 7.sp
            )
        }
    }
}

@Composable
fun ModernStatusItem(label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (active) Color.Green else Color.Red, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )
        Text(label, color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}
