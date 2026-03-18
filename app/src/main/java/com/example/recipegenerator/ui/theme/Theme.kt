package com.example.recipegenerator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.ui.viewmodel.AppSettingsViewModel
import com.example.recipegenerator.ui.viewmodel.ThemeViewModel

private val LightColorScheme = lightColorScheme(
    background = AppBackground,
    surface = SurfaceWhite,
    surfaceVariant = CardBackground,
    primary = Brown50,
    onPrimary = SurfaceWhite,
    primaryContainer = ChipBackground,
    onPrimaryContainer = Brown80,
    secondary = Brown30,
    onSecondary = SurfaceWhite,
    secondaryContainer = CardBackground,
    onSecondaryContainer = Brown80,
    tertiary = Lime80,
    onTertiary = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = Color(0xFF8D6E63),
    outline = ChipBackground,
)

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    primary = DarkPrimary,
    onPrimary = DarkBackground,
    primaryContainer = Color(0xFF3A2810),
    onPrimaryContainer = Color(0xFFF0D8B8),
    secondary = Color(0xFFD4B896),
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnBackground,
    tertiary = Color(0xFF8FA85A),
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF1E2410),
    onTertiaryContainer = Color(0xFFD4E8A0),
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF2A2A2A),
    error = Color(0xFFFF6B6B),
    onError = DarkBackground
)

// ─── Font Scale ───────────────────────────────────────────────────────────────
val LocalFontScale = compositionLocalOf { 1f }

@Composable
fun scaledSp(size: Float): TextUnit {
    val scale = LocalFontScale.current
    return (size * scale).sp
}

// ─── Theme ────────────────────────────────────────────────────────────────────
@Composable
fun RecipeGeneratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeViewModel: ThemeViewModel? = null,
    appSettingsViewModel: AppSettingsViewModel? = null,
    content: @Composable () -> Unit
) {
    // Dark mode — ViewModel state wins over parameter
    val isDark = if (themeViewModel != null) {
        val vmDark by themeViewModel.isDarkMode.collectAsState()
        vmDark
    } else {
        darkTheme
    }

    // Font scale — read from ViewModel, default to Medium (1f)
    val fontScale: Float = if (appSettingsViewModel != null) {
        val settings by appSettingsViewModel.settings.collectAsState()
        when (settings.fontSize) {
            "Small" -> 0.85f
            "Large" -> 1.15f
            else -> 1f
        }
    } else {
        1f
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalFontScale provides fontScale) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}