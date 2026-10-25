package dev.jdtech.jellyfin.presentation.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.jdtech.jellyfin.core.presentation.theme.Spacings
import dev.jdtech.jellyfin.core.presentation.theme.SystemSColors
import dev.jdtech.jellyfin.core.presentation.theme.SystemSPhosphor
import dev.jdtech.jellyfin.presentation.components.SystemSIndication

val LocalSystemS = staticCompositionLocalOf { SystemSColors.resolve(isDark = true) }

/** System S's own colours, beyond what the Material colour scheme holds. */
val MaterialTheme.systemS: SystemSColors
    @Composable @ReadOnlyComposable get() = LocalSystemS.current

/**
 * Sombrio's look: sombra System S, as in sombrAMP (music/sombrAMP/lib/sombra/system_s.dart).
 *
 * @param darkTheme dark (System S's default), light (paper white), or null to follow the system.
 * @param phosphor the colour of what is live: progress, the played and downloaded marks, focus.
 * @param amoled true black instead of System S's near black.
 */
@Composable
fun FindroidTheme(
    darkTheme: Boolean? = true,
    phosphor: SystemSPhosphor = SystemSPhosphor.AMBER,
    amoled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = darkTheme ?: isSystemInDarkTheme()
    val systemS = SystemSColors.resolve(isDark = isDark, phosphor = phosphor, amoled = amoled)
    val palette = systemS.palette

    MaterialTheme(
        colorScheme = systemSColorScheme(systemS),
        shapes = shapes,
        typography = typography,
    ) {
        CompositionLocalProvider(
            LocalSystemS provides systemS,
            LocalContentColor provides palette.ink,
            LocalSpacings provides Spacings,
            // Pressed things tint flatly instead of rippling
            LocalIndication provides SystemSIndication,
            LocalTextSelectionColors provides
                TextSelectionColors(
                    handleColor = palette.ink,
                    backgroundColor = systemS.phosphor.copy(alpha = 0.4f),
                ),
        ) {
            content()
        }
    }
}
