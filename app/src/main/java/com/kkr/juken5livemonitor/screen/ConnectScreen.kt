package com.kkr.juken5livemonitor.screen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.bluetooth.BluetoothManager
import com.kkr.juken5livemonitor.bluetooth.BluetoothScanner
import com.kkr.juken5livemonitor.session.BluetoothSession
import kotlinx.coroutines.*

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

    val devices by remember {
        mutableStateOf(scanner.getPairedDevices())
    }

    var connecting by remember {
        mutableStateOf(false)
    }

    var status by remember {
        mutableStateOf("Belum Terhubung")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Juken 5 Live Monitor",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pilih ECU Bluetooth",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (devices.isEmpty()) {

            Text("Tidak ada perangkat Bluetooth yang sudah dipair.")

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                items(devices) { device ->

                    BluetoothDeviceCard(
                        device = device,
                        enabled = !connecting,
                        onConnect = {

                            if (connecting) return@BluetoothDeviceCard

                            connecting = true
                            status = "Menghubungkan..."

                            CoroutineScope(Dispatchers.IO).launch {

                                val success =
                                    bluetoothManager.connect(device)

                                withContext(Dispatchers.Main) {

                                    if (success) {

                                        BluetoothSession.initialize(
                                            bluetoothManager
                                        )

                                        status = "Terhubung"

                                        navController.navigate("monitor") {
                                            popUpTo("connect")
                                        }

                                    } else {

                                        status = "Gagal Terhubung"
                                        connecting = false

                                    }

                                }

                            }

                        }
                    )

                }

            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        if (connecting) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(status)

            }

        } else {

            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium
            )

        }

    }

}

@Composable
private fun BluetoothDeviceCard(
    device: BluetoothDevice,
    enabled: Boolean,
    onConnect: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = device.name ?: "Unknown Device",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = device.address,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onClick = onConnect
            ) {

                Text(
                    if (enabled)
                        "CONNECT"
                    else
                        "CONNECTING..."
                )

            }

        }

    }

}