package com.kkr.juken5livemonitor.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.kkr.juken5livemonitor.location.GPSSpeedProvider
import com.kkr.juken5livemonitor.model.ECULiveData
import com.kkr.juken5livemonitor.repository.ECURepository
import com.kkr.juken5livemonitor.screen.monitor.center.CenterMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ECURepository()
    private val prefs = application.getSharedPreferences("juken_settings", Context.MODE_PRIVATE)

    // =========================
    // ECU Live Data
    // =========================

    private val _liveData = MutableStateFlow(ECULiveData())
    val liveData = _liveData.asStateFlow()

    // =========================
    // GPS Location
    // =========================

    private val _currentLocation = MutableStateFlow(
        LatLng(-6.200000, 106.816666)
    )

    val currentLocation: StateFlow<LatLng> = _currentLocation.asStateFlow()

    private val _bearing = MutableStateFlow(0f)
    val bearing: StateFlow<Float> = _bearing.asStateFlow()

    // =========================
    // Connection Status
    // =========================

    private val _status = MutableStateFlow("Disconnected")
    val status = _status.asStateFlow()

    // =========================
    // Settings
    // =========================

    private val _bikeName = MutableStateFlow(prefs.getString("bike_name", "YAMAHA R25") ?: "YAMAHA R25")
    val bikeName = _bikeName.asStateFlow()

    private val _selectedUi = MutableStateFlow(prefs.getInt("selected_ui", 2)) // 0: Default, 1: Modern, 2: Racing
    val selectedUi = _selectedUi.asStateFlow()

    private val _centerMode = MutableStateFlow<CenterMode>(CenterMode.AndroidAuto)
    val centerMode = _centerMode.asStateFlow()

    // =========================
    // Vehicle & Performance Settings
    // =========================

    private val _vehicleWeight = MutableStateFlow(prefs.getFloat("vehicle_weight", 110f)) // kg
    val vehicleWeight = _vehicleWeight.asStateFlow()

    private val _riderWeight = MutableStateFlow(prefs.getFloat("rider_weight", 70f)) // kg
    val riderWeight = _riderWeight.asStateFlow()

    enum class DrivetrainType(val efficiency: Float) {
        CVT_SCOOTER(1.18f),
        MANUAL_MOTO(1.12f),
        RWD_CAR(1.15f),
        FWD_CAR(1.13f)
    }

    private val _drivetrainType = MutableStateFlow(
        try {
            DrivetrainType.valueOf(prefs.getString("drivetrain_type", DrivetrainType.RWD_CAR.name) ?: DrivetrainType.RWD_CAR.name)
        } catch (e: Exception) {
            DrivetrainType.RWD_CAR
        }
    )
    val drivetrainType = _drivetrainType.asStateFlow()

    data class PerformanceResult(
        val time: String = "0.00s",
        val avgAccel: Double = 0.0,
        val wheelPower: Double = 0.0,
        val enginePower: Double = 0.0
    )

    private val _performanceResult = MutableStateFlow<PerformanceResult?>(null)
    val performanceResult = _performanceResult.asStateFlow()

    // =========================
    // Racing Stats & Analytics
    // =========================

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

    private var speedSum = 0f
    private var speedCount = 0

    enum class TimerStatus { IDLE, COUNTDOWN, READY, RUNNING, FINISHED, COOLDOWN }
    private val _zeroToHundredStatus = MutableStateFlow(TimerStatus.IDLE)
    val zeroToHundredStatus = _zeroToHundredStatus.asStateFlow()

    private val _countdownValue = MutableStateFlow("")
    val countdownValue = _countdownValue.asStateFlow()

    private val _cooldownRemaining = MutableStateFlow(0)
    val cooldownRemaining = _cooldownRemaining.asStateFlow()

    private val _zeroToHundredTime = MutableStateFlow(0L) // Milliseconds
    val zeroToHundredResult: StateFlow<String> = _zeroToHundredTime.map { 
        "%.2fs".format(it / 1000f) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0.00s")

    private val _estHorsePower = MutableStateFlow(0.0)
    val estHorsePower = _estHorsePower.asStateFlow()

    private var engineStartTime = 0L
    private var timerStartTime = 0L
    private var lastTimerTime = 0L
    private var lastTimerSpeed = 0f
    private var timerJob: kotlinx.coroutines.Job? = null

    private val fuelHistory = mutableListOf<Pair<String, Long>>()

    private fun detectFuelTrigger(fuel: String) {
        val now = System.currentTimeMillis()

        // Debounce: Only track if state actually changed
        if (fuelHistory.isNotEmpty() && fuelHistory.last().first == fuel) return

        fuelHistory.add(fuel to now)

        // Keep only last 4 entries (0-1-0-1 pattern needs 4 points)
        if (fuelHistory.size > 4) {
            fuelHistory.removeAt(0)
        }

        // Check pattern 0-1-0-1 and timing (<= 1 second)
        if (fuelHistory.size == 4) {
            val states = fuelHistory.map { it.first }
            val startTime = fuelHistory.first().second
            val endTime = fuelHistory.last().second

            if (states == listOf("0", "1", "0", "1") && (endTime - startTime) <= 1000) {
                Log.d("FUEL_TRIGGER", "Auto-Arming 0-100 Timer via Fuel Switch Pattern detected")
                armZeroToHundred()
                fuelHistory.clear() // Reset history to avoid accidental repeat
            }
        }
    }

    fun armZeroToHundred() {
        // If already in cooldown, do nothing
        if (_zeroToHundredStatus.value == TimerStatus.COOLDOWN) return

        // If currently finished, clicking again resets to IDLE
        if (_zeroToHundredStatus.value == TimerStatus.FINISHED) {
            _zeroToHundredStatus.value = TimerStatus.IDLE
            _performanceResult.value = null
            return
        }

        // If currently active (Countdown, Ready, or Running), trigger CANCEL
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
            
            // Show CANCEL for 1 second then clear
            delay(1000)
            _countdownValue.value = ""
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

    fun setCenterMode(mode: CenterMode) {

        if (_centerMode.value == mode) return

        Log.d("CENTER_MODE", "Switching to $mode")

        _centerMode.value = mode

    }

    fun updateBikeName(name: String) {
        _bikeName.value = name
        prefs.edit().putString("bike_name", name).apply()
    }

    fun updateSelectedUi(index: Int) {
        _selectedUi.value = index
        prefs.edit().putInt("selected_ui", index).apply()
    }

    private var monitoring = false
    private var demoJob: kotlinx.coroutines.Job? = null

    private var gpsProvider: GPSSpeedProvider? = null

    // =========================
    // Demo Mode
    // =========================

    fun startDemoMode() {
        if (monitoring) return
        monitoring = true
        _status.value = "Demo Dashboard"

        demoJob = viewModelScope.launch(Dispatchers.Default) {
            var currentRpm = 1000f
            var currentSpeed = 0f
            var currentBattery = 12.2f
            var currentEot = 75.0f
            var fuelCounter = 0

            while (monitoring) {
                // 1. Simulate RPM (oscillating between 1000 and 11000)
                val rpmDelta = (Math.random().toFloat() * 400f) - 150f
                currentRpm = (currentRpm + rpmDelta).coerceIn(1000f, 11500f)

                // 2. Simulate Speed (accelerating/decelerating)
                val speedDelta = (Math.random().toFloat() * 4f) - 1.5f
                currentSpeed = (currentSpeed + speedDelta).coerceIn(0f, 180f)

                // 3. Simulate Battery & Temp
                currentBattery = (13.8f + (Math.random().toFloat() * 0.4f - 0.2f))
                currentEot = (85f + (Math.random().toFloat() * 10f - 5f))

                // 4. Update Stats for 0-100 logic (Simulated GPS flow)
                if (currentSpeed > _topSpeed.value) _topSpeed.value = currentSpeed
                
                // 5. Build Live Data
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

                // Trigger 0-100 check (normally happens in GPS callback)
                handleZeroToHundredSim(currentSpeed)

                fuelCounter++
                delay(100) // 10 FPS updates
            }
        }
    }

    private fun handleZeroToHundredSim(currentSpeedKph: Float) {
        processZeroToHundred(currentSpeedKph)
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

                // Real-time HP Calculation during the run
                val currentSpeedMs = currentSpeedKph / 3.6f
                val deltaTime = (now - lastTimerTime) / 1000f

                if (deltaTime > 0.1f) { // Update HP calculation every ~100ms or GPS tick
                    val deltaV = currentSpeedMs - lastTimerSpeed
                    val accel = deltaV / deltaTime

                    if (accel > 0) {
                        val totalMass = _vehicleWeight.value + _riderWeight.value
                        // Physics: Power = Force * Velocity = (Mass * Acceleration) * Velocity
                        val powerWatts = (totalMass * accel) * currentSpeedMs
                        val hpWheel = powerWatts / 745.7
                        val hpEngine = hpWheel * _drivetrainType.value.efficiency

                        // Update peak HP if current instantaneous HP is higher
                        if (hpEngine > _estHorsePower.value) {
                            _estHorsePower.value = hpEngine
                        }
                    }
                    lastTimerTime = now
                    lastTimerSpeed = currentSpeedMs
                }

                if (currentSpeedKph >= 100f) {
                    _zeroToHundredStatus.value = TimerStatus.FINISHED
                    
                    val timeSeconds = elapsed / 1000f
                    if (timeSeconds > 0) {
                        // Final calculation to ensure we have a result even if GPS tick was rare
                        val totalMass = _vehicleWeight.value + _riderWeight.value
                        val vFinal = 100.0 / 3.6
                        val avgAccel = vFinal / timeSeconds
                        val force = totalMass * avgAccel
                        val powerWatts = force * vFinal
                        val hpWheel = powerWatts / 745.7
                        val hpEngine = hpWheel * _drivetrainType.value.efficiency
                        
                        // Ensure peak HP is at least the average result
                        if (hpEngine > _estHorsePower.value) {
                            _estHorsePower.value = hpEngine
                        }

                        _performanceResult.value = PerformanceResult(
                            time = "%.2fs".format(timeSeconds),
                            avgAccel = avgAccel,
                            wheelPower = hpWheel,
                            enginePower = hpEngine
                        )
                    }
                } else if (currentSpeedKph < 0.2f && elapsed > 2000) {
                    // Reset if stopped for more than 2s while running (failed run)
                    _zeroToHundredStatus.value = TimerStatus.IDLE
                }
            }
            else -> {}
        }
    }

    // =========================
    // Manual Update Location
    // =========================

    fun updateLocation(
        latitude: Double,
        longitude: Double,
        bearing: Float = 0f
    ) {

        _currentLocation.value =
            LatLng(latitude, longitude)

        _bearing.value = bearing

    }

    // =========================
    // ECU Monitor
    // =========================

    fun startMonitor() {

        if (monitoring) return

        monitoring = true

        viewModelScope.launch(Dispatchers.IO) {

            Log.d("FLOW", "===== START MONITOR =====")

            if (!repository.isConnected()) {

                _status.value = "ECU Not Connected"

                monitoring = false

                return@launch

            }

            _status.value = "Connected"

            Log.d("FLOW", "Handshake")

            repository.handshake()

            delay(300)

            repository.setLiveListener { data ->

                if (data.rpm > 0 && engineStartTime == 0L) {
                    engineStartTime = System.currentTimeMillis()
                    viewModelScope.launch {
                        while (monitoring) {
                            _engineRuntimeMillis.value = System.currentTimeMillis() - engineStartTime
                            delay(1000)
                        }
                    }
                }

                // Detect 0-1-0-1 fuel trigger for 0-100 timer
                detectFuelTrigger(data.fuel)

                _liveData.value = data.copy(
                    speed = _liveData.value.speed
                )

            }

            repository.startListening()

            delay(200)

            repository.openLive()

        }

    }

    // =========================
    // GPS
    // =========================

    fun startGPS(
        context: Context
    ) {

        if (gpsProvider == null) {
            gpsProvider = GPSSpeedProvider(context)
        }

        gpsProvider?.start { location ->

            val currentSpeedKph = location.speed * 3.6f

            // 1. Stats Update
            if (currentSpeedKph > _topSpeed.value) {
                _topSpeed.value = currentSpeedKph
            }
            
            speedSum += currentSpeedKph
            speedCount++
            _avgSpeed.value = speedSum / speedCount

            // 2. 0-100 Logic
            processZeroToHundred(currentSpeedKph)

            _currentLocation.value = LatLng(
                location.latitude,
                location.longitude
            )

            if (location.hasBearing()) {
                _bearing.value = location.bearing
            }

            _liveData.value = _liveData.value.copy(
                speed = currentSpeedKph
            )

        }

    }

    fun stopGPS() {

        gpsProvider?.stop()

    }

    // =========================
    // Disconnect
    // =========================

    fun disconnect() {

        monitoring = false
        demoJob?.cancel()
        demoJob = null

        _status.value = "Disconnected"

        stopGPS()

        repository.disconnect()

    }

    override fun onCleared() {

        super.onCleared()

        disconnect()

    }

}