package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicDarkColorScheme = darkColorScheme(
    primary = SolarPrimary,
    onPrimary = Color(0xFF111318),
    primaryContainer = CosmicSurfaceVariant,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = PlanetSecondary,
    onSecondary = Color(0xFF111318),
    secondaryContainer = Color(0xFF202B3A),
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = NebulaTertiary,
    background = SpaceBackground,
    onBackground = OnSpaceBackground,
    surface = CosmicSurface,
    onSurface = OnCosmicSurface,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = OnSpaceBackground,
    outline = BorderColor,
    outlineVariant = BorderColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force modern dark mode as requested by user
    dynamicColor: Boolean = false, // Disable dynamic styling to maintain professional astronomical palette
    content: @Composable () -> Unit,
) {
    // We enforce CosmicDarkColorScheme to keep a professional scientific dark appearance
    MaterialTheme(
        colorScheme = CosmicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
