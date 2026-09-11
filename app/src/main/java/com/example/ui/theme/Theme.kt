package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReactionColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = TextInverse,
    primaryContainer = Color(0xFF22380E),
    onPrimaryContainer = ElectricLime,
    secondary = CoolBlue,
    onSecondary = Color(0xFF002244),
    secondaryContainer = Color(0xFF132B45),
    onSecondaryContainer = CoolBlue,
    tertiary = CoralWarning,
    onTertiary = Color.White,
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
