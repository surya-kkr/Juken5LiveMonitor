package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.input

import android.util.Log
import android.view.KeyEvent

class KeyDispatcher {
    fun dispatch(event: KeyEvent) {
        Log.d("AA_INPUT", "Key: ${event.keyCode} action: ${event.action}")
    }
}
