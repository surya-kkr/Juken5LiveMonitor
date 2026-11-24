package com.kkr.juken5livemonitor.screen.monitor.center.androidauto

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine.AndroidAutoEngine
import com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine.ProjectionState
import com.kkr.juken5livemonitor.screen.monitor.center.androidauto.input.TouchDispatcher
import com.kkr.juken5livemonitor.screen.monitor.center.androidauto.render.AndroidAutoSurface
import com.kkr.juken5livemonitor.ui.components.GlassCard
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AndroidAutoPanel(
    modifier: Modifier = Modifier,
    vm: MonitorViewModel? = null
) {
    if (vm == null) return

    val engine = vm.aaEngine
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val engineState by engine.state.collectAsState()
    val aaDpi by vm?.aaDpi?.collectAsState() ?: remember { mutableStateOf(140) }
    
    // Stable Lifecycle Guard
    var isMeasured by remember { mutableStateOf(false) }

    LaunchedEffect(engineState) {
        if (engineState is ProjectionState.Running) {
            engine.sendNightMode(true) 
        }
    }

    LaunchedEffect(aaDpi) {
        engine.setDpi(aaDpi)
        // If engine is already running or waiting, we need to restart the connection 
        // to re-announce the new DPI during the handshake.
        if (isMeasured && (engineState is ProjectionState.Running || engineState is ProjectionState.WaitingProjection)) {
            Log.d("AA_PANEL", "DPI Changed to $aaDpi. Reloading Engine...")
            engine.stop()
            engine.initialize()
            engine.waitProjection()
            engine.start() 
        }
    }
    
    val touchDispatcher = remember { TouchDispatcher(engine) }

    DisposableEffect(engine, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> engine.stop()
                Lifecycle.Event.ON_START -> if (isMeasured) {
                    engine.initialize()
                    engine.waitProjection()
                    engine.start()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        engine.onPhoneNameReceived = { name -> vm.setPhoneName(name) }
        engine.initialize()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            engine.detachSurface()
            engine.stop()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onGloballyPositioned { coords ->
                if (!isMeasured) {
                    val width = coords.size.width
                    val height = coords.size.height
                    
                    engine.setDimensions(width, height)
                    
                    // Update TouchDispatcher with the visible dimensions
                    touchDispatcher.viewWidth = width
                    touchDispatcher.viewHeight = height
                    
                    isMeasured = true
                    engine.waitProjection()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isMeasured) {
            AndroidAutoSurface(
                // Forced 16:9 container to avoid stretching
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                
                onSurfaceReady = { holder ->
                    engine.attachSurface(holder)
                    engine.start()
                },
                onSurfaceDestroyed = {
                    engine.detachSurface()
                }
            )

            // Transparent touch layer that covers ONLY the visible dashboard area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInteropFilter { event ->
                        touchDispatcher.dispatch(event)
                        true
                    }
            )

        }

        // Startup Overlay: Only shown when initializing or waiting for projection
        if (engineState is ProjectionState.Initializing || engineState is ProjectionState.WaitingProjection) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .width(280.dp)
                        .wrapContentSize()
                        .offset(x = 20.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.Red,
                            strokeWidth = 4.dp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Menunggu Android Auto",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Buka Android Auto > Developer settings > Start Headunit Server",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

}
