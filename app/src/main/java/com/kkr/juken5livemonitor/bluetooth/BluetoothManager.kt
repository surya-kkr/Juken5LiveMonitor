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

    private var onDisconnect: (() -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun connect(
        device: BluetoothDevice
    ): Boolean {
        return try {
            // Ensure any previous connection is fully cleared
            disconnect()

            val bluetoothAdapter = adapter
            checkNotNull(bluetoothAdapter) { "Bluetooth is not supported on this device" }
            check(bluetoothAdapter.isEnabled) { "Bluetooth is turned off" }
            check(device.bondState == BluetoothDevice.BOND_BONDED) {
                "ECU is not paired. Pair it first in Android Bluetooth settings"
            }

            bluetoothAdapter.cancelDiscovery()
            // Important: Small delay for Xiaomi/Vivo systems to finish cancelDiscovery
            Thread.sleep(200)

            Log.d(TAG, "Attempting Secure RFCOMM connection to ${device.address}")
            socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            
            try {
                socket?.connect()
            } catch (e: Exception) {
                Log.w(TAG, "Secure connection failed, attempting INSECURE fallback...", e)
                socket?.close()
                // MIUI needs a moment to release the first RFCOMM attempt before retrying.
                Thread.sleep(300)
                
                // FALLBACK: Insecure socket often bypasses strict MIUI/OriginOS policies
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP)
                try {
                    socket?.connect()
                } catch (insecureError: Exception) {
                    Log.w(
                        TAG,
                        "Insecure SPP discovery failed; attempting RFCOMM channel 1 fallback...",
                        insecureError
                    )
                    socket?.close()
                    Thread.sleep(300)

                    // Some Redmi/MIUI Bluetooth stacks fail the SPP SDP lookup even for a
                    // paired ECU. Most SPP ECUs expose their serial service on RFCOMM channel 1.
                    socket = createRfcommChannelSocket(device, 1)
                    socket?.connect()
                }
            }

            input = socket?.inputStream
            output = socket?.outputStream

            Log.i(TAG, "Bluetooth Connected Successfully")
            ECULogger.info("Bluetooth Connected")
            true

        } catch (e: Exception) {
            val reason = e.message ?: e.javaClass.simpleName
            Log.e(TAG, "Bluetooth Connect Failed (all socket methods): $reason", e)
            ECULogger.error("Bluetooth Connect Failed: $reason")
            disconnect()
            false
        }
    }

    /**
     * Last-resort compatibility path for paired SPP devices whose service-discovery
     * record is rejected by a vendor Bluetooth stack (seen on some MIUI Android 12 builds).
     */
    private fun createRfcommChannelSocket(
        device: BluetoothDevice,
        channel: Int
    ): BluetoothSocket {
        val method = BluetoothDevice::class.java.getMethod(
            "createRfcommSocket",
            Int::class.javaPrimitiveType
        )
        return method.invoke(device, channel) as BluetoothSocket
    }

    @SuppressLint("MissingPermission")
    fun connectPlus(
        device: BluetoothDevice
    ): Boolean {
        val success = connect(device)
        if (success) {
            Log.i(TAG, "Juken 5+ Bluetooth connected")
        }
        return success
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
        if (listening) return
        val stream = input ?: return

        listening = true

        thread(
            name = "JukenBluetoothReader",
            priority = Thread.MAX_PRIORITY // Give max priority to prevent data stutter on Xiaomi
        ) {
            // Set Process priority as well for UI responsiveness on aggressive OSes
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)

            val buffer = ByteArray(2048) // Increased buffer size
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

            onDisconnect?.invoke()

        }

    }

    fun setOnDisconnectListener(callback: () -> Unit) {
        onDisconnect = callback
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
