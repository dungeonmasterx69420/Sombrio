package dev.jdtech.jellyfin.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import dev.jdtech.jellyfin.core.presentation.theme.SystemSColors

/**
 * The [ColorScheme] System S maps onto Material, as in sombrAMP.
 *
 * Controls are ink on pane, so `primary` is ink. The phosphor only marks what is live (playing,
 * downloading, progress, focus) and lives in `secondary` and `tertiary`. System S has no red:
 * problems are shown inverted, ink on pane, so `error` is ink too.
 */
fun systemSColorScheme(s: SystemSColors): ColorScheme {
    val p = s.palette
    val base = if (p.isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = p.ink,
        onPrimary = p.pane,
        primaryContainer = p.hover,
        onPrimaryContainer = p.ink,
        inversePrimary = p.pane,
        secondary = s.phosphorInk,
        onSecondary = s.onPhosphor,
        secondaryContainer = p.ink,
        onSecondaryContainer = p.pane,
        tertiary = s.phosphorInk,
        onTertiary = s.onPhosphor,
        tertiaryContainer = p.hover,
        onTertiaryContainer = p.ink,
        background = p.desk,
        onBackground = p.ink,
        surface = p.desk,
        onSurface = p.ink,
        surfaceVariant = p.pane,
        onSurfaceVariant = p.muted,
        surfaceTint = Color.Transparent,
        inverseSurface = p.ink,
        inverseOnSurface = p.pane,
        error = p.ink,
        onError = p.pane,
        errorContainer = p.ink,
        onErrorContainer = p.pane,
        outline = p.ink,
        outlineVariant = p.faint,
        scrim = p.scrim.copy(alpha = 1f),
        surfaceBright = p.hover,
        surfaceContainer = p.pane,
        surfaceContainerHigh = p.pane,
        surfaceContainerHighest = p.pane,
        surfaceContainerLow = p.pane,
        surfaceContainerLowest = p.pane,
        surfaceDim = p.well,
    )
}
