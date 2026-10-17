package com.inventtrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryTeal,
    onPrimary = PrimaryDarkBlue,
    secondary = SecondaryTeal,
    onSecondary = PrimaryDarkBlue,
    tertiary = SecondaryTeal,
    onTertiary = PrimaryDarkBlue,
    background = PrimaryDarkBlue,
    onBackground = BackgroundLight,
    surface = PrimaryDarkBlue,
    onSurface = BackgroundLight,
    error = CriticalRed,
    onError = SurfaceLight,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = BackgroundLight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDarkBlue,
    onPrimary = SurfaceLight,
    secondary = SecondaryTeal,
    onSecondary = SurfaceLight,
    tertiary = SecondaryTeal,
    onTertiary = SurfaceLight,
    background = BackgroundLight,
    onBackground = PrimaryDarkBlue,
    surface = SurfaceLight,
    onSurface = PrimaryDarkBlue,
    error = CriticalRed,
    onError = SurfaceLight,
    surfaceVariant = ChatBubbleBot,
    onSurfaceVariant = PrimaryDarkBlue
)

val ColorScheme.criticalRed: Color get() = CriticalRed
val ColorScheme.lowOrange: Color get() = LowOrange
val ColorScheme.availableGreen: Color get() = AvailableGreen
val ColorScheme.chatBubbleBot: Color get() = if (primary == PrimaryDarkBlue) ChatBubbleBot else Color(0xFF334155)
val ColorScheme.chatBubbleUser: Color get() = ChatBubbleUser

@Composable
fun InventTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}