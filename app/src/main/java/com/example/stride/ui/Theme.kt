package com.example.stride.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Pavement = Color(0xFFE9EEF2)
val Ink = Color(0xFF13222E)
val Stride = Color(0xFF2563EB)
val Sunrise = Color(0xFFF2A900)
val Mist = Color(0xFFB9C6D1)

private val Colors = lightColorScheme(
    primary = Stride,
    onPrimary = Color.White,
    background = Pavement,
    onBackground = Ink,
    surface = Pavement,
    onSurface = Ink,
)

@Composable
fun StrideTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
