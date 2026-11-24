package com.kkr.juken5livemonitor.protocol

import com.kkr.juken5livemonitor.model.ECULiveData
import kotlin.math.roundToInt

class PacketParser {

    private enum class State {
        WAIT_HEADER,
        TPS,
        VBAT,
        CORE,
        RPM,
        EOT,
        FUEL,
        AFR,
        BASE,
        INJECTOR,
        IGNITION,
        MAP,
        IAT,
        SYNC_DATA
    }

    private var state = State.WAIT_HEADER
    private var syncCounter = 0
    private var syncTarget = 0
    private var currentHeader = ""
    private val syncBuffer = mutableListOf<String>()

    private var onChunkComplete: ((String, List<String>) -> Unit)? = null

    fun setSyncListener(callback: (String, List<String>) -> Unit) {
        onChunkComplete = callback
    }

    private var tps = 0
    private var battery = 0.0
    private var core = 0
    private var rpm = 0
    private var eot = 0.0
    private var fuel = ""
    private var afr = 0.0
    private var baseMap = ""
    private var injector = ""
    private var ignition = 0.0
    private var map = 0
    private var iat = 0.0

    fun parse(token: String): ECULiveData? {

        val data = token.trim()

        if (data.isEmpty())
            return null

        if (data.equals(ECUProtocol.PING_RESPONSE, true)) {
            onChunkComplete?.invoke(ECUProtocol.PING_RESPONSE, listOf(data))
            return null
        }

        if (data.equals(ECUProtocol.ACK, true)) {
            onChunkComplete?.invoke(ECUProtocol.ACK, listOf(ECUProtocol.ACK))
            return null
        }

        when (state) {

            State.WAIT_HEADER -> {

                when {
                    data.equals(ECUProtocol.LIVE_HEADER, true) -> {
                        state = State.TPS
                        onChunkComplete?.invoke(ECUProtocol.LIVE_HEADER, listOf(data))
                    }
                    data == ECUProtocol.VARIABLE_HEADER -> startSync(data, 73)
                    data == ECUProtocol.MODEL_HEADER -> startSync(data, 3)
                    data == ECUProtocol.FUELMAP_HEADER -> startSync(data, 61)
                    data == ECUProtocol.BASEMAP_HEADER -> startSync(data, 61)
                    data == ECUProtocol.INJECTOR_HEADER -> startSync(data, 61)
                    data == ECUProtocol.IGNITION_HEADER -> startSync(data, 31)
                    data == ECUProtocol.HISTORY_HEADER -> startSync(data, 61)
                    data == ECUProtocol.REMOTE_READY_HEADER ->
                        onChunkComplete?.invoke(ECUProtocol.REMOTE_READY_HEADER, listOf(data))
                }

            }

            State.SYNC_DATA -> {
                // If we see a known header while in SYNC_DATA, 
                // the previous sync might have finished early or was interrupted.
                if (isHeader(data)) {
                    val finalBuffer = ArrayList(syncBuffer)
                    val header = currentHeader
                    reset()
                    onChunkComplete?.invoke(header, finalBuffer)
                    
                    // Re-process this token as a header for the next state
                    return parse(data)
                }

                syncBuffer.add(data)
                syncCounter++

                if (syncCounter >= syncTarget) {
                    val finalBuffer = ArrayList(syncBuffer)
                    val header = currentHeader
                    reset()
                    onChunkComplete?.invoke(header, finalBuffer)
                }
            }

            State.TPS -> {

                val raw =
                    data.toIntOrNull() ?: run {

                        reset()

                        return null

                    }

                tps = when {

                    raw <= 0 -> 0

                    raw == 1 -> 5

                    raw in 2..19 -> raw * 5 - 5

                    else -> 100

                }

                state = State.VBAT

            }

            State.VBAT -> {

                battery =
                    data.toDoubleOrNull() ?: 0.0

                state = State.CORE

            }

            State.CORE -> {

                core =
                    data.toIntOrNull() ?: 0

                state = State.RPM

            }

            State.RPM -> {

                rpm =
                    data.toIntOrNull() ?: 0

                state = State.EOT

            }

            State.EOT -> {

                eot =
                    (data.toDoubleOrNull() ?: 0.0) / 10.0

                state = State.FUEL

            }

            State.FUEL -> {

                fuel = data

                state = State.AFR

            }

            State.AFR -> {

                afr =
                    data.toDoubleOrNull() ?: 0.0

                state = State.BASE

            }

            State.BASE -> {

                baseMap = data

                state = State.INJECTOR

            }

            State.INJECTOR -> {

                injector = data

                state = State.IGNITION

            }

            State.IGNITION -> {

                ignition =
                    (data.toDoubleOrNull() ?: 0.0) / 10.0

                state = State.MAP

            }

            State.MAP -> {

                val adc =
                    data.toDoubleOrNull() ?: 0.0

                map = adcToMap(adc)

                state = State.IAT

            }

            State.IAT -> {

                iat =
                    (data.toDoubleOrNull() ?: 0.0) / 10.0

                val result = ECULiveData(

                    tps = tps,

                    battery = battery,

                    core = core,

                    rpm = rpm,

                    eot = eot,

                    fuel = fuel,

                    afr = afr,

                    baseMap = baseMap,

                    injector = injector,

                    ignition = ignition,

                    map = map,

                    iat = iat

                )

                reset()

                return result

            }

        }

        return null

    }

    private fun reset() {

        state = State.WAIT_HEADER
        syncCounter = 0
        syncBuffer.clear()

    }

    private fun startSync(header: String, target: Int) {
        state = State.SYNC_DATA
        syncTarget = target
        syncCounter = 0
        currentHeader = header
        syncBuffer.clear()
    }

    private fun isHeader(token: String): Boolean {
        return token == ECUProtocol.VARIABLE_HEADER ||
                token == ECUProtocol.MODEL_HEADER ||
                token == ECUProtocol.FUELMAP_HEADER ||
                token == ECUProtocol.BASEMAP_HEADER ||
                token == ECUProtocol.INJECTOR_HEADER ||
                token == ECUProtocol.IGNITION_HEADER ||
                token == ECUProtocol.HISTORY_HEADER ||
                token == ECUProtocol.REMOTE_READY_HEADER ||
                token.equals(ECUProtocol.LIVE_HEADER, true)
    }

    private fun adcToMap(adc: Double): Int {

        val volt =
            adc * 5.0 / 1023.0

        val hasil = when {

            volt < 0.3 ->
                volt * 100 / 3

            volt < 0.6 ->
                ((volt - 0.3) * 100 / 3) + 10

            volt < 1.1 ->
                ((volt - 0.6) * 100 / 5) + 20

            volt < 1.7 ->
                ((volt - 1.1) * 100 / 6) + 30

            volt < 2.2 ->
                ((volt - 1.7) * 100 / 5) + 40

            volt < 2.7 ->
                ((volt - 2.2) * 100 / 5) + 50

            volt < 3.3 ->
                ((volt - 2.7) * 100 / 6) + 60

            volt < 3.8 ->
                ((volt - 3.3) * 100 / 5) + 70

            volt < 4.4 ->
                ((volt - 3.8) * 100 / 6) + 80

            volt < 4.9 ->
                ((volt - 4.4) * 100 / 5) + 90

            else ->
                100.0

        }

        return hasil.roundToInt()

    }

}
