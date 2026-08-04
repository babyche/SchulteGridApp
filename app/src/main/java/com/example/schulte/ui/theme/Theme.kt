package com.example.schulte.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OnOrangeContainer,
    secondary = InkGreenSoft,
    onSecondary = Color.White,
    secondaryContainer = GreenContainer,
    onSecondaryContainer = OnGreenContainer,
    tertiary = SoftCoral,
    onTertiary = Color.White,
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = OnCoralContainer,
    background = WarmBase,
    onBackground = InkGreenDeep,
    surface = FrostSurface,
    onSurface = InkGreenDeep,
    surfaceVariant = FrostWhite,
    onSurfaceVariant = TextMuted,
    error = SoftCoral,
    outline = Hairline,
    outlineVariant = Hairline,
)

private val DarkColors = darkColorScheme(
    primary = DarkOrange,
    onPrimary = Color(0xFF3A1700),
    primaryContainer = Color(0xFF4A2A14),
    onPrimaryContainer = Color(0xFFFFD9C2),
    secondary = DarkGreen,
    onSecondary = Color(0xFF0F2A20),
    secondaryContainer = Color(0xFF1F3A30),
    onSecondaryContainer = Color(0xFFC2E6D5),
    tertiary = Color(0xFFFF9A8A),
    onTertiary = Color(0xFF5E1E14),
    tertiaryContainer = Color(0xFF572A22),
    onTertiaryContainer = Color(0xFFFFD9D1),
    background = DarkBase,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkMuted,
    error = Color(0xFFFF9A8A),
    outline = DarkHairline,
    outlineVariant = DarkHairline,
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
