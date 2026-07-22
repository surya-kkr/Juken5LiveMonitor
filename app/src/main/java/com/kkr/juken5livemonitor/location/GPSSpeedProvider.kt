package com.kkr.juken5livemonitor.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*

class GPSSpeedProvider(
    context: Context
) {

    private val client =
        LocationServices.getFusedLocationProviderClient(context)

    private var callback: ((Float) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun start(
        onSpeedChanged: (Float) -> Unit
    ) {

        callback = onSpeedChanged

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            200L
        )
            .setMinUpdateIntervalMillis(100)
            .build()

        client.requestLocationUpdates(
            request,
            locationCallback,
            null
        )

    }

    fun stop() {

        client.removeLocationUpdates(locationCallback)

    }

    private val locationCallback =
        object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                val location: Location =
                    result.lastLocation ?: return

                val speed =
                    location.speed * 3.6f

                callback?.invoke(speed)

            }

        }

}