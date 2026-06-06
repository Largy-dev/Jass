package com.largy.jass.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GreenFeltLight,
    onPrimary = Color.White,
    primaryContainer = GreenFeltContainer,
    onPrimaryContainer = GreenFeltDark,
    secondary = RedCard,
    onSecondary = Color.White,
    secondaryContainer = RedCardContainer,
    onSecondaryContainer = RedCard,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    surface = SurfaceCard,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color(0xFF2E4D2E),
    background = BackgroundLight,
    onBackground = Color(0xFF1A1A1A),
    error = Color(0xFFB00020),
    outline = Color(0xFF718F71),
    outlineVariant = Color(0xFFB8D5B8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003300),
    primaryContainer = GreenFeltDark,
    onPrimaryContainer = GreenFeltContainer,
    secondary = RedCardLight,
    onSecondary = Color(0xFF4A0000),
    secondaryContainer = Color(0xFF7A1010),
    onSecondaryContainer = RedCardContainer,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    surface = SurfaceCardDark,
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF1E2B1E),
    onSurfaceVariant = Color(0xFFB8D5B8),
    background = BackgroundDark,
    onBackground = Color(0xFFE8F5E9),
    outline = Color(0xFF71997E),
    outlineVariant = Color(0xFF3A5040),
)

@Composable
fun JassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
