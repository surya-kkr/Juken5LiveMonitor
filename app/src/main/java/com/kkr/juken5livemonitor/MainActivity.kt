package com.kkr.juken5livemonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.kkr.juken5livemonitor.navigation.AppNavigation
import com.kkr.juken5livemonitor.ui.theme.Juken5LiveMonitorTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
                } else {
                    true
                }

            if (granted) {
                startApp()
            } else {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasBluetoothPermission()) {
            startApp()
        } else {
            requestBluetoothPermission()
        }
    }

    private fun startApp() {

        setContent {

            Juken5LiveMonitorTheme {

                AppNavigation()

            }

        }

    }

    private fun hasBluetoothPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED

        } else {

            true

        }

    }

    private fun requestBluetoothPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )

        } else {

            startApp()

        }

    }

}