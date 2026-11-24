package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.render

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import android.util.Log
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun startLocalStreamDecoder(surface: Surface, port: Int = 5277, onStopped: () -> Unit) = withContext(Dispatchers.IO) {
    var socket: Socket? = null
    var mediaCodec: MediaCodec? = null
    try {
        // 1. Hubungkan ke local socket server Android Auto projection
        socket = Socket("127.0.0.1", port)
        val inputStream = socket.getInputStream()

        // 2. Inisialisasi MediaCodec untuk video H.264 / AVC (umum digunakan pada proyeksi)
        mediaCodec = MediaCodec.createDecoderByType("video/avc")

        // Sesuaikan resolusi dan format sesuai kebutuhan stream headunit kamu (misal 1280x720)
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1920, 1080)
        mediaCodec.configure(format, surface, null, 0)
        mediaCodec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val packet = ByteArray(4096)

        // 3. Loop membaca stream dan memasukkannya ke input buffer MediaCodec
        while (true) {
            val bytesRead = inputStream.read(packet)
            if (bytesRead == -1) break

            val inputBufferIndex = mediaCodec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(packet, 0, bytesRead)
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, bytesRead, System.nanoTime(), 0)
            }

            val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex >= 0) {
                // Render frame ke Surface secara langsung (true)
                mediaCodec.releaseOutputBuffer(outputBufferIndex, true)
            }
        }
    } catch (e: Exception) {
        Log.e("SelfModeDecoder", "Error streaming/decoding: ${e.message}")
    } finally {
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
            socket?.close()
        } catch (ignored: Exception) {}
        onStopped()
    }
}