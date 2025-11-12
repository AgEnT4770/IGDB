package com.example.igdb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Lavender,
    secondary = MutedLavender,
    background = NavyBlue,
    surface = LightNavyBlue,
    onPrimary = White,
    onSecondary = White,
    onBackground = White,
    onSurface = White,
    primaryContainer = LightNavyBlue,
    onPrimaryContainer = White,
    surfaceVariant = LightNavyBlue,
    onSurfaceVariant = MutedLavender
)

@Composable
fun IGDBTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}