package com.kkr.juken5livemonitor.repository

import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.protocol.ECUProtocol
import com.kkr.juken5livemonitor.protocol.PacketParser
import com.kkr.juken5livemonitor.session.BluetoothSession

class ECURepository {

    private val bt
        get() = BluetoothSession.bluetoothManager

    private val parser = PacketParser()

    fun isConnected(): Boolean {
        return bt?.isConnected() == true
    }

    fun handshake(): Boolean {
        return bt?.send(ECUProtocol.DIAGNOSTIC) ?: false
    }

    fun openLive(): Boolean {
        return bt?.send(ECUProtocol.OPEN_LIVE) ?: false
    }

    fun stopLive(): Boolean {
        return bt?.send(ECUProtocol.STOP_LIVE) ?: false
    }

    fun execute(): Boolean {
        return bt?.send(ECUProtocol.EXECUTE) ?: false
    }

    fun send(command: String): Boolean {
        return bt?.send(command) ?: false
    }

    /**
     * Callback ketika satu paket Live selesai diparse.
     */
    fun setLiveListener(

        callback: (ECULiveData) -> Unit

    ) {

        bt?.setListener { token ->

            parser.parse(token)?.let {

                callback(it)

            }

        }

    }

    fun startListening() {

        bt?.startListening()

    }

    fun stopListening() {

        bt?.stopListening()

    }

    fun disconnect() {

        try {

            stopLive()

            stopListening()

            Thread.sleep(200)

        } catch (_: Exception) {
        }

        BluetoothSession.disconnect()

    }

}