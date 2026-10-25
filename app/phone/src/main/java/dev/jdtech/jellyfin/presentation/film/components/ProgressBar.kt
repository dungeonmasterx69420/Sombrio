package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovie
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.systemS

/** How far into [item] playback is. Progress is live, so it glows in the phosphor. */
@Composable
fun ProgressBar(item: FindroidItem, width: Int, modifier: Modifier = Modifier) {
    if (item.playbackPositionTicks <= 0 || item.runtimeTicks <= 0) return
    val systemS = MaterialTheme.systemS
    val progress =
        (item.playbackPositionTicks.toFloat() / item.runtimeTicks.toFloat()).coerceIn(0f, 1f)
    Box(modifier = modifier.width((width - 16).dp).height(4.dp).background(systemS.palette.inset)) {
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(systemS.phosphor))
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressBarPreview() {
    FindroidTheme { ProgressBar(item = dummyMovie, width = 142) }
}
