package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.Black,
    primaryContainer = AircraftSurface,
    onPrimaryContainer = PrimaryColor,
    secondary = SecondaryColor,
    onSecondary = Color.Black,
    secondaryContainer = AircraftCardBg,
    onSecondaryContainer = SecondaryColor,
    tertiary = TertiaryColor,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = AircraftSurface,
    onSurfaceVariant = TextMuted,
    outline = AircraftCardBorder,
    error = WarningRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
