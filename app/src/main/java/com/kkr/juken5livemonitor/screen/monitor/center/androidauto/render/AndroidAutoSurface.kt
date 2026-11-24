package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.render

import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AndroidAutoSurface(
    modifier: Modifier = Modifier,
    onSurfaceReady: (SurfaceHolder) -> Unit,
    onSurfaceDestroyed: () -> Unit,
    onDimensionsChanged: (Int, Int) -> Unit = { _, _ -> }
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).apply {
                holder.addCallback(
                    object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            Log.d("AA_SURFACE", "Surface Created")
                            onSurfaceReady(holder)
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                            Log.d("AA_SURFACE", "Surface Changed : ${width}x$height")
                            onDimensionsChanged(width, height)
                            // Dimensions for TouchDispatcher are now handled by the parent Panel
                            // to ensure they match the visible area, not the raw SurfaceView.
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            Log.d("AA_SURFACE", "Surface Destroyed")
                            onSurfaceDestroyed()
                        }
                    }
                )
            }
        }
    )
}