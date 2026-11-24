package com.kkr.juken5livemonitor

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kkr.juken5livemonitor.navigation.AppNavigation
import com.kkr.juken5livemonitor.ui.theme.Juken5LiveMonitorTheme

class MainActivity : ComponentActivity() {

    private val backgroundSettingsPreferences by lazy {
        getSharedPreferences("permission_settings", MODE_PRIVATE)
    }

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // A denied runtime permission must not leave the UI blank. The Connect
            // screen will remain available and Android will report the missing access.
            requestBatteryAndBackgroundAccess()
        }

    private val batteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            openBackgroundActivitySettings()
        }

    private val backgroundActivitySettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            startApp()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layar tidak mati
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Fullscreen Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {

            hide(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
            )

            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        if (hasRequiredPermissions()) {
            requestBatteryAndBackgroundAccess()
        } else {
            requestRequiredPermissions()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun startApp() {

        setContent {

            Juken5LiveMonitorTheme {

                AppNavigation()

            }

        }

    }

    private fun hasRequiredPermissions(): Boolean {

        val micPerm = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val btPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

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

        val locationPerm =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

        val notifyPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return micPerm && btPerms && locationPerm && notifyPerm

    }

    private fun requestRequiredPermissions() {

        val perms = mutableListOf<String>()
        perms.add(Manifest.permission.RECORD_AUDIO)
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (perms.isNotEmpty()) {
            permissionLauncher.launch(perms.toTypedArray())
        } else {
            startApp()
        }

    }

    /**
     * Android exposes battery optimization as a special-access dialog. There is no
     * public Android API for MIUI's "Background activity" switch, so we open this
     * app's Settings page once after the battery dialog for the user to enable it.
     */
    private fun requestBatteryAndBackgroundAccess() {
        val powerManager = getSystemService(PowerManager::class.java)
        val batteryOptimizationIgnored = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                powerManager.isIgnoringBatteryOptimizations(packageName)

        if (!batteryOptimizationIgnored) {
            batteryOptimizationLauncher.launch(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }

        openBackgroundActivitySettings()
    }

    private fun openBackgroundActivitySettings() {
        if (backgroundSettingsPreferences.getBoolean("background_settings_opened", false)) {
            startApp()
            return
        }

        backgroundSettingsPreferences.edit()
            .putBoolean("background_settings_opened", true)
            .apply()

        // MIUI exposes background/autostart control in Security Center rather than
        // through a public Android runtime permission. Open it directly on Xiaomi,
        // Redmi and POCO; other devices (or MIUI versions with a different activity)
        // fall back to this app's standard App Info page.
        val miuiAutostartIntent = Intent("miui.intent.action.OP_AUTO_START").apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        }
        val isXiaomiFamily = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
                Build.BRAND.equals("Xiaomi", ignoreCase = true) ||
                Build.BRAND.equals("Redmi", ignoreCase = true) ||
                Build.BRAND.equals("POCO", ignoreCase = true)

        val settingsIntent = if (
            isXiaomiFamily && miuiAutostartIntent.resolveActivity(packageManager) != null
        ) {
            miuiAutostartIntent
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
        }

        backgroundActivitySettingsLauncher.launch(settingsIntent)
    }

}
