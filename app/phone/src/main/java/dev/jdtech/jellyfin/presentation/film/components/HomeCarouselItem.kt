package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovie
import dev.jdtech.jellyfin.film.presentation.home.HomeAction
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.models.FindroidMovie
import dev.jdtech.jellyfin.models.FindroidShow
import dev.jdtech.jellyfin.presentation.components.SystemSWindow
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.presentation.theme.systemS

/** A suggestion on Home, as a System S window: its name in the title bar, its backdrop inside. */
@Composable
fun HomeCarouselItem(item: FindroidItem, onAction: (HomeAction) -> Unit) {
    val colorStops =
        arrayOf(
            0.0f to Color.Black.copy(alpha = 0f),
            0.6f to Color.Black.copy(alpha = 0.2f),
            1f to Color.Black.copy(alpha = 0.7f),
        )

    SystemSWindow(
        title = item.name,
        modifier = Modifier.clickable { onAction(HomeAction.OnItemClick(item)) },
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.77f)) {
            AsyncImage(
                model = item.images.backdrop,
                placeholder = ColorPainter(MaterialTheme.systemS.palette.pane),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(brush = Brush.verticalGradient(colorStops = colorStops))
            }
            val genres =
                when (item) {
                    is FindroidMovie -> item.genres
                    is FindroidShow -> item.genres
                    else -> emptyList()
                }
            Text(
                text = genres.joinToString(" / "),
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                modifier =
                    Modifier.align(Alignment.BottomStart)
                        .padding(
                            horizontal = MaterialTheme.spacings.medium,
                            vertical = MaterialTheme.spacings.small,
                        ),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HomeCarouselItemPreview() {
    FindroidTheme { HomeCarouselItem(item = dummyMovie, onAction = {}) }
}
