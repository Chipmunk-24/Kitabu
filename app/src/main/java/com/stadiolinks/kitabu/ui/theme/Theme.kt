package com.stadiolinks.kitabu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = KitabuTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBF0),
    onPrimaryContainer = Color(0xFF00363F),
    secondary = Color(0xFF4A6267),
    background = Color(0xFFF6F8F8),
    onBackground = Color(0xFF191C1D),
    surface = Color.White,
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFE6ECED),
    onSurfaceVariant = Color(0xFF51606A),
    outlineVariant = Color(0xFFD3DBDD)
)

private val DarkColors = darkColorScheme(
    primary = KitabuTealLight,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF0B4A57),
    onPrimaryContainer = Color(0xFFCDEBF0),
    secondary = Color(0xFFB1CBD0),
    background = Color(0xFF101415),
    onBackground = Color(0xFFE0E3E3),
    surface = Color(0xFF171C1D),
    onSurface = Color(0xFFE0E3E3),
    surfaceVariant = Color(0xFF283031),
    onSurfaceVariant = Color(0xFFB4C0C4),
    outlineVariant = Color(0xFF3B4749)
)

private val KitabuShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KitabuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KitabuTypography,
        shapes = KitabuShapes,
        content = content
    )
}
