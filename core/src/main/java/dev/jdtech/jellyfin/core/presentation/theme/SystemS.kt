package dev.jdtech.jellyfin.core.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.jdtech.jellyfin.core.R

/*
 * sombra System S, the design system behind sombra.wtf, as sombrAMP (music/sombrAMP/lib/sombra/system_s.dart) has it.
 *
 * A 1-bit desktop: ink on black (dark, the default) or on paper white (light), hard edges, extruded shadows taken
 * from the S mark, and one phosphor colour that marks whatever is live. Every token here is copied from System S;
 * change it there first and mirror it here.
 */

object SystemSFonts {
    /** Titles, labels, buttons and menus (`--pixel`). */
    val pixel =
        FontFamily(
            Font(R.font.pixelify_sans_regular, FontWeight.Normal),
            Font(R.font.pixelify_sans_medium, FontWeight.Medium),
            Font(R.font.pixelify_sans_semibold, FontWeight.SemiBold),
            Font(R.font.pixelify_sans_bold, FontWeight.Bold),
        )

    /**
     * Pixelify Sans's "fi" and "fl" ligatures draw as broken glyphs ("Jellyfin" reads "JellyÆn"),
     * so every Pixelify style turns them off.
     */
    const val PIXEL_FEATURES = "liga 0"

    /** Body text and figures (`--mono`). */
    val mono =
        FontFamily(
            Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
            Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
            Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
        )
}

@Immutable
data class SystemSPalette(
    val isDark: Boolean,
    /** The desktop behind every window. */
    val desk: Color,
    /** The dot grid on the desktop. */
    val deskDot: Color,
    /** Windows, menus and the menu bar. */
    val pane: Color,
    /** Text fields and address boxes. */
    val well: Color,
    /** Text, borders and the shadow of the active window. */
    val ink: Color,
    val muted: Color,
    val faint: Color,
    /** The shadow of inactive windows. */
    val idle: Color,
    /** The inner shadow of text fields. */
    val inset: Color,
    val hover: Color,
    val scrim: Color,
) {
    /** The dark palette with true-black backgrounds, for the AMOLED setting. */
    fun amoled(): SystemSPalette =
        if (!isDark) this else copy(desk = Color.Black, pane = Color.Black, well = Color.Black)

    companion object {
        val Dark =
            SystemSPalette(
                isDark = true,
                desk = Color(0xFF0A0A0A),
                deskDot = Color(0xFF242424),
                pane = Color(0xFF0E0E0E),
                well = Color(0xFF070707),
                ink = Color(0xFFEDEDED),
                muted = Color(0xFFA3A3A3),
                faint = Color(0xFF6A6A6A),
                idle = Color(0xFF4A4A4A),
                inset = Color(0xFF2A2A2A),
                hover = Color(0xFF1B1B1B),
                scrim = Color(0x8C000000),
            )

        val Light =
            SystemSPalette(
                isDark = false,
                desk = Color(0xFFCFCFCF),
                deskDot = Color(0xFF9E9E9E),
                pane = Color(0xFFFFFFFF),
                well = Color(0xFFFFFFFF),
                ink = Color(0xFF111111),
                muted = Color(0xFF555555),
                faint = Color(0xFF8A8A8A),
                idle = Color(0xFF8C8C8C),
                inset = Color(0xFFDADADA),
                hover = Color(0xFFEFEFEF),
                scrim = Color(0x8CFFFFFF),
            )
    }
}

/** The phosphor colours System S offers. Amber is the default. */
enum class SystemSPhosphor(
    /** How the setting stores it. */
    val key: String,
    /** The phosphor itself (`--phosphor`), and its ink on the dark theme. */
    val glow: Color,
    /** Its ink on the light theme (`--phosphor-ink`), dark enough to read on paper white. */
    val inkOnLight: Color,
) {
    AMBER("amber", Color(0xFFFFB000), Color(0xFF8A5A00)),
    GREEN("green", Color(0xFF7CFF6B), Color(0xFF1F7A12)),
    CYAN("cyan", Color(0xFF6BD6FF), Color(0xFF00698F)),
    PINK("pink", Color(0xFFFF7AB6), Color(0xFFB0246A));

    companion object {
        fun fromKey(key: String?): SystemSPhosphor = entries.firstOrNull { it.key == key } ?: AMBER
    }
}

/** System S's colours, resolved for one theme and phosphor. */
@Immutable
data class SystemSColors(
    val palette: SystemSPalette,
    /** The phosphor as a fill: progress, live badges, the playing item (`--phosphor`). */
    val phosphor: Color,
    /** The phosphor as text, icons and outlines on a pane (`--phosphor-ink`). */
    val phosphorInk: Color,
    /** Text on a phosphor fill (`--on-phosphor`). */
    val onPhosphor: Color,
) {
    /** The tint over what has the keyboard or remote focus: the focus is live too. */
    val focus: Color
        get() = phosphor.copy(alpha = 0.3f)

    companion object {
        fun resolve(
            isDark: Boolean,
            phosphor: SystemSPhosphor = SystemSPhosphor.AMBER,
            amoled: Boolean = false,
        ): SystemSColors {
            var palette = if (isDark) SystemSPalette.Dark else SystemSPalette.Light
            if (amoled) palette = palette.amoled()
            return SystemSColors(
                palette = palette,
                phosphor = phosphor.glow,
                phosphorInk = if (isDark) phosphor.glow else phosphor.inkOnLight,
                onPhosphor = if (isDark) Color(0xFF0A0A0A) else Color(0xFF111111),
            )
        }
    }
}
