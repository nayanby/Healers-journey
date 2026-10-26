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
import androidx.compose.ui.unit.dp
import com.healersjourney.app.R

val PixelFont = FontFamily(Font(R.font.pixel_silkscreen))

val Averia = FontFamily(Font(R.font.averia_serif_libre))

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFE6BF76), onPrimary = Color(0xFF2D2C29),
    primaryContainer = Color(0xFF304953), onPrimaryContainer = Color(0xFFEAF0F8),
    secondary = Color(0xFF91BDB0), onSecondary = Color(0xFF10213B),
    secondaryContainer = Color(0xFF3B5C62), onSecondaryContainer = Color(0xFFDCE8FF),
    tertiary = Color(0xFF9CCBB2), onTertiary = Color(0xFF153124),
    background = Color(0xFF132332), onBackground = Color(0xFFEAF0F8),
    surface = Color(0xFF223949), onSurface = Color(0xFFEAF0F8),
    surfaceVariant = Color(0xFF304953), onSurfaceVariant = Color(0xFF9EADC2),
    outline = Color(0xFF777777), outlineVariant = Color(0xFF506568),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF426B5C), onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDBD), onPrimaryContainer = Color(0xFF2E3B3B),
    secondary = Color(0xFF4C7084), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0D6B9), onSecondaryContainer = Color(0xFF254B80),
    tertiary = Color(0xFF367356), onTertiary = Color.White,
    background = Color(0xFFF1E7CE), onBackground = Color(0xFF2E3B3B),
    surface = Color(0xFFFFF5DA), onSurface = Color(0xFF2E3B3B),
    surfaceVariant = Color(0xFFE9DDBD), onSurfaceVariant = Color(0xFF696F62),
    outline = Color(0xFF8991A7), outlineVariant = Color(0xFFB8AD8E),
    error = Color(0xFFB3261E), onError = Color.White
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = PixelFont, fontSize = 40.sp, lineHeight = 48.sp),
    displayMedium = TextStyle(fontFamily = PixelFont, fontSize = 34.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = PixelFont, fontSize = 28.sp, lineHeight = 36.sp),
    displaySmall = TextStyle(
        fontFamily = PixelFont, fontWeight = FontWeight.Normal,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PixelFont, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PixelFont, fontWeight = FontWeight.Normal,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PixelFont, fontWeight = FontWeight.Normal,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(
        fontFamily = PixelFont, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PixelFont, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp)
)

@Composable
fun HealersJourneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        shapes = androidx.compose.material3.Shapes(
            small = androidx.compose.foundation.shape.CutCornerShape(3.dp),
            medium = androidx.compose.foundation.shape.CutCornerShape(5.dp),
            large = androidx.compose.foundation.shape.CutCornerShape(6.dp),
            extraLarge = androidx.compose.foundation.shape.CutCornerShape(8.dp)),
        content = content
    )
}
