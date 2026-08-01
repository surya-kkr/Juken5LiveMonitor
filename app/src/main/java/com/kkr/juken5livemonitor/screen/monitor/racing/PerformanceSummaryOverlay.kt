package com.kkr.juken5livemonitor.screen.monitor.racing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun PerformanceSummaryOverlay(
    result: MonitorViewModel.PerformanceResult,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .background(Color.Black, RoundedCornerShape(24.dp))
                .border(2.dp, Color.Red, RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "PERFORMANCE SUMMARY",
                color = Color.Red,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

            SummaryItem("ELAPSED TIME", result.time, Color.White)
            SummaryItem("AVG ACCELERATION", "%.2f m/s²".format(result.avgAccel), Color.LightGray)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SummaryItem("EST. ENGINE POWER", "%.1f HP".format(result.enginePower), Color.Green)
            SummaryItem("EST. WHEEL POWER", "%.1f HP".format(result.wheelPower), Color.Yellow)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CLOSE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}
