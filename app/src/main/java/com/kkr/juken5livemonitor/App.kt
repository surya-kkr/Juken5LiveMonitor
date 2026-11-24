package com.kkr.juken5livemonitor

import android.app.Application
import android.content.Context
import android.os.SystemClock
import org.conscrypt.Conscrypt
import java.security.Security

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // HUR Fix: Use Conscrypt for high-performance SSL
        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        } catch (e: Exception) {}
    }

    companion object {
        val appStartTime = SystemClock.elapsedRealtime()

        @Volatile
        var instance: App? = null
            private set

        fun get(context: Context): App = context.applicationContext as App
    }
}