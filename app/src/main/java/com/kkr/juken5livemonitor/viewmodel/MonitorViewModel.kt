package com.kkr.juken5livemonitor.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.kkr.juken5livemonitor.data.db.LogDatabase
import com.kkr.juken5livemonitor.data.db.LogEntry
import com.kkr.juken5livemonitor.data.db.LogSession
import com.kkr.juken5livemonitor.location.GPSSpeedProvider
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.repository.ECURepository
import com.kkr.juken5livemonitor.repository.ECURepositoryJuken5Plus
import com.kkr.juken5livemonitor.repository.ReadOnlyEcuRepository
import com.kkr.juken5livemonitor.protocol.ECUProtocol
import com.kkr.juken5livemonitor.protocol.ECUProtocolJuken5Plus
import com.kkr.juken5livemonitor.screen.monitor.center.CenterMode
import com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine.AndroidAutoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ECURepository()
    private val juken5PlusRepository = ECURepositoryJuken5Plus()
    private val prefs = application.getSharedPreferences("juken_settings", Context.MODE_PRIVATE)
    private val db = LogDatabase.getDatabase(application)
    private val logDao = db.logDao()

    // Android Auto Engine (Shared across UI changes)
    private val _aaEngine = AndroidAutoEngine(application)
    val aaEngine get() = _aaEngine

    private var audioFocusRequest: AudioFocusRequest? = null

    private fun activeRepository(): ReadOnlyEcuRepository =
        if (prefs.getString("selected_juken_type", "Juken 5") == "Juken 5+") {
            juken5PlusRepository
        } else {
            repository
        }
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun requestAaAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attr)
                .setOnAudioFocusChangeListener { /* handle change */ }
                .build()
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus({ }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    fun abandonAaAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus { }
        }
    }

    // =========================
    // Flows
    // =========================

    private val _liveData = MutableStateFlow(ECULiveData())
    val liveData = _liveData.asStateFlow()

    private val _currentLocation = MutableStateFlow(LatLng(-6.200000, 106.816666))
    val currentLocation: StateFlow<LatLng> = _currentLocation.asStateFlow()

    private val _bearing = MutableStateFlow(0f)
    val bearing: StateFlow<Float> = _bearing.asStateFlow()

    private val _status = MutableStateFlow("Disconnected")
    val status = _status.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    private val _reconnectingStatus = MutableStateFlow<String?>(null)
    val reconnectingStatus = _reconnectingStatus.asStateFlow()

    private val _cityName = MutableStateFlow("Searching...")
    val cityName = _cityName.asStateFlow()

    private val _showDownloadFailed = MutableStateFlow(false)
    val showDownloadFailed = _showDownloadFailed.asStateFlow()

    private val _bikeName = MutableStateFlow(prefs.getString("bike_name", "YAMAHA R25") ?: "YAMAHA R25")
    val bikeName = _bikeName.asStateFlow()

    private val _selectedUiPortrait = MutableStateFlow(prefs.getInt("selected_ui_portrait", 0))
    val selectedUiPortrait = _selectedUiPortrait.asStateFlow()

    private val _selectedUiLandscape = MutableStateFlow(prefs.getInt("selected_ui_landscape", 0))
    val selectedUiLandscape = _selectedUiLandscape.asStateFlow()

    private val _centerMode = MutableStateFlow<CenterMode>(CenterMode.AndroidAuto)
    val centerMode = _centerMode.asStateFlow()

    private val _aaNightMode = MutableStateFlow(prefs.getBoolean("aa_night_mode", true))
    val aaNightMode = _aaNightMode.asStateFlow()

    private val _aaDpi = MutableStateFlow(prefs.getInt("aa_dpi", 140))
    val aaDpi = _aaDpi.asStateFlow()

    private val _overheatThreshold = MutableStateFlow(prefs.getInt("overheat_threshold", 105))
    val overheatThreshold = _overheatThreshold.asStateFlow()

    private val _isOverheatDismissed = MutableStateFlow(false)
    val isOverheatDismissed = _isOverheatDismissed.asStateFlow()

    private val _autoReconnectEnabled = MutableStateFlow(prefs.getBoolean("auto_reconnect_enabled", true))
    val autoReconnectEnabled = _autoReconnectEnabled.asStateFlow()

    private val _showDisconnectedPopup = MutableStateFlow(false)
    val showDisconnectedPopup = _showDisconnectedPopup.asStateFlow()

    private val _connectedPhoneName = MutableStateFlow("")
    val connectedPhoneName = _connectedPhoneName.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _showRecordStartPopup = MutableStateFlow(false)
    val showRecordStartPopup = _showRecordStartPopup.asStateFlow()

    private val _showRecordStopPopup = MutableStateFlow(false)
    val showRecordStopPopup = _showRecordStopPopup.asStateFlow()

    private val _lastSavedLogTitle = MutableStateFlow("")
    val lastSavedLogTitle = _lastSavedLogTitle.asStateFlow()

    private val _recordingDurationMillis = MutableStateFlow(0L)
    val recordingDuration: StateFlow<String> = _recordingDurationMillis.map { millis ->
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60))
        "%02d:%02d".format(minutes, seconds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "00:00")

    private var currentSessionId: Long? = null
    private var recordingStartTime: Long = 0
    private var recordingJob: kotlinx.coroutines.Job? = null
    private var pingJob: kotlinx.coroutines.Job? = null
    private var reconnectJob: kotlinx.coroutines.Job? = null

    private val _vehicleWeight = MutableStateFlow(prefs.getFloat("vehicle_weight", 110f))
    val vehicleWeight = _vehicleWeight.asStateFlow()

    private val _riderWeight = MutableStateFlow(prefs.getFloat("rider_weight", 70f))
    val riderWeight = _riderWeight.asStateFlow()

    private val _estHorsePower = MutableStateFlow(0.0)
    val estHorsePower = _estHorsePower.asStateFlow()

    private val _engineRuntimeMillis = MutableStateFlow(0L)
    val engineRuntime: StateFlow<String> = _engineRuntimeMillis.map { millis ->
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "00:00:00")

    private val _topSpeed = MutableStateFlow(0f)
    val topSpeed = _topSpeed.asStateFlow()

    private val _avgSpeed = MutableStateFlow(0f)
    val avgSpeed = _avgSpeed.asStateFlow()

    enum class TimerStatus { IDLE, COUNTDOWN, READY, RUNNING, FINISHED, COOLDOWN }
    private val _zeroToHundredStatus = MutableStateFlow(TimerStatus.IDLE)
    val zeroToHundredStatus = _zeroToHundredStatus.asStateFlow()

    private val _countdownValue = MutableStateFlow("")
    val countdownValue = _countdownValue.asStateFlow()

    private val _cooldownRemaining = MutableStateFlow(0)
    val cooldownRemaining = _cooldownRemaining.asStateFlow()

    private val _zeroToHundredTime = MutableStateFlow(0L)
    val zeroToHundredResult: StateFlow<String> = _zeroToHundredTime.map { 
        "%.2fs".format(it / 1000f) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0.00s")

    private val _performanceResult = MutableStateFlow<PerformanceResult?>(null)
    val performanceResult = _performanceResult.asStateFlow()

    private val _drivetrainType = MutableStateFlow(
        try {
            DrivetrainType.valueOf(prefs.getString("drivetrain_type", DrivetrainType.MANUAL_MOTO.name) ?: DrivetrainType.MANUAL_MOTO.name)
        } catch (e: Exception) {
            DrivetrainType.MANUAL_MOTO
        }
    )
    val drivetrainType = _drivetrainType.asStateFlow()

    enum class DrivetrainType(val efficiency: Float) {
        CVT_SCOOTER(1.18f),
        MANUAL_MOTO(1.12f),
        RWD_CAR(1.15f),
        FWD_CAR(1.13f)
    }

    data class PerformanceResult(
        val time: String = "0.00s",
        val avgAccel: Double = 0.0,
        val wheelPower: Double = 0.0,
        val enginePower: Double = 0.0
    )

    private var speedSum = 0f
    private var speedCount = 0
    private var engineStartTime = 0L
    private var timerStartTime = 0L
    private var lastTimerTime = 0L
    private var lastTimerSpeed = 0f
    private var timerJob: kotlinx.coroutines.Job? = null
    private val fuelHistory = mutableListOf<Pair<String, Long>>()

    private fun detectFuelTrigger(fuel: String) {
        val now = System.currentTimeMillis()
        if (fuelHistory.isNotEmpty() && fuelHistory.last().first == fuel) return
        fuelHistory.add(fuel to now)
        if (fuelHistory.size > 4) fuelHistory.removeAt(0)
        if (fuelHistory.size == 4) {
            val states = fuelHistory.map { it.first }
            val startTime = fuelHistory.first().second
            val endTime = fuelHistory.last().second
            if (states == listOf("0", "1", "0", "1") && (endTime - startTime) <= 1000) {
                Log.d("FUEL_TRIGGER", "Auto-Arming 0-100 Timer via Fuel Switch Pattern detected")
                armZeroToHundred()
                fuelHistory.clear()
            }
        }
    }

    fun armZeroToHundred() {
        if (_zeroToHundredStatus.value == TimerStatus.COOLDOWN) return
        if (_zeroToHundredStatus.value == TimerStatus.FINISHED) {
            _zeroToHundredStatus.value = TimerStatus.IDLE
            _performanceResult.value = null
            return
        }
        if (_zeroToHundredStatus.value == TimerStatus.COUNTDOWN || 
            _zeroToHundredStatus.value == TimerStatus.READY || 
            _zeroToHundredStatus.value == TimerStatus.RUNNING) {
            triggerCancelAndCooldown()
            return
        }

        timerJob = viewModelScope.launch {
            _zeroToHundredStatus.value = TimerStatus.COUNTDOWN
            _zeroToHundredTime.value = 0L
            _estHorsePower.value = 0.0
            for (i in 5 downTo 1) {
                _countdownValue.value = i.toString()
                delay(1000)
            }
            _countdownValue.value = "START!"
            _zeroToHundredStatus.value = TimerStatus.READY
            delay(1000)
            _countdownValue.value = ""
        }
    }

    private fun triggerCancelAndCooldown() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            _zeroToHundredStatus.value = TimerStatus.IDLE
            _countdownValue.value = "CANCEL"
            delay(1000)
            _countdownValue.value = ""
        }
    }

    fun startRecording() {
        if (_isRecording.value) return
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val title = sdf.format(java.util.Date(timestamp))
            _lastSavedLogTitle.value = title
            currentSessionId = logDao.insertSession(LogSession(timestamp = timestamp, title = title))
            recordingStartTime = timestamp
            _recordingDurationMillis.value = 0L
            _isRecording.value = true
            
            // Show start popup for 3 seconds
            _showRecordStartPopup.value = true
            viewModelScope.launch {
                delay(3000)
                _showRecordStartPopup.value = false
            }
            
            recordingJob?.cancel()
            recordingJob = viewModelScope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordingDurationMillis.value = System.currentTimeMillis() - recordingStartTime
                }
            }
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        val sessionId = currentSessionId
        val finalDuration = System.currentTimeMillis() - recordingStartTime
        
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        currentSessionId = null
        
        // Save the final duration to DB
        if (sessionId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                logDao.updateSessionDuration(sessionId, finalDuration)
            }
        }
        
        // Show stop popup for 3 seconds
        _showRecordStopPopup.value = true
        viewModelScope.launch {
            delay(3000)
            _showRecordStopPopup.value = false
        }
    }

    private fun captureLog(data: ECULiveData) {
        val sessionId = currentSessionId ?: return
        if (!_isRecording.value) return
        
        val offset = System.currentTimeMillis() - recordingStartTime
        
        // Limit to 10 minutes (600,000 ms)
        if (offset > 10 * 60 * 1000) {
            Log.i("MONITOR_VM", "Recording reached 10 minute limit. Stopping.")
            stopRecording()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            logDao.insertEntry(
                LogEntry(
                    sessionId = sessionId,
                    timeOffset = offset,
                    battery = data.battery,
                    rpm = data.rpm,
                    speed = data.speed,
                    baseMap = data.baseMap,
                    ignition = data.ignition,
                    map = data.map,
                    iat = data.iat,
                    tps = data.tps,
                    afr = data.afr,
                    eot = data.eot,
                    fuel = data.fuel,
                    injector = data.injector
                )
            )
        }
    }

    fun setCenterMode(mode: CenterMode) {
        if (_centerMode.value == mode) return
        _centerMode.value = mode
    }

    fun setPhoneName(name: String) {
        _connectedPhoneName.value = name
    }

    fun toggleAANightMode(isNight: Boolean) {
        _aaNightMode.value = isNight
        prefs.edit().putBoolean("aa_night_mode", isNight).apply()
    }

    fun updateAaDpi(dpi: Int) {
        _aaDpi.value = dpi
        prefs.edit().putInt("aa_dpi", dpi).apply()
    }

    fun updateOverheatThreshold(threshold: Int) {
        _overheatThreshold.value = threshold
        prefs.edit().putInt("overheat_threshold", threshold).apply()
    }

    fun updateAutoReconnect(enabled: Boolean) {
        _autoReconnectEnabled.value = enabled
        prefs.edit().putBoolean("auto_reconnect_enabled", enabled).apply()
    }

    fun dismissDisconnectedPopup() {
        _showDisconnectedPopup.value = false
    }

    fun cancelReconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        _reconnectingStatus.value = null
        _status.value = "Disconnected"
        _showDisconnectedPopup.value = true
    }

    fun triggerManualReconnect() {
        _showDisconnectedPopup.value = false
        currentDeviceAddress?.let { address ->
            handleDisconnect(address)
        }
    }

    fun dismissOverheatWarning() {
        _isOverheatDismissed.value = true
    }

    fun resetOverheatDismiss() {
        _isOverheatDismissed.value = false
    }

    fun updateBikeName(name: String) {
        _bikeName.value = name
        prefs.edit().putString("bike_name", name).apply()
    }

    fun updateSelectedUi(index: Int, isLandscape: Boolean) {
        if (isLandscape) {
            _selectedUiLandscape.value = index
            prefs.edit().putInt("selected_ui_landscape", index).apply()
        } else {
            _selectedUiPortrait.value = index
            prefs.edit().putInt("selected_ui_portrait", index).apply()
        }
        
        // HUR Fix: Automatically activate Android Auto when entering dashboard themes (Landscape only usually)
        if (isLandscape && (index == 0 || index == 1)) {
            _centerMode.value = CenterMode.AndroidAuto
        }
    }

    fun updateVehicleSettings(vehicle: Float, rider: Float, drivetrain: DrivetrainType) {
        _vehicleWeight.value = vehicle
        _riderWeight.value = rider
        _drivetrainType.value = drivetrain
        prefs.edit().apply {
            putFloat("vehicle_weight", vehicle)
            putFloat("rider_weight", rider)
            putString("drivetrain_type", drivetrain.name)
            apply()
        }
    }

    private var monitoring = false
    private var demoJob: kotlinx.coroutines.Job? = null
    private var gpsProvider: GPSSpeedProvider? = null
    private var currentDeviceAddress: String? = null
    private var consecutiveFailures = 0

    fun startDemoMode() {
        if (monitoring) return
        monitoring = true
        _status.value = "Demo Dashboard"
        _cityName.value = "Sentul Circuit" // Simulated location for Demo Mode
        demoJob = viewModelScope.launch(Dispatchers.Default) {
            var currentRpm = 1000f
            var currentSpeed = 0f
            var currentBattery = 12.2f
            var currentEot = 75.0f
            var fuelCounter = 0
            while (monitoring) {
                val rpmDelta = (Math.random().toFloat() * 400f) - 150f
                currentRpm = (currentRpm + rpmDelta).coerceIn(1000f, 11500f)
                val speedDelta = (Math.random().toFloat() * 4f) - 1.5f
                currentSpeed = (currentSpeed + speedDelta).coerceIn(0f, 180f)
                currentBattery = (13.8f + (Math.random().toFloat() * 0.4f - 0.2f))
                currentEot = (85f + (Math.random().toFloat() * 10f - 5f))
                if (currentSpeed > _topSpeed.value) _topSpeed.value = currentSpeed
                _liveData.value = ECULiveData(
                    rpm = currentRpm.toInt(),
                    speed = currentSpeed,
                    battery = currentBattery.toDouble(),
                    tps = (currentRpm / 120).toInt().coerceIn(0, 100),
                    afr = 12.5 + (Math.random() * 2.0),
                    eot = currentEot.toDouble(),
                    iat = 32.0,
                    ignition = 15.0 + (currentRpm / 1000),
                    injector = "%.2f ms".format(1.5 + (currentRpm / 5000)),
                    fuel = if (fuelCounter % 10 == 0) "1" else "0",
                    baseMap = "Map 1"
                )
                processZeroToHundred(currentSpeed)
                if (_isRecording.value) captureLog(_liveData.value)
                fuelCounter++
                delay(100)
            }
        }
    }

    private fun processZeroToHundred(currentSpeedKph: Float) {
        when (_zeroToHundredStatus.value) {
            TimerStatus.READY -> {
                if (currentSpeedKph > 0.5f) {
                    timerStartTime = System.currentTimeMillis()
                    lastTimerTime = timerStartTime
                    lastTimerSpeed = currentSpeedKph / 3.6f
                    _zeroToHundredStatus.value = TimerStatus.RUNNING
                }
            }
            TimerStatus.RUNNING -> {
                val now = System.currentTimeMillis()
                val elapsed = now - timerStartTime
                _zeroToHundredTime.value = elapsed
                val currentSpeedMs = currentSpeedKph / 3.6f
                val deltaTime = (now - lastTimerTime) / 1000f
                if (deltaTime > 0.1f) {
                    val deltaV = currentSpeedMs - lastTimerSpeed
                    val accel = deltaV / deltaTime
                    if (accel > 0) {
                        val totalMass = _vehicleWeight.value + _riderWeight.value
                        val powerWatts = (totalMass * accel) * currentSpeedMs
                        val hpWheel = powerWatts / 745.7
                        val hpEngine = hpWheel * _drivetrainType.value.efficiency
                        if (hpEngine > _estHorsePower.value) _estHorsePower.value = hpEngine
                    }
                    lastTimerTime = now
                    lastTimerSpeed = currentSpeedMs
                }
                if (currentSpeedKph >= 100f) {
                    _zeroToHundredStatus.value = TimerStatus.FINISHED
                    val timeSeconds = elapsed / 1000f
                    if (timeSeconds > 0) {
                        val totalMass = _vehicleWeight.value + _riderWeight.value
                        val vFinal = 100.0 / 3.6
                        val avgAccel = vFinal / timeSeconds
                        val force = totalMass * avgAccel
                        val powerWatts = force * vFinal
                        val hpWheel = powerWatts / 745.7
                        val hpEngine = hpWheel * _drivetrainType.value.efficiency
                        if (hpEngine > _estHorsePower.value) _estHorsePower.value = hpEngine
                        _performanceResult.value = PerformanceResult(
                            time = "%.2fs".format(timeSeconds),
                            avgAccel = avgAccel,
                            wheelPower = hpWheel,
                            enginePower = hpEngine
                        )
                    }
                } else if (currentSpeedKph < 0.2f && elapsed > 2000) {
                    _zeroToHundredStatus.value = TimerStatus.IDLE
                }
            }
            else -> {}
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, bearing: Float = 0f) {
        _currentLocation.value = LatLng(latitude, longitude)
        _bearing.value = bearing
    }

    fun startMonitor() {
        if (monitoring) return
        monitoring = true
        viewModelScope.launch(Dispatchers.IO) {

            val selectedJukenType = prefs.getString("selected_juken_type", "Juken 5") ?: "Juken 5"
            val lastAddress = prefs.getString("last_device_address", null)
            currentDeviceAddress = lastAddress
            if (lastAddress == null) {
                _status.value = "Device not paired"
                monitoring = false
                return@launch
            }

            // Setup response channel for the entire connection lifecycle
            val responseChannel = kotlinx.coroutines.channels.Channel<Pair<String, List<String>>>(capacity = 10)
            activeRepository().setSyncListener { header, data ->
                viewModelScope.launch { responseChannel.send(header to data) }
            }

            // 1. Initial Connection
            if (!activeRepository().isConnected()) {
                if (!reconnect(lastAddress)) {
                    Log.e("MONITOR_VM", "Initial connection failed")
                    return@launch
                }
            }

            activeRepository().setOnDisconnectListener {
                if (monitoring && _status.value == "Live Connected") {
                    onEcuConnectionLost(lastAddress)
                }
            }

            if (selectedJukenType == "Juken 5+") {
                startJuken5PlusMonitor(responseChannel, lastAddress)
                return@launch
            }

            // 1.5 Passive Detection Phase (Fast Resume)
            _status.value = "Checking ECU..."
            _syncStatus.value = "Checking ECU State..."
            activeRepository().startListening()
            
            val isAlreadyStreaming = withTimeoutOrNull(2000) { // Increased to 2s
                var res: Pair<String, List<String>>? = null
                while(res?.first != com.kkr.juken5livemonitor.protocol.ECUProtocol.LIVE_HEADER) {
                    res = responseChannel.receive()
                }
                res
            } != null

            if (isAlreadyStreaming) {
                Log.i("SYNC", "ECU is already streaming live data. Fast Resume activated.")
                enterLiveMode()
                return@launch
            }

            Log.i("SYNC", "No live data detected. Proceeding with full synchronization.")

            // 2. Perform Deep Synchronization (Normal Path)
            _status.value = "Initializing ECU..."
            _syncStatus.value = "Stopping previous streams..."
            activeRepository().stopLiveGracefully()
            delay(1500) // Increased delay

            val syncSuccess = performDeepSync(responseChannel)
            
            if (!syncSuccess) {
                _status.value = "Sync Failed"
                _syncStatus.value = null
                _showDownloadFailed.value = true
                monitoring = false
                activeRepository().stopListening() // Clean up
                return@launch
            }

            // 3. Reset Connection (Mandatory for ECU to switch to Live mode)
            _status.value = "Resetting Connection..."
            _syncStatus.value = "Preparing Live Mode..."
            activeRepository().stopListening()
            com.kkr.juken5livemonitor.session.BluetoothSession.bluetoothManager?.disconnect()
            delay(2000) // Increased delay for Xiaomi stability

            // 4. Reconnect for Live
            _status.value = "Reconnecting for Live..."
            if (!reconnect(lastAddress)) {
                Log.e("MONITOR_VM", "Reconnect for Live failed")
                return@launch
            }

            // Re-setup listeners for the new connection session
            activeRepository().setSyncListener { header, data ->
                viewModelScope.launch { responseChannel.send(header to data) }
            }

            // 5. Start Live Stream
            _syncStatus.value = "Starting Live Stream..."
            activeRepository().setLiveListener { data ->
                processIncomingLiveData(data)
            }

            activeRepository().startListening()
            delay(1000)
            activeRepository().openLive()
            
            // Wait for ACK 1A00
            val ack = withTimeoutOrNull(3000) {
                var res: Pair<String, List<String>>? = null
                while(res?.first != com.kkr.juken5livemonitor.protocol.ECUProtocol.ACK) {
                    res = responseChannel.receive()
                }
                res
            }
            
            if (ack == null) {
                Log.w("SYNC", "Did not receive ACK for Live command, proceeding anyway...")
            }

            _status.value = "Live Connected"
            _syncStatus.value = null

            // Start Heartbeat/Ping
            startPing()
        }
    }

    /**
     * Juken 5+ connection sequence reproduced from the companion PC software.
     * This monitor is read-only, so it intentionally does not download the PC
     * application's bulk calibration/map payload before entering live mode.
     */
    private suspend fun startJuken5PlusMonitor(
        responseChannel: kotlinx.coroutines.channels.Channel<Pair<String, List<String>>>,
        address: String
    ) {
        fun clearPendingResponses() {
            while (responseChannel.tryReceive().isSuccess) Unit
        }

        suspend fun awaitResponse(header: String, timeoutMs: Long): List<String>? =
            withTimeoutOrNull(timeoutMs) {
                var response: Pair<String, List<String>>
                do {
                    response = responseChannel.receive()
                } while (response.first != header)
                response.second
            }

        activeRepository().startListening()

        _status.value = "Checking Juken 5+..."
        _syncStatus.value = "Waiting for ECU response..."
        clearPendingResponses()
        activeRepository().send(ECUProtocolJuken5Plus.PING)
        if (awaitResponse(ECUProtocol.PING_RESPONSE, 4_000) == null) {
            failJuken5PlusConnection("ECU did not answer the Juken 5+ ping")
            return
        }

        _syncStatus.value = "Reading ECU parameters..."
        activeRepository().stopLive()
        delay(300)
        clearPendingResponses()
        activeRepository().handshake() // 1606
        val parameters = awaitResponse(ECUProtocol.VARIABLE_HEADER, 8_000)
        if (parameters == null) {
            // The PC tool uses 1618 as a remote-ready recovery request. It is
            // diagnostic only here; parameter data is still required to continue.
            activeRepository().send(ECUProtocolJuken5Plus.REQUEST_REMOTE_READY)
            awaitResponse(ECUProtocol.REMOTE_READY_HEADER, 2_000)
            failJuken5PlusConnection("No parameter response (9606) from ECU")
            return
        }

        _syncStatus.value = "Verifying ECU type..."
        clearPendingResponses()
        activeRepository().requestModel() // 1616
        val modelFields = awaitResponse(ECUProtocol.MODEL_HEADER, 5_000)
        if (modelFields == null) {
            failJuken5PlusConnection("ECU did not return identity data (9616)")
            return
        }

        val detectedType = modelFields
            .getOrNull(1)
            ?.trim()
            ?.replace("=", "")
            ?.trim()
            ?.split(Regex("\\s+"))
            ?.firstOrNull()
            ?.uppercase()
            .orEmpty()

        if (detectedType != "JUKEN5+") {
            val displayType = modelFields.joinToString(" ").trim().ifEmpty { "Unknown ECU" }
            failJuken5PlusConnection("Juken 5+ mode requires JUKEN5+; detected: $displayType")
            return
        }

        // Like the EXE's Live button, reconnect before starting the live stream.
        _status.value = "Preparing Juken 5+ live mode..."
        activeRepository().stopListening()
        com.kkr.juken5livemonitor.session.BluetoothSession.bluetoothManager?.disconnect()
        delay(1_000)
        if (!reconnect(address)) {
            failJuken5PlusConnection("Could not reconnect for live mode")
            return
        }

        activeRepository().setLiveListener { data -> processIncomingLiveData(data) }
        activeRepository().startListening()
        activeRepository().send("1617\r\n")
        delay(1_000)
        if (!activeRepository().openLive()) {
            failJuken5PlusConnection("Live command was not sent")
            return
        }

        _status.value = "Live Connected"
        _syncStatus.value = null
        startPing()
    }

    private fun failJuken5PlusConnection(reason: String) {
        Log.e("JUKEN5_PLUS", reason)
        _status.value = "Juken 5+ connection failed"
        _syncStatus.value = reason
        _showDownloadFailed.value = true
        monitoring = false
        activeRepository().stopListening()
        activeRepository().disconnect()
    }

    private fun enterLiveMode() {
        _status.value = "Live Connected"
        _syncStatus.value = null
        
        activeRepository().setLiveListener { data ->
            processIncomingLiveData(data)
        }
        
        // Start Heartbeat/Ping immediately to maintain the existing stream
        startPing()
    }

    private fun processIncomingLiveData(data: ECULiveData) {
        if (data.rpm > 0 && engineStartTime == 0L) {
            engineStartTime = System.currentTimeMillis()
            viewModelScope.launch {
                while (monitoring) {
                    _engineRuntimeMillis.value = System.currentTimeMillis() - engineStartTime
                    delay(1000)
                }
            }
        }
        detectFuelTrigger(data.fuel)
        val updatedData = data.copy(speed = _liveData.value.speed)
        _liveData.value = updatedData
        if (_isRecording.value) captureLog(updatedData)
    }

    private fun handleDisconnect(address: String) {
        if (reconnectJob?.isActive == true) return
        
        reconnectJob = viewModelScope.launch(Dispatchers.IO) {
            _status.value = "Reconnecting..."
            var retryCount = 1
            
            while (monitoring) {
                _reconnectingStatus.value = "Attempt $retryCount..."
                
                // 1. Reconnect Bluetooth
                if (reconnectInternal(address)) {
                    _reconnectingStatus.value = null
                    
                    // Setup response channel for sync
                    val responseChannel = kotlinx.coroutines.channels.Channel<Pair<String, List<String>>>(capacity = 10)
                    activeRepository().setSyncListener { header, data ->
                        viewModelScope.launch { responseChannel.send(header to data) }
                    }

                    // 2. Perform Full Deep Synchronization
                    _status.value = "Syncing..."
                    val syncSuccess = performDeepSync(responseChannel)
                    
                    if (!syncSuccess) {
                        _status.value = "Sync Failed"
                        _showDownloadFailed.value = true
                        // On failure, we don't break the loop, we retry from reconnection after a delay
                    } else {
                        // 3. Reset Connection (Mandatory for ECU)
                        _status.value = "Finalizing..."
                        _syncStatus.value = "Preparing Live..."
                        activeRepository().stopListening()
                        com.kkr.juken5livemonitor.session.BluetoothSession.bluetoothManager?.disconnect()
                        delay(1500)

                        // 4. Final Reconnect for Live
                        if (reconnectInternal(address)) {
                            _status.value = "Resuming Live..."
                            
                            activeRepository().setLiveListener { data ->
                                processIncomingLiveData(data)
                            }
                            
                            activeRepository().startListening()
                            delay(1000)
                            activeRepository().openLive()
                            
                            // Wait for ACK
                            withTimeoutOrNull(2000) {
                                var res: Pair<String, List<String>>? = null
                                while(res?.first != com.kkr.juken5livemonitor.protocol.ECUProtocol.ACK) {
                                    res = responseChannel.receive()
                                }
                                res
                            }

                            _status.value = "Live Connected"
                            _syncStatus.value = null
                            startPing()
                            return@launch
                        }
                    }
                }
                
                retryCount++
                delay(3000) // Wait before next retry
            }
            _reconnectingStatus.value = null
        }
    }

    private fun startPing() {
        pingJob?.cancel()
        pingJob = viewModelScope.launch(Dispatchers.IO) {
            while (monitoring) {
                val success = activeRepository().send(com.kkr.juken5livemonitor.protocol.ECUProtocol.PING)
                if (success) {
                    consecutiveFailures = 0
                } else {
                    consecutiveFailures++
                    if (consecutiveFailures >= 2) {
                        Log.e("MONITOR_VM", "PING failed twice. Triggering reconnection.")
                        currentDeviceAddress?.let { onEcuConnectionLost(it) }
                        break
                    }
                }
                delay(5000)
            }
        }
    }

    private fun onEcuConnectionLost(address: String) {
        if (_autoReconnectEnabled.value) {
            handleDisconnect(address)
        } else {
            _status.value = "Disconnected"
            _showDisconnectedPopup.value = true
        }
    }

    private fun reconnect(address: String): Boolean {
        val success = reconnectInternal(address)
        if (!success) monitoring = false
        return success
    }

    private fun reconnectInternal(address: String): Boolean {
        _status.value = "Connecting..."
        try {
            val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice(address)
            val manager = com.kkr.juken5livemonitor.bluetooth.BluetoothManager()
            if (manager.connect(device)) {
                com.kkr.juken5livemonitor.session.BluetoothSession.initialize(manager)
                return true
            }
        } catch (e: Exception) {
            _status.value = "Connect Error: ${e.message}"
        }
        return false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun performDeepSync(
        responseChannel: kotlinx.coroutines.channels.Channel<Pair<String, List<String>>>
    ): Boolean {
        activeRepository().startListening()

        // Phase 1: Get Variables & Offsets
        _syncStatus.value = "Downloading Variables..."
        delay(500) // Give hardware time to settle
        activeRepository().handshake() // Send 1607
        
        val vars = withTimeoutOrNull(15000) { // Increased timeout
            var result: Pair<String, List<String>>? = null
            while(result?.first != com.kkr.juken5livemonitor.protocol.ECUProtocol.VARIABLE_HEADER) {
                result = responseChannel.receive()
            }
            result
        } ?: run {
            Log.e("SYNC", "Failed to get variables (Timeout)")
            return false
        }
        
        // Extract Memory Offsets based on Core status (index 23 is Var 24 'tss')
        val tss = vars.second.getOrNull(23)?.trim() ?: "0"
        val isCore1 = tss == "0" || tss == "1"
        
        val bOffset = vars.second.getOrNull(if (isCore1) 24 else 28)?.trim() ?: "0"
        val fOffset = vars.second.getOrNull(if (isCore1) 25 else 29)?.trim() ?: "0"
        val itOffset = vars.second.getOrNull(if (isCore1) 26 else 30)?.trim() ?: "0"
        val igOffset = vars.second.getOrNull(if (isCore1) 27 else 31)?.trim() ?: "0"

        Log.d("SYNC", "Core Detection: tss=$tss, isCore1=$isCore1")
        Log.d("SYNC", "Offsets: B=$bOffset, F=$fOffset, IT=$itOffset, IG=$igOffset")

        // Phase 2: Get Model
        _syncStatus.value = "Checking ECU Model..."
        delay(1000) 
        activeRepository().requestModel() // Send 1617
        withTimeoutOrNull(5000) { 
            var result: Pair<String, List<String>>? = null
            while(result?.first != com.kkr.juken5livemonitor.protocol.ECUProtocol.MODEL_HEADER) {
                result = responseChannel.receive()
            }
            result
        }

        // Phase 3-7: Sequential Map Download (21 chunks each)
        val maps = listOf(
            Triple("Fuel Map", com.kkr.juken5livemonitor.protocol.ECUProtocol.FUELMAP_HEADER) { i: Int -> activeRepository().requestFuelMap(fOffset, i) },
            Triple("Base Map", com.kkr.juken5livemonitor.protocol.ECUProtocol.BASEMAP_HEADER) { i: Int -> activeRepository().requestBaseMap(bOffset, i) },
            Triple("Injector Map", com.kkr.juken5livemonitor.protocol.ECUProtocol.INJECTOR_HEADER) { i: Int -> activeRepository().requestInjectorMap(itOffset, i) },
            Triple("Ignition Map", com.kkr.juken5livemonitor.protocol.ECUProtocol.IGNITION_HEADER) { i: Int -> activeRepository().requestIgnitionMap(igOffset, i) },
            Triple("History", com.kkr.juken5livemonitor.protocol.ECUProtocol.HISTORY_HEADER) { i: Int -> activeRepository().requestHistory(i) }
        )

        for ((name, header, requestFunc) in maps) {
            for (i in 0 until 21) {
                _syncStatus.value = "Downloading $name ($i/21)..."
                
                // Clear any pending messages before requesting next chunk
                while (responseChannel.tryReceive().isSuccess) { /* consume */ }
                
                requestFunc(i)
                
                val chunkResult = withTimeoutOrNull(8000) { // Increased timeout
                    var res: Pair<String, List<String>>? = null
                    while(res?.first != header) {
                        res = responseChannel.receive()
                    }
                    res
                }
                
                if (chunkResult == null) {
                    Log.e("SYNC", "Timeout waiting for $name chunk $i")
                    return false
                }
                
                delay(50) // Increased delay for serial buffer safety
            }
            delay(300) // Brief pause between map types
        }

        return true
    }

    private suspend fun <T> withTimeoutOrNull(timeMillis: Long, block: suspend () -> T): T? {
        return try {
            kotlinx.coroutines.withTimeout(timeMillis) { block() }
        } catch (e: Exception) {
            null
        }
    }

    fun startGPS(context: Context) {
        if (gpsProvider == null) gpsProvider = GPSSpeedProvider(context)
        gpsProvider?.start { location ->
            Log.d("GPS", "Location update: ${location.latitude}, ${location.longitude}")
            val currentSpeedKph = location.speed * 3.6f
            if (currentSpeedKph > _topSpeed.value) _topSpeed.value = currentSpeedKph
            speedSum += currentSpeedKph
            speedCount++
            _avgSpeed.value = speedSum / speedCount
            processZeroToHundred(currentSpeedKph)
            _currentLocation.value = LatLng(location.latitude, location.longitude)
            if (location.hasBearing()) _bearing.value = location.bearing
            _liveData.value = _liveData.value.copy(speed = currentSpeedKph)

            // Update city name
            updateCityName(location.latitude, location.longitude)
        }
    }

    private var lastGeocodeTime = 0L
    private var lastLat = 0.0
    private var lastLon = 0.0

    private fun updateCityName(lat: Double, lon: Double) {
        // Skip if coordinates are invalid (0,0)
        if (lat == 0.0 && lon == 0.0) return

        val now = System.currentTimeMillis()
        
        // If location hasn't changed much and we already have a name, skip
        if (Math.abs(lat - lastLat) < 0.001 && Math.abs(lon - lastLon) < 0.001 && _cityName.value != "Searching..." && _cityName.value != "GPS Locked") {
             if (now - lastGeocodeTime < 60000) return // Throttle to 60 seconds for same area
        }

        lastGeocodeTime = now
        lastLat = lat
        lastLon = lon

        Log.d("GEO", "Attempting geocode for: $lat, $lon")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!android.location.Geocoder.isPresent()) {
                    Log.e("GEO", "Geocoder is not present on this device")
                    _cityName.value = "GPS Locked"
                    return@launch
                }

                val geocoder = android.location.Geocoder(getApplication(), java.util.Locale.getDefault())
                
                // Set fallback initially once we have coordinates
                if (_cityName.value == "Searching...") {
                    _cityName.value = "GPS Locked"
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "GPS Locked"
                            Log.d("GEO", "Found city (T): $city")
                            _cityName.value = city
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "GPS Locked"
                        Log.d("GEO", "Found city: $city")
                        _cityName.value = city
                    }
                }
            } catch (e: Exception) {
                Log.e("GEO", "Geocoding failed", e)
                if (_cityName.value == "Searching...") {
                    _cityName.value = "GPS Locked"
                }
            }
        }
    }

    fun stopGPS() {
        gpsProvider?.stop()
    }

    fun dismissDownloadFailed() {
        _showDownloadFailed.value = false
    }

    fun stopAndroidAuto() {
        abandonAaAudioFocus()
        _aaEngine.stop()
        _aaEngine.stopSelfMode()
    }

    fun disconnect() {
        monitoring = false
        demoJob?.cancel()
        demoJob = null
        pingJob?.cancel()
        pingJob = null
        _status.value = "Disconnected"
        stopGPS()
        
        viewModelScope.launch(Dispatchers.IO) {
            activeRepository().stopLiveGracefully()
            activeRepository().disconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAndroidAuto()
        disconnect()
    }
}
