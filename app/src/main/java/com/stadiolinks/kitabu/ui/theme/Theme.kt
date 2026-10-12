package com.stadiolinks.kitabu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = KitabuOrange,
    onPrimary = Color.White,
    secondary = Color(0xFF5D4037),
    onSecondary = Color.White
)

private val DarkColors = darkColorScheme(
    primary = KitabuOrangeLight,
    onPrimary = Color(0xFF3E2000),
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color(0xFF2B1B16)
)

@Composable
fun KitabuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
