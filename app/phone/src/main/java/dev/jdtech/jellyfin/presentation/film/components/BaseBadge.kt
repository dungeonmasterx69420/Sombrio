package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.presentation.theme.systemS

/**
 * A square System S tag: inverted, ink with pane content, or in the phosphor when it marks
 * something [live], like a download.
 */
@Composable
fun BaseBadge(
    modifier: Modifier = Modifier,
    live: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val systemS = MaterialTheme.systemS
    Box(
        modifier =
            modifier
                .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
                .background(if (live) systemS.phosphor else systemS.palette.ink)
    ) {
        content()
    }
}
