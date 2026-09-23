package com.megalauncher.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = TextPrimary,
    secondary = AccentDim,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark2,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MegaLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, typography = AppTypography, content = content)
}
