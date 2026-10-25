package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.core.R
import dev.jdtech.jellyfin.core.presentation.dummy.dummyEpisode
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovie
import dev.jdtech.jellyfin.models.FindroidEpisode
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.models.FindroidSeason
import dev.jdtech.jellyfin.models.isDownloaded
import dev.jdtech.jellyfin.presentation.components.systemSFrame
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.presentation.theme.systemS

@Composable
fun ItemCard(
    item: FindroidItem,
    direction: Direction,
    onClick: (FindroidItem) -> Unit,
    modifier: Modifier = Modifier,
    showSeriesName: Boolean = false,
) {
    val width =
        when (direction) {
            Direction.HORIZONTAL -> 260
            Direction.VERTICAL -> 150
        }
    // System S: the artwork keeps its colours, framed square on a pane with a 1dp ink edge, and the
    // name under it is drawn in the phosphor.
    Column(modifier = modifier.width(width.dp).clickable(onClick = { onClick(item) })) {
        Box(modifier = Modifier.systemSFrame().padding(1.dp)) {
            Box {
                ItemPoster(item = item, direction = direction)
                Row(
                    modifier =
                        Modifier.align(Alignment.TopEnd).padding(MaterialTheme.spacings.small),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.small),
                ) {
                    if (item.isDownloaded()) DownloadedBadge()
                    if (item.played) PlayedBadge()
                    item.unplayedItemCount?.takeIf { it > 0 }?.let { ItemCountBadge(it) }
                }
                if (direction == Direction.HORIZONTAL) {
                    ProgressBar(
                        item = item,
                        width = width,
                        modifier =
                            Modifier.align(Alignment.BottomStart)
                                .padding(MaterialTheme.spacings.small),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val title =
            when (item) {
                is FindroidEpisode -> item.seriesName
                is FindroidSeason -> if (showSeriesName) item.seriesName else item.name
                else -> item.name
            }
        val subtitle =
            when (item) {
                is FindroidEpisode ->
                    stringResource(
                        id = R.string.episode_name_extended,
                        item.parentIndexNumber,
                        item.indexNumber,
                        item.name,
                    )
                is FindroidSeason -> if (showSeriesName) item.name else null
                else -> null
            }
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.systemS.phosphorInk,
            maxLines = if (subtitle == null) 2 else 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.systemS.palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPreviewMovie() {
    FindroidTheme { ItemCard(item = dummyMovie, direction = Direction.HORIZONTAL, onClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPreviewMovieVertical() {
    FindroidTheme { ItemCard(item = dummyMovie, direction = Direction.VERTICAL, onClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPreviewEpisode() {
    FindroidTheme { ItemCard(item = dummyEpisode, direction = Direction.HORIZONTAL, onClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPreviewEpisodeVertical() {
    FindroidTheme { ItemCard(item = dummyEpisode, direction = Direction.VERTICAL, onClick = {}) }
}
