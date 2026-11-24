package com.kkr.juken5livemonitor.protocol

/**
 * Protocol constants specific to Juken 5 Plus ECU communications.
 * These commands are distinct from the generic ECUProtocol.
 */
object ECUProtocolJuken5Plus {
    /**
     * Juken 5+ PC software sequence: stop any previous live stream, request
     * parameters (1606), then request the ECU identity (1616).
     */
    const val REQUEST_PARAMETERS = "1606\r\n"
    const val GET_MODEL = "1616\r\n"
    const val REQUEST_REMOTE_READY = "1618\r\n"
    const val REQUEST_BASE_MAP = "1601"
    const val REQUEST_FUEL_MAP = "1602"
    const val REQUEST_INJECTOR_MAP = "1603"
    const val REQUEST_IGNITION_MAP = "1605"
    const val REQUEST_HISTORY = "1610"
    const val OPEN_LIVE = "160A\r\n"
    const val STOP_LIVE = "160B\r\n"
    const val PING = "1B00\r\n"
}
