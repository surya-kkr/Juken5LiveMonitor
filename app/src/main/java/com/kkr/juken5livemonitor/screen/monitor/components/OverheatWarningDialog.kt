package com.kkr.juken5livemonitor.screen.monitor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkr.juken5livemonitor.ui.components.GlassCard

@Composable
fun OverheatWarningDialog(
    visible: Boolean,
    currentTemp: Double,
    thresholdTemp: Int,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.85f),
        modifier = Modifier.fillMaxSize()
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "OverheatPulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.83f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .widthIn(max = 300.dp)
                    .wrapContentHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            Color(0xFFFF1744).copy(alpha = pulseAlpha),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Warning Icon with pulsing background glow
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF1744).copy(alpha = 0.45f * pulseAlpha),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    Color(0xFFFF1744).copy(alpha = pulseAlpha),
                                    RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Overheat Warning",
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "ENGINE OVERHEAT!",
                            fontSize = 30.sp,
                            lineHeight = 35.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF1744),
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Suhu melebihi batas aman (${thresholdTemp}°C). BANG UDAH BANG PANAS NIHH!",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray.copy(alpha = 0.95f),
                            textAlign = TextAlign.Center
                        )

                        // Temperature Status Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White.copy(alpha = 0.06f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SUHU SAAT INI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${currentTemp.toInt()}°C",
                                    fontSize = 30.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF1744)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "BATAS AMAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${thresholdTemp}°C",
                                    fontSize = 30.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF1744)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text(
                                text = "MENGERTI / TUTUP",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
