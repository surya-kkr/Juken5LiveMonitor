package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.render

import android.util.Log
import kotlinx.coroutines.*

class RenderScheduler(
    private val render: () -> Unit
) {

    private var scope: CoroutineScope? = null

    fun start() {

        if (scope != null) return

        Log.d("AA_SCHEDULER", "Starting scheduler loop")

        scope = CoroutineScope(
            Dispatchers.Default + SupervisorJob()
        )

        scope?.launch {

            while (isActive) {

                render()

                delay(16L) // Target 60 FPS (approx)

            }

        }

    }

    fun stop() {

        Log.d("AA_SCHEDULER", "Stopping scheduler loop")

        scope?.cancel()

        scope = null

    }

}
