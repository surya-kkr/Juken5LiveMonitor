package com.kkr.juken5livemonitor.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kkr.juken5livemonitor.screen.ConnectScreen
import com.kkr.juken5livemonitor.screen.MonitorScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(

        navController = navController,

        startDestination = "connect"

    ) {

        composable("connect") {

            ConnectScreen(

                navController

            )

        }

        composable("monitor") {

            MonitorScreen(

                navController

            )

        }
        composable("monitor_demo") {

            MonitorScreen(
                navController = navController,
                demoMode = true
            )

        }

    }

}