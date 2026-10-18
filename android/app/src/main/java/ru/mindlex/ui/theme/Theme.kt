package ru.mindlex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Violet,
    onPrimary = OnNight,
    primaryContainer = Violet.copy(alpha = 0.22f),
    onPrimaryContainer = VioletLight,
    secondary = Mint,
    onSecondary = NightBackground,
    secondaryContainer = Mint.copy(alpha = 0.18f),
    onSecondaryContainer = Mint,
    tertiary = Amber,
    onTertiary = NightBackground,
    background = NightBackground,
    onBackground = OnNight,
    surface = NightSurface,
    onSurface = OnNight,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = OnNightMuted,
    outline = NightOutline,
    outlineVariant = NightOutline.copy(alpha = 0.6f),
    error = Coral,
    onError = NightBackground,
)

private val LightColors = lightColorScheme(
    primary = Violet,
    onPrimary = DaySurface,
    primaryContainer = Violet.copy(alpha = 0.14f),
    onPrimaryContainer = Violet,
    secondary = DeepMint,
    onSecondary = DaySurface,
    secondaryContainer = Mint.copy(alpha = 0.20f),
    onSecondaryContainer = OnDay,
    tertiary = Amber,
    onTertiary = OnDay,
    background = DayBackground,
    onBackground = OnDay,
    surface = DaySurface,
    onSurface = OnDay,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = OnDayMuted,
    outline = DayOutline,
    error = Coral,
    onError = DaySurface,
)

@Composable
fun MindLexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MindLexTypography,
        content = content,
    )
}
