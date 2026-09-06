package com.example.csvanalyzer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldPremium,
    secondary = GoldPremium,
    tertiary = GoldPremium,
    background = BgDark,
    surface = SurfaceDark,
    onPrimary = BgDark,
    onSecondary = BgDark,
    onTertiary = BgDark,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun CSVAnalyzerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}