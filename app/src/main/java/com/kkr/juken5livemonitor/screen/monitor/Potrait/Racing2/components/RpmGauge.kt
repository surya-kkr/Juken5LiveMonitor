package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.theme.DashboardPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RpmGauge(
    rpm: Int,
    modifier: Modifier = Modifier,
    maxRpm: Int = 10000,
    redlineRpm: Int = 8000
) {
    Box(
        modifier = modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val innerRadius = size.width / 2 - 50.dp.toPx()
            val tickCount = 60
            val startAngle = 150f
            val sweepAngle = 240f

            // --- 1. Draw RPM Ticks ---
            for (i in 0..tickCount) {
                val angleInDegrees = startAngle + (i.toFloat() / tickCount) * sweepAngle
                val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
                
                val isRedline = (i.toFloat() / tickCount) * maxRpm >= redlineRpm
                val isActive = (i.toFloat() / tickCount) * maxRpm <= rpm

                val tickLength = if (i % 5 == 0) 22.dp.toPx() else 14.dp.toPx()
                val strokeWidth = if (isActive) 4.dp.toPx() else 2.dp.toPx()
                val color = when {
                    isActive && isRedline -> DashboardPrimary
                    isActive -> Color.White
                    isRedline -> DashboardPrimary.copy(alpha = 0.3f)
                    else -> Color.White.copy(alpha = 0.3f)
                }

                val start = Offset(
                    (center.x + (innerRadius - tickLength) * cos(angleInRadians)).toFloat(),
                    (center.y + (innerRadius - tickLength) * sin(angleInRadians)).toFloat()
                )
                val end = Offset(
                    (center.x + innerRadius * cos(angleInRadians)).toFloat(),
                    (center.y + innerRadius * sin(angleInRadians)).toFloat()
                )

                drawLine(
                    color = color,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // --- 2. Precision White Brackets ---
            val bracketRadius = innerRadius + 20.dp.toPx()
            
            fun drawSharpBracket(isLeft: Boolean) {
                val startA = if (isLeft) 150f else 330f
                val sweepA = 60f
                val endA = startA + sweepA
                
                val path = Path().apply {
                    // Top Tip (Inwards Triangle)
                    val topAngleRad = Math.toRadians(startA.toDouble())
                    val topTip = Offset(
                        (center.x + bracketRadius * cos(topAngleRad)).toFloat(),
                        (center.y + bracketRadius * sin(topAngleRad)).toFloat()
                    )
                    val topInner = Offset(
                        (center.x + (bracketRadius - 15.dp.toPx()) * cos(topAngleRad)).toFloat(),
                        (center.y + (bracketRadius - 15.dp.toPx()) * sin(topAngleRad)).toFloat()
                    )
                    val topOuter = Offset(
                        (center.x + (bracketRadius + 10.dp.toPx()) * cos(Math.toRadians((startA + 15).toDouble()))).toFloat(),
                        (center.y + (bracketRadius + 10.dp.toPx()) * sin(Math.toRadians((startA + 15).toDouble()))).toFloat()
                    )
                    
                    moveTo(topInner.x, topInner.y)
                    lineTo(topTip.x, topTip.y)
                    lineTo(topOuter.x, topOuter.y)
                    
                    // Arc body
                    arcTo(
                        rect = Rect(center.x - bracketRadius, center.y - bracketRadius, center.x + bracketRadius, center.y + bracketRadius),
                        startAngleDegrees = startA + 15f,
                        sweepAngleDegrees = sweepA - 30f,
                        forceMoveTo = false
                    )
                    
                    // Bottom Tip
                    val botAngleRad = Math.toRadians(endA.toDouble())
                    val botTip = Offset(
                        (center.x + bracketRadius * cos(botAngleRad)).toFloat(),
                        (center.y + bracketRadius * sin(botAngleRad)).toFloat()
                    )
                    val botInner = Offset(
                        (center.x + (bracketRadius - 15.dp.toPx()) * cos(botAngleRad)).toFloat(),
                        (center.y + (bracketRadius - 15.dp.toPx()) * sin(botAngleRad)).toFloat()
                    )
                    val botOuter = Offset(
                        (center.x + (bracketRadius + 10.dp.toPx()) * cos(Math.toRadians((endA - 15).toDouble()))).toFloat(),
                        (center.y + (bracketRadius + 10.dp.toPx()) * sin(Math.toRadians((endA - 15).toDouble()))).toFloat()
                    )
                    
                    lineTo(botOuter.x, botOuter.y)
                    lineTo(botTip.x, botTip.y)
                    lineTo(botInner.x, botInner.y)
                }
                drawPath(path = path, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
            
            drawSharpBracket(true)
            drawSharpBracket(false)

            // --- 3. Red Peaked Chevrons ---
            val redRadius = bracketRadius + 20.dp.toPx()
            val peakOffset = 15.dp.toPx()
            
            fun drawRedChevron(isLeft: Boolean) {
                val baseAngle = if (isLeft) 180f else 0f
                val span = 45f
                
                val path = Path().apply {
                    val startAngleRad = Math.toRadians((baseAngle - span).toDouble())
                    val endAngleRad = Math.toRadians((baseAngle + span).toDouble())
                    val peakAngleRad = Math.toRadians(baseAngle.toDouble())
                    
                    val startP = Offset(
                        (center.x + redRadius * cos(startAngleRad)).toFloat(),
                        (center.y + redRadius * sin(startAngleRad)).toFloat()
                    )
                    val endP = Offset(
                        (center.x + redRadius * cos(endAngleRad)).toFloat(),
                        (center.y + redRadius * sin(endAngleRad)).toFloat()
                    )
                    val peakP = Offset(
                        (center.x + (redRadius + peakOffset) * cos(peakAngleRad)).toFloat(),
                        (center.y + (redRadius + peakOffset) * sin(peakAngleRad)).toFloat()
                    )
                    
                    moveTo(startP.x, startP.y)
                    lineTo(peakP.x, peakP.y)
                    lineTo(endP.x, endP.y)
                    
                    // Horizontal Pointer from peak
                    val pointerEnd = if (isLeft) peakP.x - 20.dp.toPx() else peakP.x + 20.dp.toPx()
                    moveTo(peakP.x, peakP.y)
                    lineTo(pointerEnd, peakP.y)
                }
                drawPath(path = path, color = DashboardPrimary, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            }
            
            drawRedChevron(true)
            drawRedChevron(false)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-20).dp)
        ) {
            Text(
                text = "RPM",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = rpm.toString(),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
