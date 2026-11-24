package com.kkr.juken5livemonitor.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kkr.juken5livemonitor.screen.Connect.ConnectScreen
import com.kkr.juken5livemonitor.screen.MonitorScreen
import com.kkr.juken5livemonitor.screen.log.LogHistoryScreen
import com.kkr.juken5livemonitor.screen.log.LogPlaybackScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "connect",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {

        composable("connect") {

            ConnectScreen(

                navController

            )

        }

        composable("com/kkr/juken5livemonitor/screen/monitor") {

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

        composable("log_history") {
            LogHistoryScreen(navController = navController)
        }

        composable(
            "log_playback/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            LogPlaybackScreen(navController = navController, sessionId = sessionId)
        }
    }

}