package com.jass.largy.ui.theme

import androidx.compose.ui.graphics.Color

/** Palette « ardoise » : fond sombre, craie claire, accent orange. */
object Slate {
    val Background = Color(0xFF1A1A14)
    val Surface = Color(0xFF222218)
    val Dialog = Color(0xFF252515)
    val Chalk = Color(0xFFF0EDD5)
    val Accent = Color(0xFFCC4400)

    val ChalkFaint = Chalk.copy(alpha = 0.08f)
    val ChalkDim = Chalk.copy(alpha = 0.3f)
    val ChalkMuted = Chalk.copy(alpha = 0.5f)
    val Line = Accent.copy(alpha = 0.5f)
}
