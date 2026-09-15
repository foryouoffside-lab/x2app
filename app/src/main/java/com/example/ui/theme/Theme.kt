package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReactionColorScheme = darkColorScheme(
    primary = BrandAccent,
    onPrimary = TextInverse,
    primaryContainer = Color(0xFF0F3A2E),
    onPrimaryContainer = BrandAccent,
    secondary = CoolBlue,
    onSecondary = TextInverse,
    secondaryContainer = Color(0xFF0F3A2E),
    onSecondaryContainer = CoolBlue,
    tertiary = SportGreen,
    onTertiary = TextInverse,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = CharcoalCard,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalCardElevated,
    onSurfaceVariant = TextMuted,
    outline = BorderSubtle,
    outlineVariant = BorderActive,
    error = CoralWarning,
    onError = Color.White
)

@Composable
fun ReactionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReactionColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    ReactionTheme(content = content)
}
