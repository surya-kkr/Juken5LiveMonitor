package com.kkr.juken5livemonitor.screen.monitor.racing

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
import com.kkr.juken5livemonitor.model.ECULiveData

@Composable
fun RacingInfoGrid(
    liveData: ECULiveData
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RacingInfoBox(label = "BATTERY", value = "%.1fV".format(liveData.battery), modifier = Modifier.weight(1f))
            RacingInfoBox(label = "AFR", value = "%.1f".format(liveData.afr), modifier = Modifier.weight(1f))
            RacingInfoBox(label = "MAP", value = liveData.baseMap, modifier = Modifier.weight(1f))
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RacingInfoBox(label = "OIL TEMP", value = "${liveData.eot.toInt()}°C", modifier = Modifier.weight(1f))
            RacingInfoBox(label = "AIR TEMP", value = "${liveData.iat.toInt()}°C", modifier = Modifier.weight(1f))
            RacingInfoBox(label = "INJECTOR", value = liveData.injector.replace(" ms", ""), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun RacingInfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 18.sp
            )
            Text(
                text = label,
                color = Color.Red,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 9.sp
            )
        }
    }
}
