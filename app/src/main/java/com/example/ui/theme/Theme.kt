package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GroomlyDarkColorScheme = darkColorScheme(
    primary = GroomlyElectricLime,
    onPrimary = GroomlyObsidian,
    primaryContainer = GroomlySurfaceElevated,
    onPrimaryContainer = GroomlyElectricLime,
    secondary = GroomlyWarmIvory,
    onSecondary = GroomlyObsidian,
    secondaryContainer = GroomlySurfaceVariant,
    onSecondaryContainer = GroomlyWarmIvory,
    tertiary = GroomlyVibrantViolet,
    onTertiary = Color.White,
    background = GroomlyObsidian,
    onBackground = GroomlyWarmIvory,
    surface = GroomlyCharcoal,
    onSurface = GroomlyWarmIvory,
    surfaceVariant = GroomlySurfaceElevated,
    onSurfaceVariant = GroomlyIvoryMuted,
    error = GroomlyErrorCoral,
    onError = GroomlyObsidian
)

private val GroomlyLightColorScheme = lightColorScheme(
    primary = GroomlyObsidian,
    onPrimary = GroomlyElectricLime,
    primaryContainer = GroomlyLightSurface,
    onPrimaryContainer = GroomlyObsidian,
    secondary = GroomlyVibrantViolet,
    onSecondary = Color.White,
    secondaryContainer = GroomlyVioletSoft,
    onSecondaryContainer = GroomlyObsidian,
    tertiary = GroomlyLimeDark,
    onTertiary = GroomlyObsidian,
    background = GroomlyWarmIvory,
    onBackground = GroomlyObsidian,
    surface = GroomlyLightCard,
    onSurface = GroomlyObsidian,
    surfaceVariant = GroomlyLightSurface,
    onSurfaceVariant = Color(0xFF4A4E58),
    error = Color(0xFFD92D20),
    onError = Color.White
)

@Composable
fun GroomlyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GroomlyDarkColorScheme else GroomlyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
