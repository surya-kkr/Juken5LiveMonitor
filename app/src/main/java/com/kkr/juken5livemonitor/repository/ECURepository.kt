package com.kkr.juken5livemonitor.repository

import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.protocol.ECUProtocol
import com.kkr.juken5livemonitor.protocol.PacketParser
import com.kkr.juken5livemonitor.session.BluetoothSession

class ECURepository : ReadOnlyEcuRepository {

    private val bt
        get() = BluetoothSession.bluetoothManager

    private val parser = PacketParser()

    override fun isConnected(): Boolean {
        return bt?.isConnected() == true
    }

    override fun handshake(): Boolean {
        return bt?.send(ECUProtocol.GET_VARIABLE) ?: false
    }

    override fun requestModel(): Boolean {
        return bt?.send(ECUProtocol.GET_MAP) ?: false
    }

    override fun requestFuelMap(offset: String, chunk: Int): Boolean {
        return bt?.send("1602;$offset;$chunk\r\n") ?: false
    }

    override fun requestBaseMap(offset: String, chunk: Int): Boolean {
        return bt?.send("1601;$offset;$chunk\r\n") ?: false
    }

    override fun requestInjectorMap(offset: String, chunk: Int): Boolean {
        return bt?.send("1603;$offset;$chunk\r\n") ?: false
    }

    override fun requestIgnitionMap(offset: String, chunk: Int): Boolean {
        return bt?.send("1605;$offset;$chunk\r\n") ?: false
    }

    override fun requestHistory(chunk: Int): Boolean {
        return bt?.send("1610;$chunk\r\n") ?: false
    }

    fun setRawListener(callback: (String) -> Unit) {
        bt?.setListener(callback)
    }

    override fun setSyncListener(callback: (String, List<String>) -> Unit) {
        parser.setSyncListener(callback)
        // Ensure parser is fed with raw tokens during sync
        bt?.setListener { token ->
            parser.parse(token)
        }
    }

    override fun setOnDisconnectListener(callback: () -> Unit) {
        bt?.setOnDisconnectListener(callback)
    }

    override fun openLive(): Boolean {
        return bt?.send(ECUProtocol.OPEN_LIVE) ?: false
    }

    override fun stopLive(): Boolean {
        return bt?.send(ECUProtocol.STOP_LIVE) ?: false
    }

    override suspend fun stopLiveGracefully() {
        repeat(10) {
            stopLive()
            kotlinx.coroutines.delay(100)
        }
    }

    fun execute(): Boolean {
        return bt?.send(ECUProtocol.EXECUTE) ?: false
    }

    override fun send(command: String): Boolean {
        return bt?.send(command) ?: false
    }

    override fun setLiveListener(

        callback: (ECULiveData) -> Unit

    ) {

        bt?.setListener { token ->

            parser.parse(token)?.let {

                callback(it)

            }

        }

    }

    override fun startListening() {

        bt?.startListening()

    }

    override fun stopListening() {

        bt?.stopListening()

    }

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
