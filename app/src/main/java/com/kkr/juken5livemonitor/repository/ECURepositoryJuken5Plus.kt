package com.kkr.juken5livemonitor.repository

import com.kkr.juken5livemonitor.protocol.ECUProtocolJuken5Plus
import com.kkr.juken5livemonitor.protocol.Juken5PlusPacketParser
import com.kkr.juken5livemonitor.session.BluetoothSession
import com.kkr.juken5livemonitor.model.ECULiveData

class ECURepositoryJuken5Plus : ReadOnlyEcuRepository {
    private val bluetoothManager
        get() = BluetoothSession.bluetoothManager

    private val parser = Juken5PlusPacketParser()

    override fun isConnected(): Boolean = bluetoothManager?.isConnected() == true

    override fun handshake(): Boolean = bluetoothManager?.send(ECUProtocolJuken5Plus.REQUEST_PARAMETERS) ?: false

    override fun requestModel(): Boolean = bluetoothManager?.send(ECUProtocolJuken5Plus.GET_MODEL) ?: false

    override fun requestFuelMap(offset: String, chunk: Int): Boolean =
        bluetoothManager?.send("${ECUProtocolJuken5Plus.REQUEST_FUEL_MAP};$offset;$chunk\r\n") ?: false

    override fun requestBaseMap(offset: String, chunk: Int): Boolean =
        bluetoothManager?.send("${ECUProtocolJuken5Plus.REQUEST_BASE_MAP};$offset;$chunk\r\n") ?: false

    override fun requestInjectorMap(offset: String, chunk: Int): Boolean =
        bluetoothManager?.send("${ECUProtocolJuken5Plus.REQUEST_INJECTOR_MAP};$offset;$chunk\r\n") ?: false

    override fun requestIgnitionMap(offset: String, chunk: Int): Boolean =
        bluetoothManager?.send("${ECUProtocolJuken5Plus.REQUEST_IGNITION_MAP};$offset;$chunk\r\n") ?: false

    override fun requestHistory(chunk: Int): Boolean =
        bluetoothManager?.send("${ECUProtocolJuken5Plus.REQUEST_HISTORY};$chunk\r\n") ?: false

    override fun setSyncListener(callback: (String, List<String>) -> Unit) {
        parser.setSyncListener(callback)
        bluetoothManager?.setListener { token -> parser.parse(token) }
    }

    override fun setOnDisconnectListener(callback: () -> Unit) {
        bluetoothManager?.setOnDisconnectListener(callback)
    }

    override fun setLiveListener(callback: (ECULiveData) -> Unit) {
        bluetoothManager?.setListener { token -> parser.parse(token)?.let(callback) }
    }

    override fun startListening() = bluetoothManager?.startListening() ?: Unit

    override fun stopListening() = bluetoothManager?.stopListening() ?: Unit

    override fun openLive(): Boolean = bluetoothManager?.send(ECUProtocolJuken5Plus.OPEN_LIVE) ?: false

    override fun stopLive(): Boolean = bluetoothManager?.send(ECUProtocolJuken5Plus.STOP_LIVE) ?: false

    override suspend fun stopLiveGracefully() {
        repeat(10) {
            stopLive()
            kotlinx.coroutines.delay(100)
        }
    }

    override fun send(command: String): Boolean = bluetoothManager?.send(command) ?: false

    override fun disconnect() {
        try {
            stopLive()
            stopListening()
            Thread.sleep(200)
        } catch (_: Exception) {
        }
        BluetoothSession.disconnect()
    }
}
