package com.megalauncher.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Fallback-палитра для устройств ниже Android 12 (где Material You недоступен).
 */
private val FallbackDarkColors = darkColorScheme(
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

private val FallbackLightColors = lightColorScheme(
    primary = Accent,
    onPrimary = TextPrimary,
    secondary = AccentDim,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF5F5F7),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE0E0E5),
    onSurfaceVariant = TextSecondary
)

@Composable
fun MegaLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Material You (Monet): цвета из обоев системы, Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Fallback: если Monet недоступен
        darkTheme -> FallbackDarkColors
        else -> FallbackLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
