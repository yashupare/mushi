package com.mushi.app.modules.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.audioeffects.EQ_BAND_HZ
import com.mushi.app.core.audioeffects.EQ_MAX_GAIN
import com.mushi.app.core.audioeffects.EqualizerPreset
import com.mushi.app.core.audioeffects.normalizedGains
import com.mushi.app.core.audioeffects.presetFor
import com.mushi.app.core.ui.component.AdaptiveDialogBottomSheet
import com.mushi.app.core.ui.component.MushiFilterChip
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.modules.settings.components.SwitchSettingCard
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumSlider
import org.spotube.millennium.composables.MillenniumSwitch
import kotlin.math.roundToInt

/** The equalizer and volume evening cards. Android only, where the effects exist. */
internal fun soundSettingCards(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel): List<@Composable () -> Unit> = listOf(
    { EqualizerCard(settings, settingsViewModel) },
    {
        val title = stringResource(Res.string.settings_volume_evening_title)
        SwitchSettingCard(
            title = title,
            subtitle = stringResource(Res.string.settings_volume_evening_subtitle),
            icon = { SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Waveform, title) },
            checked = settings.volumeEvening,
            onCheckedChange = { on -> settingsViewModel.updateSettings { copy(volumeEvening = on) } })
    })

@Composable
private fun presetName(preset: EqualizerPreset?): String = stringResource(
    when (preset) {
        EqualizerPreset.Flat -> Res.string.equalizer_preset_flat
        EqualizerPreset.BassBoost -> Res.string.equalizer_preset_bass
        EqualizerPreset.Vocal -> Res.string.equalizer_preset_vocal
        EqualizerPreset.Treble -> Res.string.equalizer_preset_treble
        EqualizerPreset.Lively -> Res.string.equalizer_preset_lively
        null -> Res.string.equalizer_preset_custom
    })

@Composable
private fun EqualizerCard(settings: UserSettings, settingsViewModel: SettingsViewModel) {
    var open by remember { mutableStateOf(false) }
    val title = stringResource(Res.string.settings_equalizer_title)

    SettingCardItem(
        title = title,
        subtitle = if (settings.equalizerEnabled) presetName(presetFor(settings.equalizerGains))
        else stringResource(Res.string.settings_equalizer_off),
        icon = { SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Waveform, title) },
        onClick = { open = true })

    if (open) {
        AdaptiveDialogBottomSheet(
            onDismiss = { open = false },
            dialogTitle = title,
            title = { Text(title, style = AppTheme.typography.titleMedium) },
            actions = {
                MillenniumButton(onClick = { open = false }) {
                    Text(stringResource(Res.string.common_close))
                }
            }) {
            EqualizerContent(settings, settingsViewModel)
        }
    }
}

@Composable
private fun EqualizerContent(settings: UserSettings, settingsViewModel: SettingsViewModel) {
    val gains = settings.equalizerGains.normalizedGains()
    val selected = presetFor(gains)

    // Choosing anything switches the equalizer on, so it's heard straight away
    fun choose(next: List<Int>) = settingsViewModel.updateSettings {
        copy(equalizerEnabled = true, equalizerGains = next)
    }

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.settings_equalizer_title),
                style = AppTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f))
            MillenniumSwitch(
                checked = settings.equalizerEnabled,
                onCheckedChange = { on -> settingsViewModel.updateSettings { copy(equalizerEnabled = on) } })
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            EqualizerPreset.entries.forEach { preset ->
                MushiFilterChip(
                    label = presetName(preset),
                    selected = settings.equalizerEnabled && selected == preset,
                    onClick = { choose(preset.gains) })
            }
        }
        EQ_BAND_HZ.forEachIndexed { index, hz ->
            BandSlider(
                label = if (hz < 1000) "$hz Hz" else "${hz / 1000f} kHz".replace(".0 ", " "),
                gain = gains[index],
                onChosen = { value -> choose(gains.mapIndexed { i, g -> if (i == index) value else g }) })
        }
    }
}

@Composable
private fun BandSlider(label: String, gain: Int, onChosen: (Int) -> Unit) {
    // Follows the finger while dragging; saved once, on release
    var dragged by remember(gain) { mutableStateOf((gain + EQ_MAX_GAIN) / (2f * EQ_MAX_GAIN)) }
    val shown = (dragged * 2 * EQ_MAX_GAIN).roundToInt() - EQ_MAX_GAIN

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = AppTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
        MillenniumSlider(
            value = dragged,
            onValueChange = { dragged = it },
            onValueChangeFinished = { onChosen((dragged * 2 * EQ_MAX_GAIN).roundToInt() - EQ_MAX_GAIN) },
            label = label,
            modifier = Modifier.weight(1f))
        Text(
            if (shown > 0) "+$shown dB" else "$shown dB",
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(52.dp))
    }
}
