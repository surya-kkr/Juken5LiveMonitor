package com.kkr.juken5livemonitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(

    primary = Color(0xFFD50000),
    onPrimary = Color.White,

    secondary = Color(0xFFFF3D3D),
    onSecondary = Color.White,

    tertiary = Color(0xFFFF5252),

    background = Color(0xFF0F0F0F),
    onBackground = Color.White,

    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,

    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color.White

)

@Composable
fun Juken5LiveMonitorTheme(

    content: @Composable () -> Unit

) {

    MaterialTheme(

        colorScheme = DarkColorScheme,

        typography = Typography,

        content = content

    )

}