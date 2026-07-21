package com.kkr.juken5livemonitor.logger

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


object ECULogger {

    private const val MAX_LOG = 300

    private val _logs =
        MutableStateFlow<List<String>>(emptyList())

    val logs =
        _logs.asStateFlow()

    fun tx(command: String) {

        add("[TX] ${command.replace("\r", "\\r").replace("\n", "\\n")}")

    }

    fun rx(packet: String) {

        add("[RX] $packet")

    }

    fun info(text: String) {

        add("[INFO] $text")

    }

    fun error(text: String) {

        add("[ERROR] $text")

    }

    fun clear() {

        _logs.value = emptyList()

    }

    private fun add(text: String) {

        val list =
            (_logs.value + text)
                .takeLast(MAX_LOG)

        _logs.value = list

    }

}