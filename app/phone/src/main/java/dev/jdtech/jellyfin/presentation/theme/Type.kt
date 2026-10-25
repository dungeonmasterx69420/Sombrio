package dev.jdtech.jellyfin.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dev.jdtech.jellyfin.core.presentation.theme.SystemSFonts

/** Pixelify Sans for titles, buttons and labels; IBM Plex Mono for body text and figures. */
fun pixelStyle(size: TextUnit, weight: FontWeight = FontWeight.Normal, lineHeight: TextUnit) =
    TextStyle(
        fontFamily = SystemSFonts.pixel,
        fontFeatureSettings = SystemSFonts.PIXEL_FEATURES,
        fontSize = size,
        fontWeight = weight,
        lineHeight = lineHeight,
        letterSpacing = 0.sp,
    )

fun monoStyle(size: TextUnit, weight: FontWeight = FontWeight.Normal, lineHeight: TextUnit) =
    TextStyle(
        fontFamily = SystemSFonts.mono,
        fontSize = size,
        fontWeight = weight,
        lineHeight = lineHeight,
        letterSpacing = 0.sp,
    )

val typography =
    Typography(
        displayLarge = pixelStyle(57.sp, FontWeight.SemiBold, 64.sp),
        displayMedium = pixelStyle(45.sp, FontWeight.SemiBold, 52.sp),
        displaySmall = pixelStyle(36.sp, FontWeight.SemiBold, 44.sp),
        headlineLarge = pixelStyle(32.sp, lineHeight = 40.sp),
        headlineMedium = pixelStyle(28.sp, lineHeight = 36.sp),
        headlineSmall = pixelStyle(24.sp, lineHeight = 32.sp),
        titleLarge = pixelStyle(22.sp, lineHeight = 28.sp),
        titleMedium = pixelStyle(17.sp, FontWeight.Medium, 24.sp),
        titleSmall = pixelStyle(14.sp, FontWeight.Medium, 20.sp),
        labelLarge = pixelStyle(15.sp, lineHeight = 20.sp),
        labelMedium = pixelStyle(14.sp, lineHeight = 18.sp),
        labelSmall = pixelStyle(12.sp, lineHeight = 16.sp),
        bodyLarge = monoStyle(15.sp, lineHeight = 22.sp),
        bodyMedium = monoStyle(13.sp, lineHeight = 19.sp),
        bodySmall = monoStyle(12.sp, lineHeight = 17.sp),
    )
