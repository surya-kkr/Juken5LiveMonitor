package com.kkr.juken5livemonitor.screen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.bluetooth.BluetoothManager
import com.kkr.juken5livemonitor.bluetooth.BluetoothScanner
import com.kkr.juken5livemonitor.session.BluetoothSession
import com.kkr.juken5livemonitor.ui.components.GlassBackground
import com.kkr.juken5livemonitor.ui.components.GlassButton
import com.kkr.juken5livemonitor.ui.components.GlassCard
import com.kkr.juken5livemonitor.ui.components.GlassTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration

@SuppressLint("MissingPermission")
@Composable
fun ConnectScreen(
    navController: NavHostController
) {

    val context = LocalContext.current

    val scanner = remember {
        BluetoothScanner(context)
    }

    val bluetoothManager = remember {
        BluetoothManager()
    }

    var devices by remember {
        mutableStateOf<List<BluetoothDevice>>(emptyList())
    }

    var connecting by remember {
        mutableStateOf(false)
    }

    var status by remember {
        mutableStateOf("Disconnected")
    }

    LaunchedEffect(Unit) {
        devices = scanner.getPairedDevices()
    }
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    GlassBackground {

        if (isLandscape) {

            LandscapeConnectContent(
                devices = devices,
                status = status,
                onConnect = { device ->

                    connecting = true
                    status = "Connecting..."

                    CoroutineScope(Dispatchers.IO).launch {

                        val success = bluetoothManager.connect(device)

                        withContext(Dispatchers.Main) {

                            connecting = false

                            if (success) {

                                BluetoothSession.initialize(bluetoothManager)

                                status = "Connected"

                                navController.navigate("monitor")

                            } else {

                                status = "Failed to Connect"

                            }

                        }

                    }

                }
            )

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                GlassTopBar(
                    connected = status == "Connected",
                    ecuName = "BRT Juken 5 by KKR"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {

                    Column {

                        Text(
                            text = "Bluetooth Devices",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${devices.size} Paired Device",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                    }

                    IconButton(
                        enabled = !connecting,
                        onClick = {
                            devices = scanner.getPairedDevices()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFFE53935)
                        )
                    }

                }

                Spacer(modifier = Modifier.height(18.dp))
                if (devices.isEmpty()) {

                    GlassCard {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "Tidak ada perangkat Bluetooth yang dipair.",
                                color = Color.White
                            )

                        }

                    }

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(devices) { device ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

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
                                    text = "CONNECT",
                                    modifier = Modifier.width(120.dp)
                                ) {

                                    connecting = true
                                    status = "Connecting..."

                                    CoroutineScope(Dispatchers.IO).launch {

                                        val success = bluetoothManager.connect(device)

                                        withContext(Dispatchers.Main) {

                                            connecting = false

                                            if (success) {

                                                BluetoothSession.initialize(bluetoothManager)

                                                status = "Connected"

                                                navController.navigate("monitor")

                                            } else {

                                                status = "Failed to Connect"

                                            }

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

                Spacer(modifier = Modifier.height(18.dp))

                GlassCard(
                    modifier = Modifier.wrapContentHeight()
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                text = "Connection Status",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = status,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (status == "Connected")
                                    Color(0xFF00E676)
                                else
                                    Color.White
                            )

                        }

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    if (status == "Connected")
                                        Color(0xFF00E676)
                                    else
                                        Color(0xFFE53935)
                                )
                        )

                    }

                }
                Spacer(modifier = Modifier.height(20.dp))

                GlassButton(
                    text = "DEMO DASHBOARD",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    navController.navigate("monitor_demo")
                }

            }

    } // else

} // GlassBackground

} // ConnectScreen