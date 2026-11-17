package com.jass.largy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

private val SlateColors = darkColorScheme(
    primary = Slate.Accent,
    onPrimary = Slate.Chalk,
    secondary = Slate.Accent,
    onSecondary = Slate.Chalk,
    background = Slate.Background,
    onBackground = Slate.Chalk,
    surface = Slate.Dialog,
    onSurface = Slate.Chalk,
    surfaceVariant = Slate.Surface,
    onSurfaceVariant = Slate.ChalkMuted,
    surfaceContainer = Slate.Dialog,
    surfaceContainerHigh = Slate.Dialog,
    surfaceContainerHighest = Slate.Dialog,
    outline = Slate.ChalkDim,
    outlineVariant = Slate.ChalkFaint,
)

private fun TextStyle.mono() = copy(fontFamily = FontFamily.Monospace)

private val SlateTypography = Typography().run {
    copy(
        displayLarge = displayLarge.mono(), displayMedium = displayMedium.mono(), displaySmall = displaySmall.mono(),
        headlineLarge = headlineLarge.mono(), headlineMedium = headlineMedium.mono(), headlineSmall = headlineSmall.mono(),
        titleLarge = titleLarge.mono(), titleMedium = titleMedium.mono(), titleSmall = titleSmall.mono(),
        bodyLarge = bodyLarge.mono(), bodyMedium = bodyMedium.mono(), bodySmall = bodySmall.mono(),
        labelLarge = labelLarge.mono(), labelMedium = labelMedium.mono(), labelSmall = labelSmall.mono(),
    )
}

/** L'ardoise est toujours sombre, quel que soit le thème système. */
@Composable
fun JassTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SlateColors, typography = SlateTypography, content = content)
}
