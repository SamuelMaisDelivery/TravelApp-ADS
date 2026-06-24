package com.senac.restapi.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TravelColorScheme = lightColorScheme(
    primary = TravelGreen,
    onPrimary = TravelOnPrimary,
    primaryContainer = TravelGreenLight,
    onPrimaryContainer = TravelGreenDark,
    secondary = TravelBlue,
    onSecondary = TravelOnPrimary,
    secondaryContainer = TravelBlueLight,
    onSecondaryContainer = TravelBlueDark,
    background = TravelBackground,
    surface = TravelSurface,
    onBackground = TravelOnSurface,
    onSurface = TravelOnSurface,
    error = TravelError
)

@Composable
fun RestApiTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = TravelColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TravelGreen.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
