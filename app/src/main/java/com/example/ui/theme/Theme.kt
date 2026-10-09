package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ParosaColorScheme =
  darkColorScheme(
    primary = ParosaOrange,
    onPrimary = Black,
    primaryContainer = ParosaOrangeContainer,
    onPrimaryContainer = ParosaOrange,
    secondary = ParosaGreen,
    onSecondary = Black,
    secondaryContainer = ParosaGreenContainer,
    onSecondaryContainer = ParosaGreen,
    tertiary = ParosaOrange,
    background = ParosaBg,
    surface = ParosaSurface,
    onBackground = ParosaTextPrimary,
    onSurface = ParosaTextPrimary,
    surfaceVariant = ParosaSurfaceElevated,
    onSurfaceVariant = ParosaTextSecondary,
    outline = ParosaBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = ParosaColorScheme, typography = Typography, content = content)
}
