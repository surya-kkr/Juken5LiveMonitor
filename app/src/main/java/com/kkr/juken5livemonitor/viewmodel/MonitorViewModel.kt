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
import android.content.Context
import com.kkr.juken5livemonitor.location.GPSSpeedProvider
import android.util.Log

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
    private var gpsProvider: GPSSpeedProvider? = null

    fun startMonitor() {

        if (monitoring)
            return

        monitoring = true

        viewModelScope.launch(Dispatchers.IO) {

            Log.d("FLOW", "===== START MONITOR =====")

            if (!repository.isConnected()) {

                _status.value = "ECU Not Connected"

                monitoring = false

                return@launch

            }

            Log.d("FLOW", "Send Handshake (4602)")
            repository.handshake()

            delay(300)

            Log.d("FLOW", "Set Listener")
            repository.setLiveListener { data ->

                Log.d("FLOW", "LIVE DATA : $data")

                _liveData.value = data.copy(
                    speed = _liveData.value.speed
                )
            }

            Log.d("FLOW", "Start Listening")
            repository.startListening()

            delay(200)

            Log.d("FLOW", "Send Open Live (160A)")
            repository.openLive()

            Log.d("FLOW", "Waiting ECU Stream...")
        }
    }
    fun startGPS(context: Context) {

        if (gpsProvider == null) {
            gpsProvider = GPSSpeedProvider(context)
        }

        gpsProvider?.start { speed ->

            _liveData.value = _liveData.value.copy(
                speed = speed
            )

        }

    }
    fun stopGPS() {
        gpsProvider?.stop()
    }
    fun disconnect() {

        monitoring = false

        _status.value = "Disconnected"

        stopGPS()

        repository.disconnect()

    }

    override fun onCleared() {

        super.onCleared()

        disconnect()

    }

}