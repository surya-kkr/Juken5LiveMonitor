package com.kkr.juken5livemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.repository.ECURepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MonitorViewModel : ViewModel() {

    private val repository = ECURepository()

    private val _liveData =
        MutableStateFlow(ECULiveData())

    val liveData =
        _liveData.asStateFlow()

    private val _status =
        MutableStateFlow("Disconnected")

    val status =
        _status.asStateFlow()

    private var monitoring = false

    fun startMonitor() {

        if (monitoring)
            return

        monitoring = true

        viewModelScope.launch(Dispatchers.IO) {

            if (!repository.isConnected()) {

                _status.value = "ECU Not Connected"

                monitoring = false

                return@launch

            }

            _status.value = "Handshake..."

            repository.handshake()

            delay(300)

            repository.setLiveListener { data ->

                _liveData.value = data

            }

            repository.startListening()

            delay(200)

            repository.openLive()

            _status.value = "Live Monitoring"

        }

    }

    fun disconnect() {

        monitoring = false

        _status.value = "Disconnected"

        repository.disconnect()

    }

    override fun onCleared() {

        super.onCleared()

        disconnect()

    }

}