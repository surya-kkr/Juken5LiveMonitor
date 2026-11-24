package com.kkr.juken5livemonitor.protocol

import com.kkr.juken5livemonitor.model.ECULiveData
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Parser for the Juken 5+ A603 frame documented by the companion PC software.
 *
 * A Juken 5+ live frame has ten values after A603:
 * TPS, VBAT, core, RPM, EOT, AFR, MAP, IAT, IPump and IInj.
 * It is intentionally separate from [PacketParser], whose Juken 5 frame has a
 * different field count and meaning after EOT.
 */
class Juken5PlusPacketParser {
    private enum class State { WAIT_HEADER, SYNC_DATA, LIVE_DATA }

    private var state = State.WAIT_HEADER
    private var currentHeader = ""
    private var expectedValues = 0
    private val buffer = mutableListOf<String>()
    private var onChunkComplete: ((String, List<String>) -> Unit)? = null

    fun setSyncListener(callback: (String, List<String>) -> Unit) {
        onChunkComplete = callback
    }

    fun parse(token: String): ECULiveData? {
        val data = token.trim()
        if (data.isEmpty()) return null

        if (data.equals(ECUProtocol.PING_RESPONSE, ignoreCase = true)) {
            onChunkComplete?.invoke(ECUProtocol.PING_RESPONSE, listOf(data))
            return null
        }
        if (data.equals(ECUProtocol.ACK, ignoreCase = true)) {
            onChunkComplete?.invoke(ECUProtocol.ACK, listOf(data))
            return null
        }

        when (state) {
            State.WAIT_HEADER -> when {
                data.equals(ECUProtocol.LIVE_HEADER, ignoreCase = true) -> start(ECUProtocol.LIVE_HEADER, 10, State.LIVE_DATA)
                data == ECUProtocol.VARIABLE_HEADER -> start(data, 73, State.SYNC_DATA)
                data == ECUProtocol.MODEL_HEADER -> start(data, 3, State.SYNC_DATA)
                data == ECUProtocol.FUELMAP_HEADER -> start(data, 61, State.SYNC_DATA)
                data == ECUProtocol.BASEMAP_HEADER -> start(data, 61, State.SYNC_DATA)
                data == ECUProtocol.INJECTOR_HEADER -> start(data, 61, State.SYNC_DATA)
                data == ECUProtocol.IGNITION_HEADER -> start(data, 31, State.SYNC_DATA)
                data == ECUProtocol.HISTORY_HEADER -> start(data, 61, State.SYNC_DATA)
                data == ECUProtocol.REMOTE_READY_HEADER -> onChunkComplete?.invoke(data, listOf(data))
            }

            State.SYNC_DATA -> {
                if (isHeader(data)) {
                    completeSync()
                    return parse(data)
                }
                buffer += data
                if (buffer.size >= expectedValues) completeSync()
            }

            State.LIVE_DATA -> {
                if (isHeader(data)) {
                    reset()
                    return parse(data)
                }
                buffer += data
                if (buffer.size >= expectedValues) {
                    val frame = buffer.toList()
                    reset()
                    return parseLiveFrame(frame)
                }
            }
        }
        return null
    }

    private fun start(header: String, expected: Int, nextState: State) {
        currentHeader = header
        expectedValues = expected
        buffer.clear()
        state = nextState
    }

    private fun completeSync() {
        val header = currentHeader
        val values = buffer.toList()
        reset()
        onChunkComplete?.invoke(header, values)
    }

    private fun parseLiveFrame(values: List<String>): ECULiveData {
        fun valueAt(index: Int) = values.getOrNull(index)?.toDoubleOrNull() ?: 0.0

        val core = valueAt(2).roundToInt()
        val iPumpMs = valueAt(8) / 1000.0
        val iInjMs = valueAt(9) / 1000.0

        return ECULiveData(
            tps = valueAt(0).roundToInt().coerceIn(0, 100),
            battery = valueAt(1),
            core = core,
            rpm = valueAt(3).roundToInt(),
            eot = valueAt(4) / 10.0,
            // Existing themes use fuel to show Core 1/2. The Juken 5+ frame
            // provides the core explicitly, so normalize it for those themes.
            fuel = if (core == 2) "1" else "0",
            afr = valueAt(5),
            baseMap = "Juken 5+",
            // The EXE names fields 9/10 IPump and IInj. IInj is the closest
            // equivalent to the existing dashboard's injector-duration field.
            injector = String.format(Locale.US, "%.3f ms", iInjMs),
            ignition = 0.0,
            map = (valueAt(6) / 10.0).roundToInt(),
            iat = valueAt(7) / 10.0
        )
    }

    private fun isHeader(value: String): Boolean =
        value.equals(ECUProtocol.LIVE_HEADER, ignoreCase = true) ||
                value == ECUProtocol.VARIABLE_HEADER ||
                value == ECUProtocol.MODEL_HEADER ||
                value == ECUProtocol.FUELMAP_HEADER ||
                value == ECUProtocol.BASEMAP_HEADER ||
                value == ECUProtocol.INJECTOR_HEADER ||
                value == ECUProtocol.IGNITION_HEADER ||
                value == ECUProtocol.HISTORY_HEADER ||
                value == ECUProtocol.REMOTE_READY_HEADER

    private fun reset() {
        state = State.WAIT_HEADER
        currentHeader = ""
        expectedValues = 0
        buffer.clear()
    }
}
