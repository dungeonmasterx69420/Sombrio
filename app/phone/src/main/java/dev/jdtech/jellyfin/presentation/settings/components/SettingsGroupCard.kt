package dev.jdtech.jellyfin.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.fastForEachIndexed
import dev.jdtech.jellyfin.presentation.components.SystemSWindow
import dev.jdtech.jellyfin.presentation.components.systemSFrame
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.systemS
import dev.jdtech.jellyfin.settings.R as SettingsR
import dev.jdtech.jellyfin.settings.domain.models.Preference
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceAppLanguage
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceCategory
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceFileEdit
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceGroup
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceIntInput
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceLongInput
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceMultiSelect
import dev.jdtech.jellyfin.settings.presentation.models.PreferencePermission
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceSelect
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceSwitch
import dev.jdtech.jellyfin.settings.presentation.settings.SettingsAction

@Composable
fun SettingsGroupCard(
    group: PreferenceGroup,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // System S: each group is a window, titled with the group's name
    val title = group.nameStringResource?.let { stringResource(it) }
    if (title != null) {
        SystemSWindow(title = title, modifier = modifier) { SettingsGroupContent(group, onAction) }
    } else {
        Column(modifier = modifier.systemSFrame()) { SettingsGroupContent(group, onAction) }
    }
}

@Composable
private fun SettingsGroupContent(group: PreferenceGroup, onAction: (SettingsAction) -> Unit) {
    group.preferences.fastForEachIndexed { index, preference ->
        when (preference) {
            is PreferenceCategory ->
                SettingsCategoryCard(
                    preference = preference,
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferenceSwitch ->
                SettingsSwitchCard(
                    preference = preference,
                    onClick = {
                        onAction(
                            SettingsAction.OnUpdate(preference.copy(value = !preference.value))
                        )
                        preference.onClick(preference)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferenceSelect ->
                SettingsSelectCard(
                    preference = preference,
                    onUpdate = { value ->
                        onAction(SettingsAction.OnUpdate(preference.copy(value = value)))
                        preference.onUpdate(value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferenceMultiSelect ->
                SettingsMultiSelectCard(
                    preference = preference,
                    onUpdate = { value ->
                        onAction(SettingsAction.OnUpdate(preference.copy(value = value)))
                        preference.onUpdate(value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferenceIntInput ->
                SettingsIntInputCard(
                    preference = preference,
                    onUpdate = { value ->
                        onAction(SettingsAction.OnUpdate(preference.copy(value = value)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferenceLongInput ->
                SettingsLongInputCard(
                    preference = preference,
                    onUpdate = { value ->
                        onAction(SettingsAction.OnUpdate(preference.copy(value = value)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferenceAppLanguage ->
                SettingsAppLanguageCard(
                    preference = preference,
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferencePermission ->
                SettingsPermissionCard(
                    preference = preference,
                    modifier = Modifier.fillMaxWidth(),
                )
            is PreferenceFileEdit ->
                SettingsFileEditCard(
                    preference = preference,
                    modifier = Modifier.fillMaxWidth(),
                )
        }
        if (index < group.preferences.lastIndex) {
            HorizontalDivider(color = MaterialTheme.systemS.palette.faint)
        }
    }
}

@Preview
@Composable
private fun SettingsGroupCardPreview() {
    FindroidTheme {
        SettingsGroupCard(
            group =
                PreferenceGroup(
                    nameStringResource = SettingsR.string.mpv_player,
                    preferences =
                        listOf(
                            PreferenceSwitch(
                                nameStringResource = SettingsR.string.mpv_player,
                                descriptionStringRes = SettingsR.string.mpv_player_summary,
                                backendPreference = Preference("", false),
                            ),
                            PreferenceSelect(
                                nameStringResource = SettingsR.string.pref_player_mpv_hwdec,
                                dependencies = listOf(Preference("", false)),
                                backendPreference = Preference("", ""),
                                options = SettingsR.array.mpv_hwdec,
                                optionValues = SettingsR.array.mpv_hwdec,
                            ),
                            PreferenceSelect(
                                nameStringResource = SettingsR.string.pref_player_mpv_vo,
                                dependencies = listOf(Preference("", false)),
                                backendPreference = Preference("", ""),
                                options = SettingsR.array.mpv_vos,
                                optionValues = SettingsR.array.mpv_vos,
                            ),
                            PreferenceSelect(
                                nameStringResource = SettingsR.string.pref_player_mpv_ao,
                                dependencies = listOf(Preference("", false)),
                                backendPreference = Preference("", ""),
                                options = SettingsR.array.mpv_aos,
                                optionValues = SettingsR.array.mpv_aos,
                            ),
                        ),
                ),
            onAction = {},
        )
    }
}
