package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.input

import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine.AndroidAutoEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TouchDispatcher(private val engine: AndroidAutoEngine) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    var viewWidth: Int = 0
    var viewHeight: Int = 0
    
    private var lastEventTime = 0L

    fun dispatch(event: MotionEvent) {
        val action = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> 0
            MotionEvent.ACTION_UP -> 1
            MotionEvent.ACTION_MOVE -> 2
            MotionEvent.ACTION_CANCEL -> 3
            else -> return
        }

        if (viewWidth <= 0 || viewHeight <= 0) return
        
        // Performance: Debounce MOVE events to 16ms (60Hz tracking for smooth swipe)
        val now = SystemClock.elapsedRealtime()
        if (action == 2 && now - lastEventTime < 16) return
        lastEventTime = now

        // Sync Logic:
        // We advertised a touch surface that exactly matches the active area (1280-margins).
        // Therefore, we just map local finger pixels directly to that range.
        val aaWidth = 1280 - engine.getMarginWidth()
        val aaHeight = 720 - engine.getMarginHeight()

        val aaX = (event.x / viewWidth * aaWidth).toInt().coerceIn(0, aaWidth - 1)
        val aaY = (event.y / viewHeight * aaHeight).toInt().coerceIn(0, aaHeight - 1)
        
        // DEBUG: Trace active-area mapping
        // Log.v("TOUCH_DEBUG", "Visible: ${viewWidth}x${viewHeight} | AdvTouch: ${aaWidth}x${aaHeight} | Raw: (${event.x}, ${event.y}) -> AA: ($aaX, $aaY)")
        
        scope.launch {
            try {
                engine.sendTouch(SystemClock.elapsedRealtime(), action, aaX, aaY)
            } catch (e: Exception) {}
        }
    }
}
