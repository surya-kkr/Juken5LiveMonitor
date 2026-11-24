package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import com.kkr.juken5livemonitor.aap.protocol.proto.*
import android.view.Surface
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLEngineResult

class ProjectionManager(private val context: Context) {

    private val _state = MutableStateFlow<ProjectionState>(ProjectionState.Disconnected)
    val state: StateFlow<ProjectionState> = _state.asStateFlow()
    
    var onPhoneNameReceived: ((String) -> Unit)? = null
    private var displaySurface: Surface? = null
    private var clientSocket: Socket? = null
    private var projectionScope: CoroutineScope? = null
    
    private var sendThread: HandlerThread? = null
    private var sendHandler: Handler? = null
    private var readerThread: Thread? = null
    private var heartbeatJob: Job? = null
    private var connectionGeneration = 0L
    
    private val serviceToChannelMap = mutableMapOf<Int, Int>()
    private val sessionIds = mutableMapOf<Int, Int>()
    private val sslManager = AndroidAutoSSLManager(context)
    private var isSslEstablished = false
    private var mediaCodec: MediaCodec? = null
    private var outputThread: Thread? = null
    private var lastFrameTime = 0L
    private var currentNightMode = true

    // Android Auto 17.4+ no longer accepts the old private Self-Mode launch intents.
    // The supported fallback is the developer Headunit Server, which listens locally.
    // In this local loopback mode Spotify/Maps run on the same phone, so media audio
    // should stay with Android's normal phone output route instead of being captured
    // as a headunit PCM sink.
    private val isSelfMode = true
    private val selfModeHost = "127.0.0.1"
    private val selfModePort = 5277
    private val selfModeConnectTimeoutMs = 1_500
    private val selfModeRetryDelayMs = 2_000L
    
    private var advertisedWidth = 1280
    private var advertisedHeight = 720
    private var advertisedDpi = 140
    private var marginWidth = 0
    private var marginHeight = 0
    private var physicalViewWidth = 1280
    private var physicalViewHeight = 720

    private val fifo = ByteBuffer.allocateDirect(4 * 1024 * 1024)
    private val readBuffer = ByteArray(131080)
    private val decryptBuffer = ByteBuffer.allocateDirect(131080)
    private val packetHeader = ByteArray(4)
    private val skipBuffer = ByteArray(4)
    private val channelBuffers = mutableMapOf<Int, ByteBuffer>()
    private val audioTracks = ConcurrentHashMap<Int, AudioTrack>()
    private val audioFramesReceived = ConcurrentHashMap<Int, Int>()
    private val audioUnderruns = ConcurrentHashMap<Int, Int>()
    private val audioWriters = ConcurrentHashMap<Int, ThreadPoolExecutor>()

    fun setSurface(surface: Surface?) {
        synchronized(this) {
            if (displaySurface === surface) return
            Log.d("AAP_MANAGER", "New Surface set: $surface")
            stopDecoder()
            displaySurface = surface
        }
    }

    fun setDimensions(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (this.physicalViewWidth == width && this.physicalViewHeight == height) return
        this.physicalViewWidth = width
        this.physicalViewHeight = height
        val physicalRatio = width.toFloat() / height
        val containerRatio = 1280f / 720f
        if (physicalRatio > containerRatio) {
            marginWidth = 0
            val activeHeight = (1280 / physicalRatio).toInt()
            marginHeight = (720 - activeHeight).coerceAtLeast(0)
        } else {
            marginHeight = 0
            val activeWidth = (720 * physicalRatio).toInt()
            marginWidth = (1280 - activeWidth).coerceAtLeast(0)
        }
        this.advertisedWidth = 1280
        this.advertisedHeight = 720
    }

    fun getPhysicalWidth() = physicalViewWidth
    fun getPhysicalHeight() = physicalViewHeight
    fun setDpi(dpi: Int) { this.advertisedDpi = dpi }
    fun initialize() { _state.value = ProjectionState.Initializing }
    fun waitingProjection() { _state.value = ProjectionState.WaitingProjection }
    fun startSelfMode() {}
    fun stopSelfMode() {}

    @Synchronized
    fun running() {
        if (readerThread != null) return
        startConnection(++connectionGeneration)
    }
    
    private fun startConnection(sessionGeneration: Long) {
        sendThread = HandlerThread("AA-Sender", Process.THREAD_PRIORITY_AUDIO)
        sendThread?.start()
        sendHandler = Handler(sendThread!!.looper)
        projectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val thread = Thread {
            // Do not fail permanently when Android Auto's developer server is started after
            // the dashboard. Keeping this thread alive also makes Classic and Modern behave
            // identically because they share this one engine instance.
            while (isCurrentSession(sessionGeneration) && !Thread.currentThread().isInterrupted) {
                var socket: Socket? = null
                try {
                    _state.value = ProjectionState.WaitingProjection
                    Log.d("AAP_MANAGER", "Waiting for Android Auto Headunit Server on $selfModeHost:$selfModePort")
                    socket = Socket().apply {
                        connect(InetSocketAddress(selfModeHost, selfModePort), selfModeConnectTimeoutMs)
                        tcpNoDelay = true
                        receiveBufferSize = 2 * 1024 * 1024
                        sendBufferSize = 512 * 1024
                        soTimeout = 10_000
                    }
                    if (!isCurrentSession(sessionGeneration)) break

                    clientSocket = socket
                    if (!performHandshakeSync(socket.getInputStream())) {
                        Log.w("AAP_MANAGER", "Headunit Server handshake did not complete; retrying.")
                        resetForSelfModeRetry(socket)
                        waitForSelfModeRetry(sessionGeneration)
                        continue
                    }

                    Log.i("AAP_MANAGER", "Headunit Server handshake complete.")
                    heartbeatJob = projectionScope?.launch {
                        while (isCurrentSession(sessionGeneration) && socket.isConnected) {
                            sendDrivingStatusUnrestricted()
                            delay(5_000)
                        }
                    }
                    runReadLoop(socket.getInputStream(), socket, sessionGeneration)
                } catch (e: Exception) {
                    if (isCurrentSession(sessionGeneration)) {
                        Log.i("AAP_MANAGER", "Headunit Server is not ready yet: ${e.message}")
                    }
                } finally {
                    resetForSelfModeRetry(socket)
                }

                waitForSelfModeRetry(sessionGeneration)
            }
            disconnect(sessionGeneration)
        }.apply { name = "AA-SelfMode"; priority = Thread.NORM_PRIORITY + 2 }
        readerThread = thread
        thread.start()
    }

    private fun waitForSelfModeRetry(sessionGeneration: Long) {
        if (!isCurrentSession(sessionGeneration)) return
        try {
            Thread.sleep(selfModeRetryDelayMs)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun resetForSelfModeRetry(socket: Socket?) {
        heartbeatJob?.cancel()
        heartbeatJob = null
        try { socket?.close() } catch (_: Exception) {}
        if (clientSocket === socket) clientSocket = null
        isSslEstablished = false
        serviceToChannelMap.clear()
        sessionIds.clear()
        channelBuffers.clear()
        stopDecoder()
        stopAudio()
    }

    private fun performHandshakeSync(inputStream: InputStream): Boolean {
        var versionOk = false
        repeat(3) {
            if (versionOk) return@repeat
            sendControlPacketDirect(0x0001, byteArrayOf(0x00, 0x01, 0x00, 0x02))
            if (readFullSync(inputStream, packetHeader, 4)) {
                val len = ((packetHeader[2].toInt() and 0xFF) shl 8) or (packetHeader[3].toInt() and 0xFF)
                val payload = ByteArray(len)
                if (readFullSync(inputStream, payload, len)) {
                    val msgId = ((payload[0].toInt() and 0xFF) shl 8) or (payload[1].toInt() and 0xFF)
                    if (msgId == 0x0002) versionOk = true
                }
            }
        }
        if (!versionOk) return false
        sslManager.initialize() 
        sslManager.doHandshakeAction { response -> sendControlPacketDirect(0x0003, response) }
        while (sslManager.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.FINISHED &&
               sslManager.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            try {
                if (readFullSync(inputStream, packetHeader, 4)) {
                    val len = ((packetHeader[2].toInt() and 0xFF) shl 8) or (packetHeader[3].toInt() and 0xFF)
                    val sslPayload = ByteArray(len)
                    if (readFullSync(inputStream, sslPayload, len) && sslPayload.size >= 2) {
                        val msgId = ((sslPayload[0].toInt() and 0xFF) shl 8) or (sslPayload[1].toInt() and 0xFF)
                        if (msgId == 0x0003) {
                            sslManager.unwrapHandshake(ByteBuffer.wrap(sslPayload.copyOfRange(2, len))) { reply ->
                                sendControlPacketDirect(0x0003, reply)
                            }
                        }
                    }
                }
            } catch (e: Exception) { return false }
        }
        sendControlPacketDirect(0x0004, byteArrayOf(0x08, 0x00))
        isSslEstablished = true
        return true
    }

    private fun runReadLoop(inputStream: InputStream, socket: Socket, sessionGeneration: Long) {
        fifo.clear()
        while (!Thread.currentThread().isInterrupted && isCurrentSession(sessionGeneration) && socket.isConnected) {
            try {
                val readSize = inputStream.read(readBuffer)
                if (readSize < 0) break
                if (readSize == 0) continue
                if (fifo.remaining() < readSize) fifo.clear()
                fifo.put(readBuffer, 0, readSize); processFifoBulk()
            } catch (e: SocketTimeoutException) {
                sendPing()
            } catch (e: Exception) { break }
        }
    }

    private fun processFifoBulk() {
        fifo.flip()
        while (fifo.remaining() >= 4) {
            fifo.mark()
            fifo.get(packetHeader, 0, 4)
            val channel = packetHeader[0].toInt() and 0xFF
            val flags = packetHeader[1].toInt() and 0xFF
            val length = ((packetHeader[2].toInt() and 0xFF) shl 8) or (packetHeader[3].toInt() and 0xFF)
            if (flags == 0x09) {
                if (fifo.remaining() < 4) { fifo.reset(); break }
                fifo.get(skipBuffer, 0, 4)
            }
            if (fifo.remaining() < length) { fifo.reset(); break }
            val payload = ByteArray(length); fifo.get(payload); processAapPacket(channel, flags, payload)
        }
        fifo.compact()
    }

    private fun processAapPacket(channel: Int, flags: Int, data: ByteArray) {
        val channelBuffer = channelBuffers.getOrPut(channel) { 
            ByteBuffer.allocateDirect(if (channel == serviceToChannelMap[2]) 4 * 1024 * 1024 else 256 * 1024)
        }
        if ((flags and 0x01) != 0) channelBuffer.clear()
        val decryptedData: ByteBuffer? = if (flags and 0x08 != 0) {
            decryptBuffer.clear()
            val produced = sslManager.decryptDirect(ByteBuffer.wrap(data), decryptBuffer)
            if (produced > 0) { decryptBuffer.flip(); decryptBuffer } else null
        } else { ByteBuffer.wrap(data) }
        if (decryptedData != null) {
            if (channelBuffer.remaining() < decryptedData.remaining()) channelBuffer.clear()
            channelBuffer.put(decryptedData)
        }
        if ((flags and 0x02) != 0) {
            channelBuffer.flip()
            val fullSize = channelBuffer.limit()
            if (fullSize >= 2) {
                val messageId = ((channelBuffer.get(0).toInt() and 0xFF) shl 8) or (channelBuffer.get(1).toInt() and 0xFF)
                val videoChannel = serviceToChannelMap[2] ?: -1
                if ((messageId == 0x0001 || messageId == 0x0000) && (channel == videoChannel || listOf(4, 5, 6).mapNotNull { serviceToChannelMap[it] }.contains(channel))) {
                    if (channel == videoChannel) {
                        val offset = if (messageId == 0x0000) 10 else 2
                        val timestamp = System.nanoTime() / 1000L
                        if (fullSize > offset) {
                            channelBuffer.position(offset)
                            synchronized(this) {
                                if (mediaCodec == null && displaySurface != null) startDecoder()
                                feedToDecoder(channelBuffer, fullSize - offset, timestamp)
                            }
                            sendMediaAck(channel)
                        }
                    } else if (listOf(4, 5, 6).mapNotNull { serviceToChannelMap[it] }.contains(channel)) {
                        // AAP audio DATA and CODEC_CONFIG messages both reserve the first
                        // 10 bytes for the media header (type + timestamp). The current
                        // implementation used offset 2 for type 1, feeding the header into
                        // PCM and making the Android mixer discard the stream.
                        val audioOffset = 10
                        if (fullSize > audioOffset) {
                            val payloadLen = fullSize - audioOffset
                            val audioPayload = ByteArray(payloadLen)
                            channelBuffer.position(audioOffset)
                            channelBuffer.get(audioPayload)
                            // The AAP media header has already been removed above. Passing the
                            // payload from byte zero is essential: dropping it a second time
                            // corrupts every PCM frame and results in silent Self Mode audio.
                            feedToAudio(channel, audioPayload, payloadLen)
                            sendMediaAck(channel)
                        }
                    }
                } else {
                    val controlPayload = ByteArray(fullSize - 2)
                    channelBuffer.position(2); channelBuffer.get(controlPayload)
                    handleControlMessageSync(messageId, controlPayload, channel)
                }
            }
            channelBuffer.clear()
        }
    }

    private fun readFullSync(inputStream: InputStream, buffer: ByteArray, length: Int): Boolean {
        var totalRead = 0
        while (totalRead < length) {
            try {
                val r = inputStream.read(buffer, totalRead, length - totalRead)
                if (r == -1) return false
                totalRead += r
            } catch (e: Exception) { return false }
        }
        return true
    }

    private fun handleControlMessageSync(messageId: Int, payload: ByteArray, channel: Int) {
        val sensorChannel = serviceToChannelMap[1] ?: -1
        val inputChannel = serviceToChannelMap[3] ?: -1
        when (messageId) {
            Control.ControlMsgType.MESSAGE_SERVICE_DISCOVERY_REQUEST_VALUE -> {
                if (channel == 0) {
                    val phoneName = extractPhoneName(payload)
                    if (phoneName != null) onPhoneNameReceived?.invoke(phoneName)
                    sendServiceDiscoveryResponse()
                }
            }
            Control.ControlMsgType.MESSAGE_CHANNEL_OPEN_REQUEST_VALUE -> {
                try {
                    val openReq = Control.ChannelOpenRequest.parseFrom(payload)
                    val serviceId = openReq.serviceId
                    serviceToChannelMap[serviceId] = channel
                    sendChannelOpenResponse(channel)
                    if (serviceId == 1) {
                        sendDrivingStatusUnrestricted(channel); sendNightMode(currentNightMode)
                    } else if (serviceId == 2) {
                        _state.value = ProjectionState.Running
                    } else if (listOf(4, 5, 6).contains(serviceId)) {
                        sendAudioFocusNotification(Control.AudioFocusNotification.AudioFocusStateType.STATE_GAIN, 0)
                    }
                } catch (e: Exception) {}
            }
            Control.ControlMsgType.MESSAGE_PING_REQUEST_VALUE -> sendPing()
            Control.ControlMsgType.MESSAGE_AUDIO_FOCUS_REQUEST_VALUE -> {
                try {
                    val focusReq = Control.AudioFocusRequestNotification.parseFrom(payload)
                    val response = when (focusReq.request) {
                        Control.AudioFocusRequestNotification.AudioFocusRequestType.RELEASE -> Control.AudioFocusNotification.AudioFocusStateType.STATE_LOSS
                        Control.AudioFocusRequestNotification.AudioFocusRequestType.GAIN -> Control.AudioFocusNotification.AudioFocusStateType.STATE_GAIN
                        Control.AudioFocusRequestNotification.AudioFocusRequestType.GAIN_TRANSIENT -> Control.AudioFocusNotification.AudioFocusStateType.STATE_GAIN_TRANSIENT
                        Control.AudioFocusRequestNotification.AudioFocusRequestType.GAIN_TRANSIENT_MAY_DUCK -> Control.AudioFocusNotification.AudioFocusStateType.STATE_GAIN_TRANSIENT_GUIDANCE_ONLY
                        else -> Control.AudioFocusNotification.AudioFocusStateType.STATE_GAIN
                    }
                    sendAudioFocusNotification(response, channel)
                } catch (e: Exception) { sendAudioFocusNotification(Control.AudioFocusNotification.AudioFocusStateType.STATE_GAIN, channel) }
            }
            Control.ControlMsgType.MESSAGE_NAV_FOCUS_REQUEST_VALUE -> {
                val response = Control.NavFocusNotification.newBuilder().setFocusType(Control.NavFocusType.NAV_FOCUS_2).build()
                sendControlPacketDirect(Control.ControlMsgType.MESSAGE_NAV_FOCUS_NOTIFICATION_VALUE, response.toByteArray(), channel)
            }
            Control.ControlMsgType.MESSAGE_BYEBYE_REQUEST_VALUE -> {
                val response = Control.ByeByeResponse.newBuilder().build()
                sendControlPacketDirect(Control.ControlMsgType.MESSAGE_BYEBYE_RESPONSE_VALUE, response.toByteArray(), channel)
                disconnect()
            }
            0x0012 -> {
                val reqType = if (payload.size >= 2) payload[1].toInt() and 0xFF else 1
                sendControlPacketDirect(0x0013, byteArrayOf(0x08, (if (reqType == 4) 3 else 1).toByte()))
            }
        }
        if (channel == sensorChannel && messageId == 0x8001) {
            val response = Sensors.SensorResponse.newBuilder().setStatus(Common.MessageStatus.STATUS_SUCCESS).build()
            sendControlPacketDirect(0x8002, response.toByteArray(), channel)
        } else if (channel == inputChannel && messageId == 0x8002) {
            val response = Input.BindingResponse.newBuilder().setStatus(Common.MessageStatus.STATUS_SUCCESS).build()
            sendControlPacketDirect(0x8003, response.toByteArray(), channel)
        } else if (channel != 0 && channel != sensorChannel && channel != inputChannel) {
            when (messageId) {
                0x8000 -> {
                    val maxUnacked = if (channel == serviceToChannelMap[2]) 12 else if (isSelfMode) 30 else 16
                    val response = Media.Config.newBuilder().setStatus(Media.Config.ConfigStatus.HEADUNIT).setMaxUnacked(maxUnacked).addConfigurationIndices(0).build()
                    sendControlPacketDirect(0x8003, response.toByteArray(), channel)
                    if (channel == serviceToChannelMap[2]) {
                        val focus = Media.VideoFocusNotification.newBuilder().setMode(Media.VideoFocusMode.VIDEO_FOCUS_PROJECTED).setUnsolicited(true).build()
                        sendControlPacketDirect(0x8008, focus.toByteArray(), channel)
                    }
                }
                0x8001 -> { try { val msg = Media.Start.parseFrom(payload); sessionIds[channel] = msg.sessionId } catch (e: Exception) {} }
                0x8002 -> {
                    val audioService = serviceToChannelMap.entries
                        .firstOrNull { it.value == channel }
                        ?.key
                    // Maps temporarily stops service 6 while it owns guidance audio. Keeping
                    // Spotify's AudioTrack alive avoids a cold track/buffer when service 6 starts
                    // again, which otherwise produces a noticeable stutter after every prompt.
                    // Service 4 and 5 are short-lived voice/system tracks and can be released.
                    if (audioService == 6) {
                        Log.d("AAP_AUDIO", "Media stopped for transient guidance; keeping channel=$channel warm")
                    } else if (audioService == 4 || audioService == 5) {
                        stopAudioChannel(channel)
                    }
                }
            }
        }
    }

    private fun sendAudioFocusNotification(state: Control.AudioFocusNotification.AudioFocusStateType, channel: Int) {
        val notification = Control.AudioFocusNotification.newBuilder().setFocusState(state).build()
        sendControlPacketDirect(Control.ControlMsgType.MESSAGE_AUDIO_FOCUS_NOTIFICATION_VALUE, notification.toByteArray(), channel)
    }

    private fun sendServiceDiscoveryResponse() {
        val response = Control.ServiceDiscoveryResponse.newBuilder().apply {
            headUnitMake = "Google"; headUnitModel = "Android Auto"; headUnitSoftwareBuild = "1"; headUnitSoftwareVersion = "0.1.0"
            make = "Google"; model = "Android Auto"; year = "2025"; vehicleId = "headlessunit-001"
            driverPosition = Control.DriverPosition.DRIVER_POSITION_LEFT
            canPlayNativeMediaDuringVr = false; hideProjectedClock = false; displayName = "Android Auto"
            addServices(Control.Service.newBuilder().setId(1).setSensorSourceService(Control.Service.SensorSourceService.newBuilder().addSensors(Control.Service.SensorSourceService.Sensor.newBuilder().setType(Sensors.SensorType.DRIVING_STATUS)).addSensors(Control.Service.SensorSourceService.Sensor.newBuilder().setType(Sensors.SensorType.NIGHT)).addSensors(Control.Service.SensorSourceService.Sensor.newBuilder().setType(Sensors.SensorType.LOCATION))).build())
            addServices(Control.Service.newBuilder().setId(2).setMediaSinkService(Control.Service.MediaSinkService.newBuilder().setAvailableType(Media.MediaCodecType.MEDIA_CODEC_VIDEO_H264_BP).setAvailableWhileInCall(true).addVideoConfigs(Control.Service.MediaSinkService.VideoConfiguration.newBuilder().setCodecResolution(Control.Service.MediaSinkService.VideoConfiguration.VideoCodecResolutionType._1280x720).setFrameRate(Control.Service.MediaSinkService.VideoConfiguration.VideoFrameRateType._60).setMarginWidth(marginWidth).setMarginHeight(marginHeight).setDensity(advertisedDpi).setPixelAspectRatioE4(10000).setVideoCodecType(Media.MediaCodecType.MEDIA_CODEC_VIDEO_H264_BP).build())).build())
            addServices(Control.Service.newBuilder().setId(3).setInputSourceService(Control.Service.InputSourceService.newBuilder().setTouchscreen(Control.Service.InputSourceService.TouchConfig.newBuilder().setWidth(1280 - marginWidth).setHeight(720 - marginHeight)).addAllKeycodesSupported(listOf(1, 2, 4, 19, 20, 21, 22, 23, 126, 127, 85, 87, 88, 84, 5, 209, 24, 25, 61, 62, 66, 3, 79, 86, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 81, 224, 264, 265, 267, 268, 269, 270, 271, 65536, 65537, 65538))).build())
            val audioType = Media.MediaCodecType.MEDIA_CODEC_AUDIO_PCM
            if (!isSelfMode) {
                addServices(Control.Service.newBuilder().setId(4).setMediaSinkService(Control.Service.MediaSinkService.newBuilder().setAvailableType(audioType).setAudioType(Media.AudioStreamType.SPEECH).addAudioConfigs(Media.AudioConfiguration.newBuilder().setSampleRate(16000).setNumberOfBits(16).setNumberOfChannels(1))).build())
                addServices(Control.Service.newBuilder().setId(6).setMediaSinkService(Control.Service.MediaSinkService.newBuilder().setAvailableType(audioType).setAudioType(Media.AudioStreamType.MEDIA).addAudioConfigs(Media.AudioConfiguration.newBuilder().setSampleRate(48000).setNumberOfBits(16).setNumberOfChannels(2))).build())
            }
            addServices(Control.Service.newBuilder().setId(5).setMediaSinkService(Control.Service.MediaSinkService.newBuilder().setAvailableType(audioType).setAudioType(Media.AudioStreamType.SYSTEM).addAudioConfigs(Media.AudioConfiguration.newBuilder().setSampleRate(16000).setNumberOfBits(16).setNumberOfChannels(1))).build())
            addServices(Control.Service.newBuilder().setId(7).setMediaSourceService(Control.Service.MediaSourceService.newBuilder().setType(audioType).setAudioConfig(Media.AudioConfiguration.newBuilder().setSampleRate(16000).setNumberOfBits(16).setNumberOfChannels(1))).build())
            addServices(Control.Service.newBuilder().setId(8).setBluetoothService(Control.Service.BluetoothService.newBuilder().setCarAddress("00:11:22:33:44:55")).build())
            addServices(Control.Service.newBuilder().setId(9).setMediaPlaybackService(Control.Service.MediaPlaybackStatusService.newBuilder()).build())
            addServices(Control.Service.newBuilder().setId(10).setNavigationStatusService(Control.Service.NavigationStatusService.newBuilder().setMinimumIntervalMs(1000).setType(Control.Service.NavigationStatusService.ClusterType.ImageCodesOnly)).build())
            setHeadunitInfo(Common.HeadUnitInfo.newBuilder().setHeadUnitMake("Google").setHeadUnitModel("Android Auto").setMake("Google").setModel("Android Auto").setYear("2025").setVehicleId("headlessunit-001").setHeadUnitSoftwareBuild("1").setHeadUnitSoftwareVersion("0.1.0").build())
        }.build()
        sendControlPacketDirect(Control.ControlMsgType.MESSAGE_SERVICE_DISCOVERY_RESPONSE_VALUE, response.toByteArray())
    }

    private fun sendDrivingStatusUnrestricted(channel: Int = serviceToChannelMap[1] ?: 0) {
        if (channel == 0) return
        val status = Sensors.SensorBatch.newBuilder().addDrivingStatus(Sensors.SensorBatch.DrivingStatusData.newBuilder().setStatus(Sensors.SensorBatch.DrivingStatusData.Status.UNRESTRICTED_VALUE)).build()
        sendControlPacketDirect(Sensors.SensorsMsgType.SENSOR_EVENT_VALUE, status.toByteArray(), channel)
    }

    private fun sendLocationUpdate(location: android.location.Location) {
        val channel = serviceToChannelMap[1] ?: return
        val batch = Sensors.SensorBatch.newBuilder().addLocationData(Sensors.SensorBatch.LocationData.newBuilder().setTimestamp(location.time).setLatitude((location.latitude * 1E7).toInt()).setLongitude((location.longitude * 1E7).toInt()).setAltitude((location.altitude * 1E2).toInt()).setBearing((location.bearing * 1E6).toInt()).setSpeed((location.speed * 1E3).toInt()).setAccuracy((location.accuracy * 1E3).toInt())).build()
        sendControlPacketDirect(Sensors.SensorsMsgType.SENSOR_EVENT_VALUE, batch.toByteArray(), channel)
    }

    private fun sendChannelOpenResponse(channel: Int) {
        val response = Control.ChannelOpenResponse.newBuilder().setStatus(Common.MessageStatus.STATUS_SUCCESS).build()
        sendControlPacketDirect(Control.ControlMsgType.MESSAGE_CHANNEL_OPEN_RESPONSE_VALUE, response.toByteArray(), channel)
    }

    private fun sendPing() {
        val response = Control.PingResponse.newBuilder().setTimestamp(System.nanoTime()).build()
        sendControlPacketDirect(Control.ControlMsgType.MESSAGE_PING_RESPONSE_VALUE, response.toByteArray())
    }

    private fun sendMediaAck(channel: Int) {
        val sessionId = sessionIds[channel] ?: return
        val ack = Media.Ack.newBuilder().setSessionId(sessionId).setAck(1).build()
        sendControlPacketDirect(Media.MediaMsgType.MEDIA_MESSAGE_ACK_VALUE, ack.toByteArray(), channel)
    }

    fun sendNightMode(isNight: Boolean) {
        this.currentNightMode = isNight
        val channel = serviceToChannelMap[1] ?: return
        val status = Sensors.SensorBatch.newBuilder().addNightMode(Sensors.SensorBatch.NightData.newBuilder().setIsNightMode(isNight)).build()
        sendControlPacketDirect(Sensors.SensorsMsgType.SENSOR_EVENT_VALUE, status.toByteArray(), channel)
    }

    fun sendTouch(ts: Long, action: Int, x: Int, y: Int) {
        val channel = serviceToChannelMap[3] ?: return
        val report = Input.InputReport.newBuilder().setTimestamp(ts * 1000000L).setTouchEvent(Input.TouchEvent.newBuilder().addPointerData(Input.TouchEvent.Pointer.newBuilder().setX(x).setY(y).setPointerId(0)).setAction(Input.TouchEvent.PointerAction.forNumber(action))).build()
        sendControlPacketDirect(Input.InputMsgType.EVENT_VALUE, report.toByteArray(), channel)
    }

    fun getAdvertisedWidth() = advertisedWidth
    fun getAdvertisedHeight() = advertisedHeight
    fun getMarginWidth() = marginWidth
    fun getMarginHeight() = marginHeight

    private fun sendControlPacketDirect(messageId: Int, payload: ByteArray, channel: Int = 0) {
        val handler = sendHandler ?: return
        handler.post {
            try {
                val plainBuffer = ByteBuffer.allocate(2 + payload.size)
                plainBuffer.putShort(messageId.toShort())
                plainBuffer.put(payload)
                var data = plainBuffer.array()
                
                var flags = 0x03 
                if (isSslEstablished && (messageId > 0x0004 || channel != 0)) {
                    val encrypted = sslManager.encrypt(data)
                    if (encrypted != null) { 
                        data = encrypted
                        flags = if (channel != 0 && (messageId shr 8) == 0) 0x0F else 0x0B 
                    } else {
                        Log.e("AAP_MANAGER", "sendControlPacket: SSL Encryption FAILED for Id:0x${"%04X".format(messageId)}")
                        return@post
                    }
                }
                
                val packet = ByteBuffer.allocate(4 + data.size)
                packet.put(channel.toByte()); packet.put(flags.toByte()); packet.putShort(data.size.toShort()); packet.put(data)
                
                clientSocket?.getOutputStream()?.let { os ->
                    os.write(packet.array())
                    os.flush()
                }
            } catch (e: Exception) {
                Log.e("AAP_MANAGER", "sendControlPacket ERROR: ${e.message}")
            }
        }
    }

    private fun startDecoder() {
        if (mediaCodec != null || displaySurface == null) return
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, advertisedWidth, advertisedHeight)
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec?.configure(format, displaySurface, null, 0)
            mediaCodec?.start()
            lastFrameTime = System.currentTimeMillis()
            outputThread = Thread {
                val bufferInfo = MediaCodec.BufferInfo()
                var frameCount = 0
                var lastFpsTime = System.currentTimeMillis()
                try {
                    while (!Thread.currentThread().isInterrupted && mediaCodec != null) {
                        try {
                            val index = mediaCodec?.dequeueOutputBuffer(bufferInfo, 1000) ?: -1
                            if (index >= 0) {
                                val surface = displaySurface
                                if (surface != null && surface.isValid) { 
                                    mediaCodec?.releaseOutputBuffer(index, true)
                                    frameCount++; lastFrameTime = System.currentTimeMillis() 
                                } else { mediaCodec?.releaseOutputBuffer(index, false) }
                                val now = System.currentTimeMillis()
                                if (now - lastFpsTime >= 1000) {
                                    Log.d("AAP_PERF", "Projection FPS: ${(frameCount * 1000f / (now - lastFpsTime)).toInt()}")
                                    frameCount = 0; lastFpsTime = now
                                }
                            }
                        } catch (e: Exception) { break }
                    }
                } catch (e: Exception) {}
            }.apply { name = "AA-VideoOutput"; priority = Thread.NORM_PRIORITY + 1; start() }
        } catch (e: Exception) { error("Decoder Init Failed.") }
    }

    private fun stopDecoder() {
        try { outputThread?.interrupt(); outputThread = null; mediaCodec?.stop(); mediaCodec?.release() } catch (e: Exception) {}
        mediaCodec = null
    }

    fun stop() { _state.value = ProjectionState.Stopping; disconnect() }

    fun error(message: String) {
        _state.value = ProjectionState.Error(message)
        disconnect()
    }

    fun disconnect() = disconnect(null)

    @Synchronized
    private fun disconnect(sessionGeneration: Long?) {
        if (sessionGeneration != null && sessionGeneration != connectionGeneration) return
        connectionGeneration++
        _state.value = ProjectionState.Disconnected
        heartbeatJob?.cancel(); readerThread?.interrupt()
        sendThread?.quitSafely()
        readerThread = null; sendThread = null; sendHandler = null
        stopDecoder(); stopAudio()
        try { clientSocket?.close() } catch (e: Exception) {}
        clientSocket = null; isSslEstablished = false; serviceToChannelMap.clear(); sessionIds.clear(); channelBuffers.clear()
        projectionScope?.cancel(); projectionScope = null
    }

    @Synchronized
    private fun isCurrentSession(sessionGeneration: Long): Boolean =
        sessionGeneration == connectionGeneration

    private fun feedToDecoder(data: ByteBuffer, length: Int, timestamp: Long) {
        val codec = mediaCodec ?: return
        try {
            val index = codec.dequeueInputBuffer(2000) 
            if (index >= 0) {
                val inputBuffer = codec.getInputBuffer(index)
                inputBuffer?.clear(); inputBuffer?.put(data)
                codec.queueInputBuffer(index, 0, length, timestamp, 0)
            }
        } catch (e: Exception) {}
    }

    private fun feedToAudio(channel: Int, data: ByteArray, length: Int) {
        val track = getAudioTrack(channel) ?: return
        if (length <= 0) return
        // In Self Mode Android Auto, the media app, and this AudioTrack all live on the
        // same phone. Asking AudioManager for focus here can make the Android Auto media
        // source lose focus and close service 6. That presents as one short burst of audio,
        // followed by repeated MEDIA_STOP / MEDIA_START cycles. The AAP focus protocol is
        // still answered with GAIN in handleControlMessageSync(); do not mirror every PCM
        // packet into a local platform focus request.
        // Never block the AAP reader on AudioTrack.write(). A blocked reader delays media ACKs,
        // then Android Auto tears down and reopens the audio channel (the audible-once-then-silent
        // failure visible in the log). Each AA audio service gets its own bounded writer.
        try {
            audioWriter(channel).execute {
                try {
                    // A MEDIA_STOP can release a track while its final PCM job is waiting.
                    // Never let that expected lifecycle race crash the application process.
                    if (audioTracks[channel] !== track) return@execute
                    val written = track.write(data, 0, length, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) {
                        Log.e("AAP_AUDIO", "AudioTrack write failed: code=$written channel=$channel")
                        return@execute
                    }
                    val frame = audioFramesReceived.getOrDefault(channel, 0)
                    if (frame == 0) {
                        Log.i(
                            "AAP_AUDIO",
                            "Receiving PCM audio: bytes=$written channel=$channel " +
                                "routed=${track.routedDevice?.productName ?: "unresolved"}"
                        )
                    }
                    // A transport/SSL log can be completely healthy while the Android mixer runs out
                    // of queued frames. Log only changes so a real audio underrun is visible in logcat.
                    val underruns = track.underrunCount
                    val previousUnderruns = audioUnderruns.put(channel, underruns)
                    if (previousUnderruns != null && underruns > previousUnderruns) {
                        Log.w(
                            "AAP_AUDIO",
                            "Audio underrun: channel=$channel count=$underruns " +
                                "frames=$frame bufferFrames=${track.bufferSizeInFrames}"
                        )
                    }
                    audioFramesReceived[channel] = frame + 1
                } catch (e: Exception) {
                    Log.w("AAP_AUDIO", "Ignoring audio write during track teardown: channel=$channel", e)
                }
            }
        } catch (_: RejectedExecutionException) {
            // The channel was stopped after getAudioTrack() but before this job was submitted.
            Log.d("AAP_AUDIO", "Dropped PCM for stopped channel=$channel")
        }
    }

    private fun audioWriter(channel: Int): ThreadPoolExecutor = audioWriters.getOrPut(channel) {
        ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            ArrayBlockingQueue(24),
            { runnable ->
                Thread({
                    // Java thread priority alone is not enough on Android; ask the scheduler
                    // for the audio priority before this writer can block in AudioTrack.write().
                    try {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                    } catch (_: Exception) {
                        // Playback can continue with the platform-assigned priority.
                    }
                    runnable.run()
                }, "AA-Audio-$channel").apply {
                    isDaemon = true
                }
            },
            // Keeping the newest PCM is preferable to delayed audio if output briefly stalls.
            ThreadPoolExecutor.DiscardOldestPolicy()
        )
    }

    private fun getAudioTrack(channel: Int): AudioTrack? {
        val serviceId = serviceToChannelMap.entries.find { it.value == channel }?.key ?: return null
        if (audioTracks.containsKey(channel)) return audioTracks[channel]
        val sampleRate = if (serviceId == 6) 48000 else 16000
        val channelConfig = if (serviceId == 6) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)
        if (minBufferSize <= 0) {
            Log.e("AAP_AUDIO", "Unsupported output format for AA service $serviceId")
            return null
        }
        val attributes = AudioAttributes.Builder()
            .setUsage(if (serviceId == 6) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(if (serviceId == 6) AudioAttributes.CONTENT_TYPE_MUSIC else AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(channelConfig)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()
        return try {
            AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(minBufferSize * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
                .also { track ->
                    // Let Android route the media track. Forcing a connected Bluetooth
                    // device can leave an AudioTrack unrouted on some vendor stacks.
                    track.setVolume(1.0f)
                    track.play()
                    audioTracks[channel] = track
                    Log.i(
                        "AAP_AUDIO",
                        "Audio output started: service=$serviceId rate=$sampleRate channel=$channel " +
                            "routed=${track.routedDevice?.productName ?: "pending"}"
                    )
                }
        } catch (e: Exception) {
            Log.e("AAP_AUDIO", "Unable to create AA audio output", e)
            null
        }
    }

    private fun stopAudio() {
        audioWriters.values.forEach { it.shutdownNow() }
        audioWriters.clear()
        audioTracks.values.forEach { try { it.stop(); it.release() } catch (e: Exception) {} }
        audioTracks.clear()
        audioFramesReceived.clear()
        audioUnderruns.clear()
    }

    private fun stopAudioChannel(channel: Int) {
        audioWriters.remove(channel)?.shutdownNow()
        audioFramesReceived.remove(channel)
        audioUnderruns.remove(channel)
        audioTracks.remove(channel)?.let { track ->
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    private fun extractPhoneName(payload: ByteArray): String? {
        try {
            var i = 0
            while (i < payload.size) {
                val tag = payload[i].toInt() and 0xFF
                val wireType = tag and 0x07
                val fieldNumber = tag shr 3
                i++
                if (fieldNumber == 4 && wireType == 2) {
                    val len = payload[i].toInt() and 0xFF; i++
                    return String(payload.copyOfRange(i, i + len))
                }
                when (wireType) {
                    0 -> { while (payload[i].toInt() and 0x80 != 0) i++; i++ }
                    1 -> i += 8
                    2 -> { val len = payload[i].toInt() and 0xFF; i += 1 + len }
                    5 -> i += 4
                    else -> return null
                }
            }
        } catch (e: Exception) {}
        return null
    }
}
