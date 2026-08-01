package com.kkr.juken5livemonitor.screen.monitor.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.screen.monitor.components.StatusIndicator
import com.kkr.juken5livemonitor.ui.icons.AppIcons
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClassicHeader(
    liveData: ECULiveData = ECULiveData(),
    location: String = "Bekasi",
    bikeName: String = "YAMAHA R25",
    ecuConnected: Boolean = true,
    gpsConnected: Boolean = true,
    bluetoothConnected: Boolean = true
) {

    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())

            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.10f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Default.DirectionsBike,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy((-10).dp)
        ) {

            Text(
                text = bikeName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "JUKEN 5 LIVE MONITOR",
                color = Color.White.copy(alpha = .55f),
                fontSize = 8.sp
            )

        }

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            Icons.Default.LocationOn,
            null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = location,
            color = Color.White,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            Icons.Default.AccessTime,
            null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = AppIcons.Processor,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(3.dp))

        val displayFuel = when (liveData.fuel) {
            "0" -> "1"
            "1" -> "2"
            else -> liveData.fuel
        }

        Text(
            text = displayFuel,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = liveData.speed.toInt().toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = "km/h",
                color = Color.White.copy(alpha = .65f),
                fontSize = 15.sp
            )

        }

        Spacer(modifier = Modifier.width(10.dp))

        StatusIndicator(
            title = "GPS",
            connected = gpsConnected
        )

        Spacer(modifier = Modifier.width(4.dp))

        StatusIndicator(
            title = "ECU",
            connected = ecuConnected
        )

        Spacer(modifier = Modifier.width(4.dp))

        StatusIndicator(
            title = "BT",
            connected = bluetoothConnected
        )

    }

}
