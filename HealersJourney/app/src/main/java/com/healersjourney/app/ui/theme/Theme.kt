package com.healersjourney.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.healersjourney.app.R

val Averia = FontFamily(Font(R.font.averia_serif_libre))

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFBDB0FF), onPrimary = Color(0xFF241C4C),
    primaryContainer = Color(0xFF28364B), onPrimaryContainer = Color(0xFFEAF0F8),
    secondary = Color(0xFF78A9F8), onSecondary = Color(0xFF10213B),
    secondaryContainer = Color(0xFF1B3E70), onSecondaryContainer = Color(0xFFDCE8FF),
    tertiary = Color(0xFF9CCBB2), onTertiary = Color(0xFF153124),
    background = Color(0xFF111926), onBackground = Color(0xFFEAF0F8),
    surface = Color(0xFF1B2738), onSurface = Color(0xFFEAF0F8),
    surfaceVariant = Color(0xFF28364B), onSurfaceVariant = Color(0xFF9EADC2),
    outline = Color(0xFF777777), outlineVariant = Color(0xFF304058),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF6753BB), onPrimary = Color.White,
    primaryContainer = Color(0xFFF0EDF9), onPrimaryContainer = Color(0xFF292B40),
    secondary = Color(0xFF3B82F6), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EFF8), onSecondaryContainer = Color(0xFF254B80),
    tertiary = Color(0xFF367356), onTertiary = Color.White,
    background = Color(0xFFFAF8F3), onBackground = Color(0xFF292B40),
    surface = Color.White, onSurface = Color(0xFF292B40),
    surfaceVariant = Color(0xFFF0EDF9), onSurfaceVariant = Color(0xFF757A8E),
    outline = Color(0xFF8991A7), outlineVariant = Color(0xFFE6E3EE),
    error = Color(0xFFB3261E), onError = Color.White
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp)
)

@Composable
fun HealersJourneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        content = content
    )
}
