package dev.jdtech.jellyfin.presentation.setup.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.presentation.components.SombraMark
import dev.jdtech.jellyfin.presentation.components.SombraWordmark
import dev.jdtech.jellyfin.presentation.components.SystemSButton
import dev.jdtech.jellyfin.presentation.components.SystemSWindow
import dev.jdtech.jellyfin.presentation.setup.components.RootLayout
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.systemS
import dev.jdtech.jellyfin.setup.R as SetupR
import dev.jdtech.jellyfin.setup.presentation.welcome.WelcomeAction

@Composable
fun WelcomeScreen(onContinueClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    WelcomeScreenLayout(
        onAction = { action ->
            when (action) {
                is WelcomeAction.OnContinueClick -> onContinueClick()
                is WelcomeAction.OnLearnMoreClick -> {
                    uriHandler.openUri("https://jellyfin.org/")
                }
            }
        }
    )
}

/** Laid out like System S's sign-in, as in sombrAMP: the S mark, one window, the wordmark. */
@Composable
private fun WelcomeScreenLayout(onAction: (WelcomeAction) -> Unit) {
    val palette = MaterialTheme.systemS.palette

    RootLayout(padding = PaddingValues(horizontal = 16.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier.align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 40.dp),
        ) {
            SombraMark(height = 116.dp, color = palette.ink)
            Spacer(modifier = Modifier.height(40.dp))
            SystemSWindow(
                title = stringResource(CoreR.string.app_name),
                modifier = Modifier.widthIn(max = 420.dp),
            ) {
                Column(
                    modifier =
                        Modifier.padding(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 22.dp)
                ) {
                    Text(
                        text = stringResource(SetupR.string.welcome),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(SetupR.string.welcome_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    SystemSButton(
                        onClick = { onAction(WelcomeAction.OnContinueClick) },
                        modifier = Modifier.fillMaxWidth(),
                        isDefault = true,
                    ) {
                        Text(text = stringResource(SetupR.string.welcome_btn_continue))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SystemSButton(
                        onClick = { onAction(WelcomeAction.OnLearnMoreClick) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(SetupR.string.welcome_btn_learn_more))
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            SombraWordmark(width = 120.dp, color = palette.ink)
        }
    }
}

@PreviewScreenSizes
@Composable
private fun WelcomeScreenLayoutPreview() {
    FindroidTheme { WelcomeScreenLayout(onAction = {}) }
}
