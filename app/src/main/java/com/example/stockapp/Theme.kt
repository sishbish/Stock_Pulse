package com.example.stockapp

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// Defining the exact hex color codes used across the app layout
val PureDarkBackground = Color(0xFF121212)
val SolidCardSurface = Color(0xFF1E1E1E)
val TextPrimaryWhite = Color(0xFFFFFFFF)
val TextSecondaryGray = Color(0xB3FFFFFF)
val AccentPurple = Color(0xFFBB86FC)
val CustomGreen = Color(0xFF388E3C)
val CustomRed = Color(0xFFD32F2F)

private val StrictDarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    background = PureDarkBackground,
    surface = SolidCardSurface,
    onBackground = TextPrimaryWhite,
    onSurface = TextPrimaryWhite,
    onSurfaceVariant = TextSecondaryGray,
    error = CustomRed
)

@Composable
fun StockPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StrictDarkColorScheme,
        content = content
    )
}