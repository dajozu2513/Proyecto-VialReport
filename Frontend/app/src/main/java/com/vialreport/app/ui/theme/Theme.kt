package com.vialreport.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary              = JadeDark,
    onPrimary            = SurfaceLight,
    primaryContainer     = JadeTint,
    onPrimaryContainer   = JadeDeep,

    secondary            = JadeMid,
    onSecondary          = SurfaceLight,
    secondaryContainer   = JadeTint,
    onSecondaryContainer = JadeDeep,

    tertiary             = JadeMid,
    onTertiary           = SurfaceLight,

    background           = BgLight,
    onBackground         = TextLight,

    surface              = SurfaceLight,
    onSurface            = TextLight,
    onSurfaceVariant     = TextSecondary,

    outline              = BorderLight,
    outlineVariant       = BorderLight,
)

private val DarkColorScheme = darkColorScheme(
    primary              = JadeMid,
    onPrimary            = JadeDeep,
    primaryContainer     = JadeDark,
    onPrimaryContainer   = JadeTint,

    secondary            = JadeMidDark,
    onSecondary          = JadeDeep,
    secondaryContainer   = JadeDark,
    onSecondaryContainer = JadeTint,

    tertiary             = JadeMidDark,
    onTertiary           = JadeDeep,

    background           = BgDark,
    onBackground         = TextDark,

    surface              = SurfaceDark,
    onSurface            = TextDark,
    onSurfaceVariant     = MutedDark,

    outline              = BorderDark,
    outlineVariant       = BorderDark,
)

@Composable
fun VialReportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // Desactivado: usamos siempre los tokens de diseño
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
