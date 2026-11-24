package com.kkr.juken5livemonitor.repository

import com.kkr.juken5livemonitor.model.ECULiveData

interface ReadOnlyEcuRepository {
    fun isConnected(): Boolean
    fun handshake(): Boolean
    fun requestModel(): Boolean
    fun requestFuelMap(offset: String, chunk: Int): Boolean
    fun requestBaseMap(offset: String, chunk: Int): Boolean
    fun requestInjectorMap(offset: String, chunk: Int): Boolean
    fun requestIgnitionMap(offset: String, chunk: Int): Boolean
    fun requestHistory(chunk: Int): Boolean
    fun setSyncListener(callback: (String, List<String>) -> Unit)
    fun setOnDisconnectListener(callback: () -> Unit)
    fun setLiveListener(callback: (ECULiveData) -> Unit)
    fun startListening()
    fun stopListening()
    fun openLive(): Boolean
    fun stopLive(): Boolean
    suspend fun stopLiveGracefully()
    fun send(command: String): Boolean
    fun disconnect()
}
