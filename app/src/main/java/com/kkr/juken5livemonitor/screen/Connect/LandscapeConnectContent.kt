package com.kkr.juken5livemonitor.screen.Connect

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import com.kkr.juken5livemonitor.ui.components.GlassButton
import com.kkr.juken5livemonitor.ui.components.GlassCard
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.kkr.juken5livemonitor.ui.components.GlassTopBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun LandscapeConnectContent(
    devices: List<BluetoothDevice>,
    status: String,
    onConnect: (BluetoothDevice) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                GlassTopBar(
                    connected = false,
                    ecuName = "BRT Juken 5 by KKR"
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                                color = if (status == "Connected")
                                    Color(0xFF00E676)
                                else
                                    Color.White
                            )

                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (status == "Connected")
                                        Color(0xFF00E676)
                                    else
                                        Color(0xFFE53935)
                                )
                        )

                    }

                }

                Spacer(modifier = Modifier.weight(1f))

                GlassButton(
                    text = "DEMO DASHBOARD",
                    modifier = Modifier.fillMaxWidth()
                ) {

                }

            }

        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .weight(0.65f)
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
                        onClick = {

                        }
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

                        GlassCard {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
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
