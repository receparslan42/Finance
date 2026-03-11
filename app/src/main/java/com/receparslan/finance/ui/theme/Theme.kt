package com.receparslan.finance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MidnightBlue,
    secondary = BrightBlue,
    tertiary = MediumGray,
    surfaceTint = VividRed,

    onPrimary = VividBlue,
    onTertiary = LightGray,
    primaryContainer = VividGreen,


    background = VeryDarkBlue,
    surface = Snow
)

private val LightColorScheme = lightColorScheme(
    primary = MidnightBlue,
    secondary = BrightBlue,
    tertiary = MediumGray,
    surfaceTint = VividRed,

    onPrimary = VividBlue,
    onTertiary = LightGray,
    primaryContainer = VividGreen,


    background = VeryDarkBlue,
    surface = Snow
)

@Composable
fun FinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}