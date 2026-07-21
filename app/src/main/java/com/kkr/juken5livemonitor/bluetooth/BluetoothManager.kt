package com.kkr.juken5livemonitor.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.kkr.juken5livemonitor.logger.ECULogger
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.concurrent.thread

class BluetoothManager {

    companion object {

        private const val TAG = "JUKEN_BT"

        private val UUID_SPP =
            UUID.fromString(
                "00001101-0000-1000-8000-00805F9B34FB"
            )

    }

    private val adapter =
        BluetoothAdapter.getDefaultAdapter()

    private var socket: BluetoothSocket? = null

    private var input: InputStream? = null

    private var output: OutputStream? = null

    @Volatile
    private var listening = false

    private var listener: ((String) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun connect(
        device: BluetoothDevice
    ): Boolean {

        return try {

            adapter?.cancelDiscovery()

            socket =
                device.createRfcommSocketToServiceRecord(UUID_SPP)

            socket?.connect()

            input =
                socket?.inputStream

            output =
                socket?.outputStream

            Log.d(
                TAG,
                "Bluetooth Connected"
            )

            ECULogger.info(
                "Bluetooth Connected"
            )

            true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Bluetooth Connect Failed",
                e
            )

            ECULogger.error(
                "Bluetooth Connect Failed : ${e.message}"
            )

            disconnect()

            false

        }

    }

    fun isConnected(): Boolean {

        return socket?.isConnected == true

    }

    fun send(
        command: String
    ): Boolean {

        return try {

            output?.write(
                command.toByteArray(
                    Charsets.US_ASCII
                )
            )

            output?.flush()

            Log.d(
                TAG,
                "TX : ${
                    command
                        .replace("\r", "\\r")
                        .replace("\n", "\\n")
                }"
            )

            ECULogger.tx(command)

            true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Send Failed",
                e
            )

            ECULogger.error(
                "Send Failed : ${e.message}"
            )

            false

        }

    }

    fun setListener(
        callback: (String) -> Unit
    ) {

        listener = callback

    }

    fun startListening() {

        if (listening)
            return

        val stream = input ?: return

        listening = true

        thread(
            name = "JukenBluetoothReader"
        ) {

            val buffer = ByteArray(1024)

            val builder = StringBuilder()

            while (
                listening &&
                isConnected()
            ) {

                try {

                    val len =
                        stream.read(buffer)

                    if (len <= 0)
                        continue

                    val raw =
                        String(
                            buffer,
                            0,
                            len,
                            Charsets.US_ASCII
                        )
                            .replace("\r", "\\r")
                            .replace("\n", "\\n")

                    Log.d(
                        TAG,
                        "RAW : $raw"
                    )

                    for (i in 0 until len) {

                        val c =
                            (buffer[i].toInt() and 0xFF).toChar()

                        if (
                            c == ';' ||
                            c == '\n'
                        ) {

                            val token =
                                builder
                                    .toString()
                                    .replace("\r", "")
                                    .trim()

                            builder.clear()

                            if (token.isNotEmpty()) {

                                Log.d(
                                    TAG,
                                    "TOKEN : $token"
                                )

                                ECULogger.rx(token)

                                listener?.invoke(token)

                            }

                        } else {

                            builder.append(c)

                        }

                    }
                } catch (e: Exception) {

                    if (!listening)
                        break

                    Log.e(
                        TAG,
                        "Reader stopped",
                        e
                    )

                    ECULogger.error(
                        "Reader stopped"
                    )

                    break

                }

            }

            listening = false

            Log.d(
                TAG,
                "Reader Finished"
            )

            ECULogger.info(
                "Reader Finished"
            )

        }

    }

    fun stopListening() {

        listening = false

        ECULogger.info(
            "Stop Listening"
        )

    }

    fun disconnect() {

        listening = false

        try {
            input?.close()
        } catch (_: Exception) {
        }

        try {
            output?.close()
        } catch (_: Exception) {
        }

        try {
            socket?.close()
        } catch (_: Exception) {
        }

        input = null
        output = null
        socket = null

        Log.d(
            TAG,
            "Bluetooth Disconnected"
        )

        ECULogger.info(
            "Bluetooth Disconnected"
        )

    }

}