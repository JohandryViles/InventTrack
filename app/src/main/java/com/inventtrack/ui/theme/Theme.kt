package com.inventtrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InventTrackTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme(
        primary = Color(0xFF91D5BA), secondary = Color(0xFFFFBE85)
    ) else lightColorScheme(
        primary = Color(0xFF176B53), secondary = Color(0xFF9C541B),
        background = Color(0xFFF5F8F5), surface = Color(0xFFF5F8F5)
    )
    MaterialTheme(colorScheme = colors, typography = Typography(), shapes = Shapes(
        small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp)
    ), content = content)
}
