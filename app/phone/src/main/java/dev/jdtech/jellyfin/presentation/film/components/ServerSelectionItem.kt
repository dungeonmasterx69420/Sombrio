package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.core.presentation.dummy.dummyServer
import dev.jdtech.jellyfin.core.presentation.dummy.dummyServerAddress
import dev.jdtech.jellyfin.models.ServerWithAddresses
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.presentation.theme.systemS
import java.util.UUID

@Composable
fun ServerSelectionItem(
    server: ServerWithAddresses,
    selected: Boolean,
    onClick: () -> Unit,
    onClickAddress: (addressId: UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        enabled = server.server.currentUserId != null,
        border = BorderStroke(1.dp, MaterialTheme.systemS.palette.ink),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacings.medium)) {
            Row(modifier = Modifier.height(48.dp)) {
                Box(
                    modifier =
                        Modifier.size(48.dp)
                            .background(
                                if (selected) MaterialTheme.systemS.palette.ink
                                else Color.Transparent
                            )
                ) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_server),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center),
                        tint =
                            if (selected) MaterialTheme.systemS.palette.pane
                            else LocalContentColor.current,
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacings.medium))
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = server.server.name, style = MaterialTheme.typography.titleMedium)
                    if (!selected || server.addresses.count() < 2) {
                        server.addresses
                            .firstOrNull { it.id == server.server.currentServerAddressId }
                            ?.let { address ->
                                Spacer(
                                    modifier = Modifier.height(MaterialTheme.spacings.extraSmall)
                                )
                                Text(
                                    text = address.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                    }
                }
            }
            if (selected && server.addresses.count() > 1) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacings.small))
                server.addresses.forEach { address ->
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable(onClick = { onClickAddress(address.id) })
                    ) {
                        Row(
                            modifier = Modifier.padding(all = MaterialTheme.spacings.medium),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(8.dp)
                                        .background(
                                            if (address.id == server.server.currentServerAddressId)
                                                MaterialTheme.systemS.phosphor
                                            else Color.Transparent
                                        )
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacings.medium))
                            Text(
                                text = address.address,
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun ServerSelectionItemPreview() {
    FindroidTheme {
        ServerSelectionItem(
            server =
                ServerWithAddresses(
                    server = dummyServer,
                    addresses = listOf(dummyServerAddress),
                    user = null,
                ),
            selected = false,
            onClick = {},
            onClickAddress = {},
        )
    }
}

@Composable
@Preview
private fun ServerSelectionItemSelectedPreview() {
    FindroidTheme {
        ServerSelectionItem(
            server =
                ServerWithAddresses(
                    server = dummyServer,
                    addresses = listOf(dummyServerAddress, dummyServerAddress),
                    user = null,
                ),
            selected = true,
            onClick = {},
            onClickAddress = {},
        )
    }
}
