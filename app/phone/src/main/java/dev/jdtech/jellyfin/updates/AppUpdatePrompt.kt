package dev.jdtech.jellyfin.updates

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.presentation.components.systemSDialogFrame
import dev.jdtech.jellyfin.presentation.theme.systemS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/** Whether this launch of the app has looked for an update yet. */
private var checkedThisLaunch = false

/**
 * Offers a newer build of Sombrio, once the app has settled after starting, as sombrAMP does. Not
 * while offline.
 */
@Composable
fun AppUpdatePrompt(isOfflineMode: Boolean) {
    var update by remember { mutableStateOf<AppUpdate?>(null) }

    LaunchedEffect(isOfflineMode) {
        if (!AppUpdater.isSupported || isOfflineMode || checkedThisLaunch) return@LaunchedEffect
        delay(10_000)
        checkedThisLaunch = true
        update =
            try {
                AppUpdater.findUpdate()
            } catch (e: Exception) {
                Timber.w(e, "Couldn't check for updates")
                null
            }
    }

    update?.let { AppUpdateDialog(update = it, onDismiss = { update = null }) }
}

/** Offers [update], and downloads and opens it in Android's installer if accepted. */
@Composable
fun AppUpdateDialog(update: AppUpdate, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<Float?>(null) }
    val systemS = MaterialTheme.systemS

    AlertDialog(
        modifier = Modifier.systemSDialogFrame(),
        onDismissRequest = { if (!downloading) onDismiss() },
        title = { Text(text = stringResource(CoreR.string.app_update_title, update.buildNumber)) },
        text = {
            if (downloading) {
                Column {
                    Text(text = stringResource(CoreR.string.app_update_downloading))
                    Spacer(Modifier.height(12.dp))
                    // Downloading is live, so it glows
                    val currentProgress = progress
                    if (currentProgress == null) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = systemS.phosphor,
                            trackColor = systemS.palette.inset,
                            strokeCap = StrokeCap.Butt,
                            gapSize = 0.dp,
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { currentProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = systemS.phosphor,
                            trackColor = systemS.palette.inset,
                            strokeCap = StrokeCap.Butt,
                            gapSize = 0.dp,
                            drawStopIndicator = {},
                        )
                    }
                }
            } else {
                Text(
                    text =
                        update.notes.ifEmpty { stringResource(CoreR.string.app_update_no_notes) },
                    modifier =
                        Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !downloading,
                onClick = {
                    downloading = true
                    scope.launch {
                        try {
                            AppUpdater.install(context, update) { progress = it }
                            onDismiss()
                        } catch (e: Exception) {
                            Timber.e(e, "Couldn't install build ${update.buildNumber}")
                            Toast.makeText(
                                    context,
                                    context.getString(
                                        CoreR.string.app_update_failed,
                                        e.localizedMessage ?: e.toString(),
                                    ),
                                    Toast.LENGTH_LONG,
                                )
                                .show()
                            downloading = false
                        }
                    }
                },
            ) {
                Text(text = stringResource(CoreR.string.app_update_install))
            }
        },
        dismissButton = {
            TextButton(enabled = !downloading, onClick = onDismiss) {
                Text(text = stringResource(CoreR.string.app_update_later))
            }
        },
    )
}
