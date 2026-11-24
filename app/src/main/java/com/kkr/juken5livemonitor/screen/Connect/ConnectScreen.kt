package com.kkr.juken5livemonitor.screen.Connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.bluetooth.BluetoothManager
import com.kkr.juken5livemonitor.bluetooth.BluetoothScanner
import com.kkr.juken5livemonitor.screen.Connect.components.ConnectionHelpDialog
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import android.content.res.Configuration
import android.util.Log
import android.provider.Settings
import android.content.Intent
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    var isNotificationAccessGranted by remember {
        mutableStateOf(false)
    }

    var isPostNotificationPermissionGranted by remember {
        mutableStateOf(false)
    }

    var showHelpDialog by remember {
        mutableStateOf(false)
    }

    var selectedJukenType by remember {
        mutableStateOf(
            context.getSharedPreferences("juken_settings", android.content.Context.MODE_PRIVATE)
                .getString("selected_juken_type", "Juken 5") ?: "Juken 5"
        )
    }

    val postNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPostNotificationPermissionGranted = granted
    }

    LaunchedEffect(Unit) {
        isNotificationAccessGranted = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )?.contains(context.packageName) == true

        isPostNotificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        val list = scanner.getPairedDevices()

        Log.d("BT_DEBUG", "Start = ${list.size}")

        list.forEach {
            Log.d(
                "BT_DEBUG",
                "${it.name} | ${it.address}"
            )
        }

        devices = list
    }
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    if (showHelpDialog) {
        ConnectionHelpDialog(onDismiss = { showHelpDialog = false })
    }

    GlassBackground {

        if (isLandscape) {

            LandscapeConnectContent(
                devices = devices,
                status = status,
                isNotificationEnabled = isNotificationAccessGranted && isPostNotificationPermissionGranted,
                selectedJukenType = selectedJukenType,
                onJukenTypeChanged = { type ->
                    selectedJukenType = type
                    context.getSharedPreferences("juken_settings", android.content.Context.MODE_PRIVATE)
                        .edit().putString("selected_juken_type", type).apply()
                },
                onConnect = { device ->

                    connecting = true
                    status = "Connecting..."

                    // Save device address for automatic reconnection during sync
                    val prefs = context.getSharedPreferences("juken_settings", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putString("last_device_address", device.address).apply()

                    CoroutineScope(Dispatchers.IO).launch {

                        val success = if (selectedJukenType == "Juken 5+") {
                            bluetoothManager.connectPlus(device)
                        } else {
                            bluetoothManager.connect(device)
                        }

                        withContext(Dispatchers.Main) {

                            connecting = false

                            if (success) {

                                BluetoothSession.initialize(bluetoothManager)

                                status = "Connected"

                                navController.navigate("com/kkr/juken5livemonitor/screen/monitor")

                            } else {

                                status = "Failed to Connect"

                            }

                        }

                    }

                },
                onDemoMode = {
                    navController.navigate("monitor_demo")
                },
                onViewLogs = {
                    navController.navigate("log_history")
                },
                onRefresh = {
                    val list = scanner.getPairedDevices()
                    Log.d("BT_DEBUG", "Refresh Landscape = ${list.size}")
                    devices = list
                },
                onGrantNotification = {
                    context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                },
                onHelpClick = { showHelpDialog = true }
            )

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                GlassTopBar(
                    connected = status == "Connected",
                    ecuName = "Juken 5 Live Monitor",
                    subtitle = "By KKR X AG",
                    onHelpClick = { showHelpDialog = true }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        text = "JUKEN 5",
                        modifier = Modifier.weight(1f),
                        containerColor = if (selectedJukenType == "Juken 5") Color(0xFFE53935) else Color.DarkGray
                    ) {
                        selectedJukenType = "Juken 5"
                        context.getSharedPreferences("juken_settings", android.content.Context.MODE_PRIVATE)
                            .edit().putString("selected_juken_type", "Juken 5").apply()
                    }

                    GlassButton(
                        text = "JUKEN 5+",
                        modifier = Modifier.weight(1f),
                        containerColor = if (selectedJukenType == "Juken 5+") Color(0xFFE53935) else Color.DarkGray
                    ) {
                        selectedJukenType = "Juken 5+"
                        context.getSharedPreferences("juken_settings", android.content.Context.MODE_PRIVATE)
                            .edit().putString("selected_juken_type", "Juken 5+").apply()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                            val list = scanner.getPairedDevices()

                            Log.d("BT_DEBUG", "Refresh = ${list.size}")

                            list.forEach {
                                Log.d(
                                    "BT_DEBUG",
                                    "${it.name} | ${it.address}"
                                )
                            }

                            devices = list
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
                            .height(240.dp),
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

                                    // Save device address for automatic reconnection during sync
                                    val prefs = context.getSharedPreferences("juken_settings", android.content.Context.MODE_PRIVATE)
                                    prefs.edit().putString("last_device_address", device.address).apply()

                                    CoroutineScope(Dispatchers.IO).launch {

                                        val success = if (selectedJukenType == "Juken 5+") {
                                            bluetoothManager.connectPlus(device)
                                        } else {
                                            bluetoothManager.connect(device)
                                        }

                                        withContext(Dispatchers.Main) {

                                            connecting = false

                                            if (success) {

                                                BluetoothSession.initialize(bluetoothManager)

                                                status = "Connected"

                                                navController.navigate("com/kkr/juken5livemonitor/screen/monitor")

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
                                color = when (status) {
                                    "Connected" -> Color(0xFF00E676)
                                    "Connecting..." -> Color(0xFFFFD54F)
                                    else -> Color.White
                                }
                            )

                        }

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    when (status) {
                                        "Connected" -> Color(0xFF00E676)
                                        "Connecting..." -> Color(0xFFFFD54F)
                                        else -> Color(0xFFE53935)
                                    }
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

                Spacer(modifier = Modifier.height(10.dp))

                GlassButton(
                    text = "VIEW HISTORY LOG",
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.History,
                    containerColor = Color.DarkGray
                ) {
                    navController.navigate("log_history")
                }

            }

    } // else

} // GlassBackground

} // ConnectScreen
