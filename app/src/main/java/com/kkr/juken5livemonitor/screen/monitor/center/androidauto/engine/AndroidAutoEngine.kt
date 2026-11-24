package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine

import android.content.Context
import android.view.SurfaceHolder
import kotlinx.coroutines.flow.StateFlow

class AndroidAutoEngine(private val context: Context) {

    private val projectionManager = ProjectionManager(context)
    
    var onPhoneNameReceived: ((String) -> Unit)?
        get() = projectionManager.onPhoneNameReceived
        set(value) { projectionManager.onPhoneNameReceived = value }

    val state: StateFlow<ProjectionState>
        get() = projectionManager.state

    fun initialize() {
        projectionManager.initialize()
    }

    fun waitProjection() {
        projectionManager.waitingProjection()
    }

    fun setDimensions(width: Int, height: Int) {
        projectionManager.setDimensions(width, height)
    }

    fun setDpi(dpi: Int) {
        projectionManager.setDpi(dpi)
    }

    /**
     * Dipanggil saat SurfaceView sudah siap.
     */
    fun attachSurface(holder: SurfaceHolder) {
        // HAPUS render manual (SurfaceRenderer & RenderScheduler).
        // Serahkan Surface langsung ke ProjectionManager agar didecode oleh Android Auto.
        projectionManager.setSurface(holder.surface)
    }

    /**
     * Dipanggil saat Surface dihancurkan.
     */
    fun detachSurface() {
        // Beritahu manager bahwa surface sudah dilepas agar decoder berhenti merender.
        projectionManager.setSurface(null)
    }

    fun start() {
        projectionManager.running()
    }

    fun startSelfMode() {
        projectionManager.startSelfMode()
    }

    fun stopSelfMode() {
        projectionManager.stopSelfMode()
    }

    fun stop() {
        projectionManager.stop()
    }

    fun sendNightMode(isNight: Boolean) {
        projectionManager.sendNightMode(isNight)
    }

    fun error(message: String) {
        projectionManager.error(message)
    }

    fun sendTouch(ts: Long, action: Int, x: Int, y: Int) {
        projectionManager.sendTouch(ts, action, x, y)
    }

    fun getAdvertisedWidth() = projectionManager.getAdvertisedWidth()
    fun getAdvertisedHeight() = projectionManager.getAdvertisedHeight()
    fun getPhysicalWidth() = projectionManager.getPhysicalWidth()
    fun getPhysicalHeight() = projectionManager.getPhysicalHeight()
    fun getMarginWidth() = projectionManager.getMarginWidth()
    fun getMarginHeight() = projectionManager.getMarginHeight()

    fun destroy() {
        projectionManager.disconnect()
    }
}
