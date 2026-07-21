package com.kkr.juken5livemonitor.protocol

object ECUProtocol {

    /*
     * ============================================================
     * LIVE MONITOR
     * ============================================================
     */

    /** Open Live Monitor */
    const val OPEN_LIVE = "160A\r\n"

    /** Stop Live Monitor */
    const val STOP_LIVE = "160B\r\n"

    /** Execute / Apply Live Changes */
    const val EXECUTE = "5009\r\n"

    /*
     * ============================================================
     * DIAGNOSTIC
     * ============================================================
     */

    /** Request Diagnostic Data */
    const val DIAGNOSTIC = "4602\r\n"

    /*
     * ============================================================
     * ECU INFORMATION
     * ============================================================
     */

    /** Download ECU Variables */
    const val GET_VARIABLE = "1607\r\n"

    /** Continue Download Map */
    const val GET_MAP = "1617\r\n"

    /*
     * ============================================================
     * ECU RESPONSE HEADER
     * ============================================================
     */

    const val ACK = "1A00"

    const val LIVE_HEADER = "A603"

    const val DIAGNOSTIC_HEADER = "A602"

    const val VARIABLE_HEADER = "9606"

    const val MODEL_HEADER = "9616"

    const val BASEMAP_HEADER = "9601"

    const val FUELMAP_HEADER = "9602"

    const val INJECTOR_HEADER = "9603"

    const val IGNITION_HEADER = "9605"

    const val HISTORY_HEADER = "9610"

    const val AUTO_MAPPING_HEADER = "9614"

}