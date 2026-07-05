package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val BentoLightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    secondary = BentoSecondary,
    tertiary = BentoOnPrimaryContainer,
    background = BentoBackground,
    surface = BentoSurface,
    surfaceVariant = BentoSurfaceVariant,
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary,
    outline = BentoBorder,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer
)

private val BentoDarkColorScheme = darkColorScheme(
    primary = TwilightPrimary,
    secondary = TwilightSecondary,
    tertiary = TwilightTertiary,
    background = TwilightBackground,
    surface = TwilightSurface,
    surfaceVariant = TwilightSurfaceVariant,
    onBackground = TwilightOnBackground,
    onSurface = TwilightOnSurface,
    outline = TwilightSurfaceVariant,
    primaryContainer = TwilightSurfaceVariant,
    onPrimaryContainer = TwilightPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Set to false to default to our gorgeous bright Bento theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) BentoDarkColorScheme else BentoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


