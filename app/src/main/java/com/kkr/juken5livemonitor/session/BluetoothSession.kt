package com.kkr.juken5livemonitor.session

import com.kkr.juken5livemonitor.bluetooth.BluetoothManager

object BluetoothSession {

    var bluetoothManager: BluetoothManager? = null
        private set

    fun initialize(manager: BluetoothManager) {
        bluetoothManager = manager
    }

    fun isConnected(): Boolean {
        return bluetoothManager?.isConnected() == true
    }

    fun disconnect() {
        bluetoothManager?.disconnect()
        bluetoothManager = null
    }
}