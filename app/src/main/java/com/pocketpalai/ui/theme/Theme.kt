package com.pocketpalai.ui.theme

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

// PocketPal AI Pure OLED Black Theme Colors
val PocketPalBlackBg = Color(0xFF000000)
val PocketPalSurfaceDark = Color(0xFF121212)
val PocketPalCardDark = Color(0xFF18181A)
val PocketPalInputDark = Color(0xFF1C1C1E)
val PocketPalGreenPrimary = Color(0xFF1D4D32)
val PocketPalGreenLight = Color(0xFF22C55E)
val PocketPalGreenAccent = Color(0xFF1A5A38)
val PocketPalBorderDark = Color(0xFF27272A)
val PocketPalTextWhite = Color(0xFFFFFFFF)
val PocketPalTextMuted = Color(0xFFA1A1AA)

private val DarkColorScheme = darkColorScheme(
    primary = PocketPalGreenLight,
    onPrimary = Color.White,
    primaryContainer = PocketPalGreenPrimary,
    onPrimaryContainer = Color.White,
    secondary = PocketPalGreenAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF93C5FD),
    tertiary = Color(0xFF10B981),
    onTertiary = Color.White,
    background = PocketPalBlackBg,
    onBackground = PocketPalTextWhite,
    surface = PocketPalSurfaceDark,
    onSurface = PocketPalTextWhite,
    surfaceVariant = PocketPalCardDark,
    onSurfaceVariant = PocketPalTextMuted,
    outline = PocketPalBorderDark,
    outlineVariant = Color(0xFF202022)
)

private val LightColorScheme = DarkColorScheme // Default to OLED Dark mode as requested by design spec

@Composable
fun PocketPalTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}


