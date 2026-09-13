package com.example.fithub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FitHubPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = FitHubLightBlue,
    onPrimaryContainer = FitHubPrimary,

    secondary = FitHubSecondary,
    onSecondary = TextOnPrimary,

    tertiary = FitHubMidBlue,
    onTertiary = TextOnPrimary,

    background = BackgroundGray,
    onBackground = TextPrimary,

    surface = CardWhite,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = TextOnPrimary,

    outline = TextHint
)

@Composable
fun FitHubTheme(
    content: @Composable () -> Unit
) {
    // Force light theme for now — matches screenshots. Dark mode can be added later.
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}