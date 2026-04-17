package com.example.tsumaps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val tsuBlue = Color(0xFF1A56A7)
private val tsuBlueDark = Color(0xFF2E6FD4)

private val LightColors = lightColorScheme(
    primary = tsuBlue,
    onPrimary = Color.White,
    secondary = tsuBlue,
    background = Color(0xFFF8F9FB),
    surface = Color.White,
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE8EDF5),
    outline = Color(0xFFCCD5E0)
)

private val DarkColors = darkColorScheme(
    primary = tsuBlueDark,
    onPrimary = Color.White,
    secondary = tsuBlueDark,
    background = Color(0xFF111827),
    surface = Color(0xFF1E2A3A),
    onBackground = Color(0xFFECF0F5),
    onSurface = Color(0xFFECF0F5),
    surfaceVariant = Color(0xFF243447),
    outline = Color(0xFF3A4F65)
)

@Composable
fun TSUMapsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
