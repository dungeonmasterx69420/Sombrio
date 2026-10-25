package dev.jdtech.jellyfin.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import dev.jdtech.jellyfin.BuildConfig
import dev.jdtech.jellyfin.R
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.presentation.components.SombraMark
import dev.jdtech.jellyfin.presentation.components.SystemSButton
import dev.jdtech.jellyfin.presentation.components.SystemSDefaults
import dev.jdtech.jellyfin.presentation.components.systemSMenuBar
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.presentation.theme.systemS
import dev.jdtech.jellyfin.settings.R as SettingsR
import dev.jdtech.jellyfin.updates.AppUpdate
import dev.jdtech.jellyfin.updates.AppUpdateDialog
import dev.jdtech.jellyfin.updates.AppUpdater
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AboutScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val safePaddingStart =
        with(density) { WindowInsets.safeDrawing.getLeft(this, layoutDirection).toDp() }
    val safePaddingEnd =
        with(density) { WindowInsets.safeDrawing.getRight(this, layoutDirection).toDp() }

    val paddingStart = safePaddingStart
    val paddingEnd = safePaddingEnd

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val libraries by produceLibraries(R.raw.aboutlibraries)

    val scope = rememberCoroutineScope()
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    update?.let { AppUpdateDialog(update = it, onDismiss = { update = null }) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = SystemSDefaults.topAppBarColors(),
                modifier = Modifier.systemSMenuBar(),
                title = { Text(text = stringResource(SettingsR.string.about)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_arrow_left),
                            contentDescription = null,
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = paddingStart + innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = paddingEnd + innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
            header = {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(
                            modifier =
                                Modifier.padding(horizontal = MaterialTheme.spacings.default),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(Modifier.height(MaterialTheme.spacings.medium))
                            SombraMark(height = 72.dp)
                            Spacer(Modifier.height(MaterialTheme.spacings.medium))
                            Text(
                                text = stringResource(CoreR.string.app_name),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(
                                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.systemS.palette.muted,
                            )
                            Spacer(Modifier.height(MaterialTheme.spacings.small))
                            Text(
                                text = stringResource(CoreR.string.app_description),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(MaterialTheme.spacings.small))
                            Text(
                                text = stringResource(CoreR.string.about_built_on),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.systemS.palette.muted,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(MaterialTheme.spacings.medium))
                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(MaterialTheme.spacings.small)
                            ) {
                                listOf(
                                        CoreR.string.about_source_code to SOMBRIO_SOURCE,
                                        CoreR.string.about_findroid to FINDROID_SOURCE,
                                        CoreR.string.about_sombramp to SOMBRAMP_SOURCE,
                                    )
                                    .forEach { (label, uri) ->
                                        SystemSButton(
                                            onClick = {
                                                try {
                                                    uriHandler.openUri(uri)
                                                } catch (e: IllegalArgumentException) {
                                                    Toast.makeText(
                                                            context,
                                                            e.localizedMessage,
                                                            Toast.LENGTH_SHORT,
                                                        )
                                                        .show()
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 12.dp),
                                        ) {
                                            Text(text = stringResource(label))
                                        }
                                    }
                            }
                            if (AppUpdater.isSupported) {
                                Spacer(Modifier.height(MaterialTheme.spacings.small))
                                SystemSButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                update = AppUpdater.findUpdate()
                                                if (update == null) {
                                                    Toast.makeText(
                                                            context,
                                                            CoreR.string.app_update_up_to_date,
                                                            Toast.LENGTH_SHORT,
                                                        )
                                                        .show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                        context,
                                                        context.getString(
                                                            CoreR.string.app_update_failed,
                                                            e.localizedMessage ?: e.toString(),
                                                        ),
                                                        Toast.LENGTH_LONG,
                                                    )
                                                    .show()
                                            }
                                        }
                                    }
                                ) {
                                    Text(text = stringResource(CoreR.string.app_update_check))
                                }
                            }
                            Spacer(Modifier.height(MaterialTheme.spacings.medium))
                            HorizontalDivider(color = MaterialTheme.systemS.palette.faint)
                            Spacer(Modifier.height(MaterialTheme.spacings.small))
                        }
                    }
                }
            },
        )
    }
}

private const val SOMBRIO_SOURCE = "https://github.com/dungeonmasterx69420/Sombrio"
private const val FINDROID_SOURCE = "https://github.com/jarnedemeulemeester/findroid"
private const val SOMBRAMP_SOURCE = "https://github.com/dungeonmasterx69420/sombrAMP"

@Composable
@PreviewScreenSizes
private fun AboutScreenPreview() {
    FindroidTheme { AboutScreen(navigateBack = {}) }
}
