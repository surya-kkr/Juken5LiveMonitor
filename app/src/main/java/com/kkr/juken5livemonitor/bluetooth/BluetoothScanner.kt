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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            android.util.Log.d(
                "BT_PERMISSION",
                "Granted = $granted"
            )
            android.util.Log.d(
                "BT_PERMISSION",
                "SDK = ${Build.VERSION.SDK_INT}"
            )
            if (!granted) {
                return emptyList()
            }
        }

        android.util.Log.d("BT_DEBUG", "Adapter = $adapter")
        android.util.Log.d("BT_DEBUG", "Enabled = ${adapter?.isEnabled}")
        android.util.Log.d("BT_DEBUG", "State = ${adapter?.state}")

        val bonded = adapter?.bondedDevices

        android.util.Log.d("BT_DEBUG", "Bonded = ${bonded?.size}")

        bonded?.forEach {
            android.util.Log.d(
                "BT_DEBUG",
                "${it.name} | ${it.address}"
            )
        }

        return bonded?.toList() ?: emptyList()
    }
}