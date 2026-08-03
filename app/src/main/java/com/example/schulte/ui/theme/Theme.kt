package com.example.schulte.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = SurfaceLight,
    primaryContainer = PeriwinkleContainer,
    onPrimaryContainer = Color(0xFF232352),
    secondary = Mint,
    onSecondary = SurfaceLight,
    secondaryContainer = MintContainer,
    onSecondaryContainer = Color(0xFF0F3B30),
    tertiary = Coral,
    onTertiary = SurfaceLight,
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = Color(0xFF4E1818),
    background = ScreenBackgroundLight,
    onBackground = Color(0xFF1A1A25),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1A25),
    surfaceVariant = CellLight,
    onSurfaceVariant = TextSecondaryLight,
    error = Coral,
    outline = DividerLight,
    outlineVariant = DividerLight,
)

private val DarkColors = darkColorScheme(
    primary = IndigoDark,
    onPrimary = Color(0xFF2A2A4E),
    primaryContainer = PeriwinkleContainerDark,
    onPrimaryContainer = IndigoDark,
    secondary = Mint,
    onSecondary = Color(0xFF0F3B30),
    secondaryContainer = MintContainerDark,
    onSecondaryContainer = Color(0xFFB4EFD8),
    tertiary = Coral,
    onTertiary = Color(0xFF4E1819),
    tertiaryContainer = CoralContainerDark,
    onTertiaryContainer = Color(0xFFFFC1C1),
    background = ScreenBackgroundDark,
    onBackground = Color(0xFFECECF6),
    surface = SurfaceDark,
    onSurface = Color(0xFFECECF6),
    surfaceVariant = CellDark,
    onSurfaceVariant = TextSecondaryDark,
    error = Color(0xFFFF8A8A),
    outline = DividerDark,
    outlineVariant = DividerDark,
)

@Composable
fun SchulteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}