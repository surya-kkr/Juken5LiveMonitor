package com.kkr.juken5livemonitor.screen.monitor.kkrxag

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KkrRacingMainGauge(
    rpm: Int,
    gear: String,
    runtime: String,
    maxRpm: Int = 14000
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            val sX = w * 0.02f; val sY = h * 0.65f
            val cX = w * 0.12f; val cY = h * 0.20f
            val mX = w * 0.25f; val mY = h * 0.20f
            val eX = w * 0.98f; val eY = h * 0.20f

            val path = Path().apply {
                moveTo(sX, sY)
                quadraticTo(cX, cY, mX, mY)
                lineTo(eX, eY)
            }

            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.2f),
                style = Stroke(width = 12f)
            )

            for (i in 0..14) {
                val step = i / 14f
                val x = if (i <= 2) sX + (mX - sX) * (i / 2f) else mX + (eX - mX) * ((i - 2) / 12f)
                val y = if (i <= 2) sY - (sY - mY) * (i / 2f) else mY
                
                if (x <= eX) {
                    drawLine(Color.White, Offset(x, y - 6f), Offset(x, y + 6f), 2f)
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            for (i in 1..14) {
                val xP: Float; val yP: Float
                if (i == 1) { xP = 0.04f; yP = 0.55f }
                else if (i == 2) { xP = 0.18f; yP = 0.28f }
                else {
                    val step = (i - 3).toFloat() / 11f
                    xP = 0.28f + (step * 0.70f)
                    yP = 0.28f
                }
                Text(
                    text = i.toString(),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.align(Alignment.TopStart).offset(x = (850 * xP).dp, y = (220 * yP).dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = rpm.toString(),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = gear,
                color = Color.White,
                fontSize = 110.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(y = (-15).dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 50.dp, top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("RUNTIME", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = runtime, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }

        Box(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 45.dp, end = 15.dp).background(Color.Red).padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("RPM\nX1000", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, lineHeight = 11.sp)
        }
    }
}
