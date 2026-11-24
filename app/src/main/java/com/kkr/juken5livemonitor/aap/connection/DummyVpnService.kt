package com.kkr.juken5livemonitor.aap.connection

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class DummyVpnService : VpnService() {
    private val TAG = "AA/VPN"
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_VPN) {
            stopVpn()
            return START_NOT_STICKY
        }
        startVpn()
        return START_NOT_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .setSession("Juken AA Loopback Mode")
            
            vpnInterface = builder.establish()
            Log.i(TAG, "Dummy VPN established for Self Mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish Dummy VPN", e)
            stopSelf()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            stopSelf()
            Log.i(TAG, "Dummy VPN stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    companion object {
        const val ACTION_STOP_VPN = "com.kkr.juken5livemonitor.aap.STOP_VPN"
    }
}
