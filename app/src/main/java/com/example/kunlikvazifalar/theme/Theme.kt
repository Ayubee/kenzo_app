package com.example.kunlikvazifalar.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WarmDarkTerracottaAccent,
    onPrimary = Color(0xFF1E1B18),
    primaryContainer = WarmDarkTerracottaContainer,
    onPrimaryContainer = WarmDarkOnTerracottaContainer,
    secondary = WarmDarkTerracottaAccent,
    onSecondary = Color(0xFF1E1B18),
    secondaryContainer = Color(0xFF38322C),
    onSecondaryContainer = Color(0xFFEDE4D8),
    tertiaryContainer = Color(0xFF2C382E),
    onTertiaryContainer = CalmGreenCheckDark,
    background = WarmDarkBg,
    surface = WarmDarkSurface,
    surfaceDim = WarmDarkBg,
    surfaceBright = Color(0xFF3D3630),
    surfaceContainerLowest = WarmDarkBg,
    surfaceContainerLow = WarmDarkSurface,
    surfaceContainer = WarmDarkSurface,
    surfaceContainerHigh = Color(0xFF352F29),
    surfaceContainerHighest = Color(0xFF3D3630),
    surfaceTint = WarmDarkTerracottaAccent,
    onBackground = WarmDarkText,
    onSurface = WarmDarkText,
    surfaceVariant = Color(0xFF352F29),
    onSurfaceVariant = WarmDarkMutedText,
    outline = Color(0xFF6E6358),
    outlineVariant = WarmDarkBorder,
    error = Color(0xFFFF8B91),
    onError = Color(0xFF4D2224),
    errorContainer = Color(0xFF502224),
    onErrorContainer = Color(0xFFFF8B91)
)

private val LightColorScheme = lightColorScheme(
    primary = WarmTerracottaAccentLight,
    onPrimary = Color.White,
    primaryContainer = WarmTerracottaContainerLight,
    onPrimaryContainer = WarmOnTerracottaContainerLight,
    secondary = WarmTerracottaAccentLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE4D8),
    onSecondaryContainer = WarmInkTextLight,
    tertiaryContainer = Color(0xFFE4EDE5),
    onTertiaryContainer = CalmGreenCheckLight,
    background = WarmBeigeBgLight,
    surface = WarmCreamSurfaceLight,
    surfaceDim = Color(0xFFEADFD3),
    surfaceBright = WarmCreamSurfaceLight,
    surfaceContainerLowest = WarmCreamSurfaceLight,
    surfaceContainerLow = WarmCreamSurfaceLight,
    surfaceContainer = WarmBeigeBgLight,
    surfaceContainerHigh = WarmCreamSurfaceLight,
    surfaceContainerHighest = Color(0xFFEFE6DB),
    surfaceTint = WarmTerracottaAccentLight,
    onBackground = WarmInkTextLight,
    onSurface = WarmInkTextLight,
    surfaceVariant = Color(0xFFEFE6DB),
    onSurfaceVariant = WarmMutedTextLight,
    outline = Color(0xFFC8BCB0),
    outlineVariant = WarmBorderLight,
    error = PriorityHighTextLight,
    onError = Color.White,
    errorContainer = PriorityHighBgLight,
    onErrorContainer = PriorityHighTextLight
)

@Composable
fun KunlikVazifalarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
