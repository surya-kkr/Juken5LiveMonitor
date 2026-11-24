package com.kkr.juken5livemonitor.screen.monitor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kkr.juken5livemonitor.R
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentBikeName: String,
    currentUi: Int,
    currentDpi: Int,
    currentVehicleWeight: Float,
    currentRiderWeight: Float,
    currentDrivetrain: MonitorViewModel.DrivetrainType,
    currentOverheatThreshold: Int = 105,
    currentAutoReconnect: Boolean = true,
    showUiSelection: Boolean = true,
    allowedUis: List<Int>? = null,
    customLabels: Map<Int, String>? = null,
    isRecording: Boolean = false,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onDismiss: () -> Unit,
    onViewLogs: () -> Unit = {},
    onSave: (String, Int, Float, Float, MonitorViewModel.DrivetrainType, Int, Int, Boolean) -> Unit
) {
    var bikeName by remember { mutableStateOf(currentBikeName) }
    var selectedUi by remember { mutableStateOf(currentUi) }
    var selectedDpi by remember { mutableStateOf(currentDpi.toFloat()) }
    var overheatInput by remember { mutableStateOf(currentOverheatThreshold.toString()) }
    var autoReconnect by remember { mutableStateOf(currentAutoReconnect) }
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val uiOptions = listOf(
        Triple(customLabels?.get(0) ?: "Classic Android Auto", 0, "Classic"),
        Triple(customLabels?.get(1) ?: "Modern Android Auto", 1, "Modern"),
        Triple(customLabels?.get(2) ?: "Racing", 2, "Racing"),
        Triple(customLabels?.get(3) ?: "KKRXAG", 3, "KKRXAG")
    )

    val filteredUiOptions = if (allowedUis != null) {
        uiOptions.filter { it.second in allowedUis }
    } else {
        uiOptions
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.85f else 0.92f)
                .wrapContentHeight()
                .background(
                    Color.Black.copy(alpha = if (showUiSelection) 0.9f else 0.85f),
                    RoundedCornerShape(20.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(20.dp)
                )
                .padding(if (isLandscape) 20.dp else 16.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Application Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = if (isLandscape) 17.sp else 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val finalOverheat = overheatInput.toIntOrNull()?.coerceIn(50, 180) ?: currentOverheatThreshold
                                onSave(
                                    bikeName,
                                    selectedUi,
                                    currentVehicleWeight,
                                    currentRiderWeight,
                                    currentDrivetrain,
                                    selectedDpi.toInt(),
                                    finalOverheat,
                                    autoReconnect
                                )
                            },
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Save Changes",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                if (isLandscape) {
                    // Landscape: 1 Baris Horisontal (Bike Name | Overheat | Auto Reconnect)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Bike Name",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )
                            OutlinedTextField(
                                value = bikeName,
                                onValueChange = { bikeName = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.Red,
                                    focusedBorderColor = Color.Red,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.width(130.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Overheat Warning",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )
                            OutlinedTextField(
                                value = overheatInput,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() } && input.length <= 3) {
                                        overheatInput = input
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                trailingIcon = {
                                    Text(
                                        text = "°C",
                                        color = Color(0xFFFF1744),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFFFF1744),
                                    focusedBorderColor = Color(0xFFFF1744),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Auto Reconnect",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (autoReconnect) Color.White else Color.LightGray
                            )
                            Box(
                                modifier = Modifier
                                    .height(52.dp)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Switch(
                                    checked = autoReconnect,
                                    onCheckedChange = { autoReconnect = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color.Red,
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                                        uncheckedBorderColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Portrait: Tata Letak Lega & Proporsional
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Baris 1: Bike Name (lebar penuh)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Bike Name",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )
                            OutlinedTextField(
                                value = bikeName,
                                onValueChange = { bikeName = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.Red,
                                    focusedBorderColor = Color.Red,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Baris 2: Overheat Warning & Auto Reconnect berdampingan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Overheat Warning",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.LightGray
                                )
                                OutlinedTextField(
                                    value = overheatInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } && input.length <= 3) {
                                            overheatInput = input
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                    .height(50.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    trailingIcon = {
                                        Text(
                                            text = "°C",
                                            color = Color(0xFFFF1744),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color(0xFFFF1744),
                                        focusedBorderColor = Color(0xFFFF1744),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Auto Reconnect",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (autoReconnect) Color.White else Color.LightGray
                                )
                                Box(
                                    modifier = Modifier
                                        .height(50.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Switch(
                                        checked = autoReconnect,
                                        onCheckedChange = { autoReconnect = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color.Red,
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                                            uncheckedBorderColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (showUiSelection) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "UI Design Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.LightGray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            filteredUiOptions.forEach { (label, index, _) ->
                                UiTypeCard(
                                    label = label,
                                    selected = selectedUi == index,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedUi = index }
                                )
                            }
                        }
                    }
                }

                if (selectedUi == 1 || (selectedUi == 0 && customLabels == null)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Android Auto DPI",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )
                            Text(
                                text = "${selectedDpi.toInt()} DPI",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = selectedDpi,
                            onValueChange = { selectedDpi = it },
                            valueRange = 100f..240f,
                            steps = 13, // 10 DPI steps: 100, 110, ..., 240
                            modifier = Modifier.height(28.dp),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color.Red, RoundedCornerShape(8.dp))
                                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                                )
                            },
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    sliderState = sliderState,
                                    modifier = Modifier.height(4.dp),
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = Color.Red,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        )
                    }
                }

                // Log Section: Responsive layout (Landscape: Side-by-Side, Portrait: Stacked)
                if (!showUiSelection || (selectedUi == 0 && customLabels != null) || selectedUi == 2 || selectedUi == 3) {
                    if (showUiSelection) {
                        // Landscape (Racing/KKRXAG): Side by Side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    if (isRecording) onStopRecording() else onStartRecording() 
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecording) Color.Red else Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = if (isRecording) Color.White else Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isRecording) "STOP LOG" else "RECORD LOG",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = onViewLogs,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("VIEW LOG", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Portrait: Stacked and Close
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { 
                                    if (isRecording) onStopRecording() else onStartRecording() 
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecording) Color.Red else Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = if (isRecording) Color.White else Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRecording) "STOP RECORDING" else "START RECORD LOG",
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = onViewLogs,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VIEW HISTORY LOG", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UiTypeCard(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .background(
                if (selected) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (selected) Color.Red else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (label.contains("Android Auto")) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stat_aa),
                    contentDescription = null,
                    tint = if (selected) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = if (selected) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else Color.Gray,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
