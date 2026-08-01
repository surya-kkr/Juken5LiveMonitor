package com.kkr.juken5livemonitor.screen.monitor.kkrxag

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData

@Composable
fun KkrRacingBottomGrid(
    liveData: ECULiveData,
    speed: Int,
    zeroToHundred: String,
    hp: String,
    topSpeed: Int,
    avgSpeed: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(80.dp).align(Alignment.TopCenter)) {
            val w = size.width
            val h = size.height
            val lineY = h * 0.1f
            val dropY = h * 0.9f
            
            drawLine(Color.Red, Offset(w * 0.05f, lineY), Offset(w * 0.35f, lineY), 4f)
            drawLine(Color.Red, Offset(w * 0.35f, lineY), Offset(w * 0.35f, dropY), 4f)
            
            drawLine(Color.Red, Offset(w * 0.65f, lineY), Offset(w * 0.95f, lineY), 4f)
            drawLine(Color.Red, Offset(w * 0.65f, lineY), Offset(w * 0.65f, dropY), 4f)
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(30.dp), modifier = Modifier.padding(bottom = 5.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    GridValue("OIL PRES", "43.3", "")
                    GridValue("OIL TEMP", "0.0", "°C")
                }
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    GridValue("INTAKE AIR", "${liveData.iat.toInt()}", "°C")
                    GridValue("AIR TEMP", "${liveData.iat.toInt() + 2}", "°C")
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 0.dp)
            ) {
                Text("SPEED", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(
                    text = "%03d".format(speed),
                    color = Color.White,
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 100.sp,
                    letterSpacing = 4.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(30.dp), modifier = Modifier.padding(bottom = 5.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    GridValue("0-100 KM/H", zeroToHundred, "s")
                    GridValue("TOP SPEED", "$topSpeed", "kpj")
                }
                Column(verticalArrangement = Arrangement.spacedBy(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    GridValue("EST. HP", hp, "HP")
                    GridValue("AVG SPEED", "$avgSpeed", "kpj")
                }
            }
        }
    }
}

@Composable
private fun GridValue(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black, lineHeight = 38.sp)
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(unit, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}
