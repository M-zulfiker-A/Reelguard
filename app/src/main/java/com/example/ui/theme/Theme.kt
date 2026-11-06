package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AirbnbMinimalColorScheme = lightColorScheme(
    primary = AirbnbRausch,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF0F3),
    onPrimaryContainer = AirbnbRauschDark,
    secondary = AirbnbCharcoal,
    onSecondary = Color.White,
    secondaryContainer = AirbnbSurface,
    onSecondaryContainer = AirbnbCharcoal,
    tertiary = AirbnbBabuTeal,
    onTertiary = Color.White,
    background = AirbnbWhite,
    onBackground = AirbnbCharcoal,
    surface = AirbnbWhite,
    onSurface = AirbnbCharcoal,
    surfaceVariant = AirbnbSurface,
    onSurfaceVariant = AirbnbSlate,
    outline = AirbnbBorder,
    outlineVariant = AirbnbDivider,
    error = StageFogTerracotta,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AirbnbMinimalColorScheme,
        typography = Typography,
        content = content
    )
}
