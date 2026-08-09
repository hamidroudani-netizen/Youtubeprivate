package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ModernLightColorScheme = lightColorScheme(
    primary = ModernPrimary,
    onPrimary = Color.White,
    primaryContainer = ModernPrimaryContainer,
    onPrimaryContainer = ModernOnPrimaryContainer,
    secondary = ModernSecondary,
    onSecondary = Color.White,
    secondaryContainer = ModernSecondaryContainer,
    onSecondaryContainer = ModernOnSecondaryContainer,
    tertiary = ModernAccentTeal,
    onTertiary = Color.White,
    tertiaryContainer = ModernAccentAmber,
    background = ModernBackground,
    onBackground = ModernOnSurface,
    surface = ModernSurface,
    onSurface = ModernOnSurface,
    surfaceVariant = ModernSurfaceVariant,
    onSurfaceVariant = ModernOnSurfaceVariant,
    outlineVariant = ModernOutlineVariant
)

private val ModernDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFA29BFE),
    onPrimary = Color(0xFF1E105C),
    primaryContainer = Color(0xFF4A00E0),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFF8E8D),
    onSecondary = Color(0xFF5E1312),
    secondaryContainer = Color(0xFF8B2523),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF55E6E1),
    onTertiary = Color(0xFF003836),
    background = Color(0xFF12131A),
    onBackground = Color(0xFFE2E4F0),
    surface = Color(0xFF1A1C28),
    onSurface = Color(0xFFE2E4F0),
    surfaceVariant = Color(0xFF282B3D),
    onSurfaceVariant = Color(0xFFAAAEC0),
    outlineVariant = Color(0xFF3B3E54)
)

val ModernShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

@Composable
fun YouTubePriveTheme(
    appThemeSetting: String = "system",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = when (appThemeSetting) {
        "light" -> false
        "dark" -> true
        else -> darkTheme
    }

    val colorScheme = if (isDark) ModernDarkColorScheme else ModernLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = ModernShapes,
        typography = Typography,
        content = content
    )
}
