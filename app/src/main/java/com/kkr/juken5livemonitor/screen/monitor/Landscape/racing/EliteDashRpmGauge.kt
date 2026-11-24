package com.kkr.juken5livemonitor.screen.monitor.Landscape.racing

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.model.ECULiveData
import kotlin.math.roundToInt

@Composable
fun EliteDashRpmGauge(
    liveData: ECULiveData,
    runtime: String,
    modifier: Modifier = Modifier
) {
    val smoothRpm by animateFloatAsState(
        targetValue = liveData.rpm.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow * 0.5f
        ),
        label = "SmoothRpm"
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(130.dp)) { 
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val gaugeData = remember(widthPx, heightPx) {
            val path = Path().apply {
                moveTo(widthPx * 0.02f, heightPx * 0.85f) // Moved up
                quadraticTo(widthPx * 0.12f, heightPx * 0.08f, widthPx * 0.32f, heightPx * 0.08f) // Moved up
                lineTo(widthPx * 0.98f, heightPx * 0.08f)
            }
            val pm = PathMeasure()
            pm.setPath(path, false)
            val len = pm.length
            
            val nums = (1..12).map { i ->
                val progress = (i - 1) / 11f
                val pos = pm.getPosition(len * progress)
                // Position numbers below the inner arc with less offset to avoid clipping
                Triple(i.toString(), pos.x / widthPx, (pos.y + 35f) / heightPx)
            }
            
            Triple(path, len, nums)
        }
        
        val curvePath = gaugeData.first
        val pathLength = gaugeData.second
        val rpmNumbers = gaugeData.third

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = curvePath,
                color = Color.White,
                style = Stroke(width = 3f, cap = StrokeCap.Butt)
            )

            val totalTicks = 120 
            val pm = PathMeasure()
            pm.setPath(curvePath, false)
            
            for (i in 0..totalTicks) {
                val tickProgress = i.toFloat() / totalTicks
                val pos = pm.getPosition(pathLength * tickProgress)
                val isMainTick = i % 10 == 0
                val isMediumTick = i % 5 == 0
                val tickHeight = when {
                    isMainTick -> 14f
                    isMediumTick -> 10f
                    else -> 6f
                }
                
                // Redline starts from 10k: 100/120 * totalTicks
                val isRedline = i >= 100 
                val colorTick = if (isRedline) Color.Red else Color.White

                drawLine(
                    color = colorTick.copy(alpha = 0.5f),
                    start = Offset(pos.x, pos.y + 4f),
                    end = Offset(pos.x, pos.y + 4f + tickHeight),
                    strokeWidth = if (isMainTick) 4f else 1.5f
                )
                
                if (i < totalTicks) {
                    val nextPos = pm.getPosition(pathLength * (i + 1).toFloat() / totalTicks)
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(pos.x, pos.y + 40f),
                        end = Offset(nextPos.x, nextPos.y + 40f),
                        strokeWidth = 2.5f
                    )
                }
            }

            val progress = (smoothRpm / 12000f).coerceIn(0f, 1f)
            val activePath = Path()
            pm.getSegment(0f, pathLength * progress, activePath, true)
            
            drawPath(
                path = activePath,
                color = if (smoothRpm >= 10000) Color.Red else Color.White,
                style = Stroke(width = 36f, cap = StrokeCap.Butt),
                alpha = 1.0f
            )
        }

        rpmNumbers.forEach { (text, x, y) ->
            RpmNumber(text = text, x = x, y = y)
        }

        // --- Runtime Display ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 50.dp, end = 24.dp) 
                .wrapContentHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("RUNTIME", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(
                text = runtime,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun BoxScope.RpmNumber(text: String, x: Float, y: Float) {
    val isRed = text.toIntOrNull()?.let { it >= 10 } ?: false
    Text(
        text = text,
        color = if (isRed) Color.Red else Color.White,
        fontSize = 28.sp, // Slightly smaller to be safe
        fontWeight = FontWeight.Black,
        modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                val posX = (constraints.maxWidth * x) - (placeable.width / 2)
                val posY = (constraints.maxHeight * y)
                placeable.placeRelative(posX.roundToInt(), posY.roundToInt())
            }
        }
    )
}
