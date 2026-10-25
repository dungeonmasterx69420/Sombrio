package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.presentation.components.SombraMark
import dev.jdtech.jellyfin.presentation.components.systemSShadow
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.systemS
import dev.jdtech.jellyfin.presentation.utils.LocalOfflineMode

/**
 * Home's menu bar, as on sombra.wtf: the S mark and the server, then the menu items, on a pane with
 * an ink edge and a hard shadow.
 */
@Composable
fun HomeHeader(
    serverName: String,
    isLoading: Boolean,
    isError: Boolean,
    onServerClick: () -> Unit,
    onErrorClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOfflineMode = LocalOfflineMode.current
    val systemS = MaterialTheme.systemS
    val palette = systemS.palette

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .systemSShadow(palette.ink, 3.dp, stepped = false)
                .background(palette.pane)
                .border(1.dp, palette.ink),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier.weight(1f)
                    .fillMaxHeight()
                    .clickable(role = Role.Button, onClick = onServerClick)
                    .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SombraMark(height = 26.dp, color = palette.ink)
            Text(
                text = serverName,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.animateContentSize(),
            )
        }

        AnimatedVisibility(visible = isError, enter = fadeIn(), exit = fadeOut()) {
            MenuBarItem(onClick = onErrorClick) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_alert_circle),
                    contentDescription = null,
                )
            }
        }

        AnimatedVisibility(visible = isLoading || isError, enter = fadeIn(), exit = fadeOut()) {
            MenuBarItem(onClick = onRetryClick, enabled = !isLoading) {
                when {
                    isError -> {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_rotate_ccw),
                            contentDescription = null,
                        )
                    }
                    isLoading -> {
                        // Loading is live, so it glows
                        CircularProgressIndicator(
                            color = systemS.phosphor,
                            trackColor = palette.inset,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        if (!isOfflineMode) {
            MenuBarItem(onClick = onSearchClick) {
                Icon(painter = painterResource(CoreR.drawable.ic_search), contentDescription = null)
            }
        }

        MenuBarItem(onClick = onUserClick) {
            Icon(painter = painterResource(CoreR.drawable.ic_user), contentDescription = null)
        }
    }
}

/** One item of the menu bar: a square cell after a 1dp ink rule. */
@Composable
private fun MenuBarItem(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxHeight()) {
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.systemS.palette.ink)
        Box(
            modifier =
                Modifier.fillMaxHeight()
                    .width(48.dp)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HomeHeaderLoadingPreview() {
    FindroidTheme {
        HomeHeader(
            serverName = "Jellyfin",
            isLoading = true,
            isError = false,
            onServerClick = {},
            onErrorClick = {},
            onRetryClick = {},
            onSearchClick = {},
            onUserClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun HomeHeaderErrorPreview() {
    FindroidTheme {
        HomeHeader(
            serverName = "Jellyfin",
            isLoading = false,
            isError = true,
            onServerClick = {},
            onErrorClick = {},
            onRetryClick = {},
            onSearchClick = {},
            onUserClick = {},
        )
    }
}
