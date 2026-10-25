package dev.jdtech.jellyfin.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** System S has hard edges: windows, covers, menus and fields are square. */
private val square = RoundedCornerShape(0.dp)

val shapes =
    Shapes(
        extraSmall = square,
        small = square,
        medium = square,
        large = square,
        extraLarge = square,
    )

/** Buttons are the one rounded thing, as on sombra.wtf. */
val SystemSButtonShape = RoundedCornerShape(10.dp)

/** Chips and segmented buttons. */
val SystemSChipShape = RoundedCornerShape(8.dp)
