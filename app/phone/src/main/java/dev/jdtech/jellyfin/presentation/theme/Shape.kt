package dev.jdtech.jellyfin.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

/** System S has hard edges: windows, covers, menus and fields are square. */
val shapes =
    Shapes(
        extraSmall = RectangleShape,
        small = RectangleShape,
        medium = RectangleShape,
        large = RectangleShape,
        extraLarge = RectangleShape,
    )

/** Buttons are the one rounded thing, as on sombra.wtf. */
val SystemSButtonShape = RoundedCornerShape(10.dp)

/** Chips and segmented buttons. */
val SystemSChipShape = RoundedCornerShape(8.dp)
