package com.kkr.juken5livemonitor.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class BluetoothScanner(private val context: Context) {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun getPairedDevices(): List<BluetoothDevice> {

        // Android 12 (API 31) ke atas wajib punya BLUETOOTH_CONNECT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                return emptyList()
            }
        }

        return adapter?.bondedDevices?.toList() ?: emptyList()
    }
}