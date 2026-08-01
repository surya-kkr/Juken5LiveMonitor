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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentBikeName: String,
    currentUi: Int,
    currentVehicleWeight: Float,
    currentRiderWeight: Float,
    currentDrivetrain: MonitorViewModel.DrivetrainType,
    showUiSelection: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (String, Int, Float, Float, MonitorViewModel.DrivetrainType) -> Unit
) {
    var bikeName by remember { mutableStateOf(currentBikeName) }
    var selectedUi by remember { mutableStateOf(currentUi) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
                .padding(20.dp)
        ) {
            Column(
                modifier = if (showUiSelection) Modifier.verticalScroll(scrollState) else Modifier,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Application Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

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

                if (showUiSelection) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "UI Design Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.LightGray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UiTypeCard(
                                label = "Classic",
                                selected = selectedUi == 0,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedUi = 0 }
                            )
                            UiTypeCard(
                                label = "Modern",
                                selected = selectedUi == 1,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedUi = 1 }
                            )
                            UiTypeCard(
                                label = "Racing",
                                selected = selectedUi == 2,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedUi = 2 }
                            )
                            UiTypeCard(
                                label = "KKRXAG",
                                selected = selectedUi == 3,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedUi = 3 }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = { 
                            onSave(bikeName, selectedUi, currentVehicleWeight, currentRiderWeight, currentDrivetrain) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Save Changes", color = Color.White)
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
            Icon(
                Icons.Default.Palette,
                contentDescription = null,
                tint = if (selected) Color.Red else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else Color.Gray,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
