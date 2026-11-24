package com.kkr.juken5livemonitor.screen.Connect

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.kkr.juken5livemonitor.ui.components.GlassButton
import com.kkr.juken5livemonitor.ui.components.GlassCard
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.kkr.juken5livemonitor.ui.components.GlassTopBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@SuppressLint("MissingPermission")
@Composable
fun LandscapeConnectContent(
    devices: List<BluetoothDevice>,
    status: String,
    isNotificationEnabled: Boolean,
    selectedJukenType: String,
    onJukenTypeChanged: (String) -> Unit,
    onConnect: (BluetoothDevice) -> Unit,
    onDemoMode: () -> Unit = {},
    onViewLogs: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onGrantNotification: () -> Unit = {},
    onHelpClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                GlassTopBar(
                    connected = false,
                    ecuName = "Juken 5 Live Monitor",
                    subtitle = "By KKR X AG",
                    inlineSubtitle = false,
                    onHelpClick = onHelpClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        text = "JUKEN 5",
                        modifier = Modifier.weight(1f),
                        containerColor = if (selectedJukenType == "Juken 5") Color(0xFFE53935) else Color.DarkGray
                    ) {
                        onJukenTypeChanged("Juken 5")
                    }

                    GlassButton(
                        text = "JUKEN 5+",
                        modifier = Modifier.weight(1f),
                        containerColor = if (selectedJukenType == "Juken 5+") Color(0xFFE53935) else Color.DarkGray
                    ) {
                        onJukenTypeChanged("Juken 5+")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassCard {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                text = "Connection Status",
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = status,
                                color = when (status) {
                                    "Connected" -> Color(0xFF00E676)
                                    "Connecting..." -> Color(0xFFFFD54F)
                                    else -> Color.White
                                }
                            )

                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    when (status) {
                                        "Connected" -> Color(0xFF00E676)
                                        "Connecting..." -> Color(0xFFFFD54F)
                                        else -> Color(0xFFE53935)
                                    },
                                    CircleShape
                                )
                        )

                    }

                }

                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.weight(1f))

                GlassButton(
                    text = "DEMO DASHBOARD",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onDemoMode()
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassButton(
                    text = "VIEW HISTORY LOG",
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.History,
                    containerColor = Color.DarkGray
                ) {
                    onViewLogs()
                }

            }

        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Bluetooth Devices",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    IconButton(
                        onClick = onRefresh
                    ) {

                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFFE53935)
                        )

                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(devices) { device ->

                        GlassCard(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp)
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            )  {

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = device.name ?: "Unknown",
                                        color = Color.White
                                    )


                                    Text(
                                        text = device.address,
                                        color = Color.Gray
                                    )

                                }

                                GlassButton(
                                    text = "CONNECT"
                                ) {
                                    onConnect(device)
                                }

                                }

                            }

                        }

                    }

                }

            }

        }

    }
