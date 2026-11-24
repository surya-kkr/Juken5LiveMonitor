package com.kkr.juken5livemonitor.screen.Connect.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kkr.juken5livemonitor.ui.components.GlassCard

@Composable
fun ConnectionHelpDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .size(420.dp) // Force square shape (Width = Height)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "🔵 Panduan Koneksi ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, // Extra bold
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f) // Take remaining space for scroll
                        .padding(end = 12.dp) // Space for scrollbar
                        .scrollbar(scrollState)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Ikuti langkah berikut untuk menghubungkan ECU ke aplikasi:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, // Bolder
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val steps = listOf(
                        "Izinkan semua permission yang diperlukan oleh aplikasi.",
                        "Pastikan Bluetooth HP sudah menyala.",
                        "Buka Pengaturan Bluetooth HP, lalu lakukan pairing dengan Bluetooth ECU dan masukkan PIN Bluetooth jika diminta.",
                        "Setelah berhasil di-pair, kembali ke aplikasi.",
                        "Jika nama Bluetooth ECU belum muncul, tekan tombol Refresh di pojok kanan atas.",
                        "Jika nama Bluetooth ECU yang sudah di-pair sudah muncul, tekan Connect.",
                        "Tunggu hingga proses koneksi Bluetooth selesai.",
                        "Selesai! 🎉 ECU sudah terhubung dan aplikasi siap digunakan untuk melakukan Live Monitor."
                    )

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}. ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black, // Extra bold numbers
                                color = Color(0xFF00E676) // Green numbers for visibility
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold, // Bolder body
                                color = Color.LightGray,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "💬 Butuh Bantuan?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Jika mengalami kendala, menemukan bug, atau memiliki pertanyaan, silakan DM melalui TikTok:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "@surya_kkr",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E676),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Terima kasih sudah menggunakan aplikasi! 🏍️",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("MENGERTI", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

fun Modifier.scrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    color: Color = Color.White.copy(alpha = 0.5f),
    cornerRadius: Dp = 4.dp
): Modifier = drawWithContent {
    drawContent()
    if (state.maxValue > 0) {
        val viewPortHeight = size.height
        val totalHeight = state.maxValue + viewPortHeight
        val scrollbarHeight = (viewPortHeight / totalHeight) * viewPortHeight
        val scrollbarOffset = (state.value / totalHeight) * viewPortHeight

        drawRoundRect(
            color = color,
            topLeft = Offset(size.width + 6.dp.toPx(), scrollbarOffset),
            size = Size(width.toPx(), scrollbarHeight),
            cornerRadius = CornerRadius(cornerRadius.toPx())
        )
    }
}
