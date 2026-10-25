package dev.jdtech.jellyfin.presentation.setup.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.presentation.components.SystemSButton
import dev.jdtech.jellyfin.presentation.components.SystemSSpinner
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme

@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // The default button of the step, with System S's ring around it
    SystemSButton(onClick = onClick, enabled = !isLoading, isDefault = true, modifier = modifier) {
        if (isLoading) {
            SystemSSpinner(size = 16.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text)
    }
}

@Composable
@Preview
private fun LoadingButtonPreview() {
    FindroidTheme { LoadingButton(text = "Connect", onClick = {}, isLoading = true) }
}
