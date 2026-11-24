package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine

import android.util.Log

class ProjectionLifecycle {
    fun onStart() {
        Log.d("AA_LIFECYCLE", "Projection Started")
    }

    fun onStop() {
        Log.d("AA_LIFECYCLE", "Projection Stopped")
    }
}
