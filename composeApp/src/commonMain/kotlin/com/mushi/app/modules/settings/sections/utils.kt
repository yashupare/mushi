package com.mushi.app.modules.settings.sections

import com.mushi.app.core.ui.theming.flatPanel


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumSeparator
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme

import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioFormat
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioQuality
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_audio_format_label
import com.mushi.app.generated.resources.settings_audio_quality_lossless
import com.mushi.app.generated.resources.settings_audio_quality_lossy

private val standardLossyQualities = listOf(
    AudioQuality.Lossy(bitrate = 44_000),
    AudioQuality.Lossy(bitrate = 96_000),
    AudioQuality.Lossy(bitrate = 128_000),
    AudioQuality.Lossy(bitrate = 256_000))

internal val streamingFormatPresets = listOf(
    AudioFormat(
        codec = "opus",
        container = "webm",
        qualities = standardLossyQualities),
    AudioFormat(
        codec = "aac",
        container = "mp4",
        qualities = standardLossyQualities),
    AudioFormat(
        codec = "vorbis",
        container = "ogg",
        qualities = standardLossyQualities)
)

internal val downloadFormatPresets = listOf(
    AudioFormat(
        codec = "aac",
        container = "mp4",
        qualities = standardLossyQualities),
    AudioFormat(
        codec = "mp3",
        container = "mp3",
        qualities = standardLossyQualities),
    AudioFormat(
        codec = "opus",
        container = "webm",
        qualities = standardLossyQualities),
    AudioFormat(
        codec = "flac",
        container = "flac",
        qualities = listOf(
            AudioQuality.Lossless(sampleRate = 44_100, channels = 2),
            AudioQuality.Lossless(sampleRate = 48_000, channels = 2))))

internal fun LazyListScope.settingsSectionHeader(title: StringResource) {
    item {
        Text(
            text = stringResource(title),
            style = AppTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * A page-level title for a settings destination (used by the large-screen sidebar
 * layout, where each section is its own page rather than one entry in a long list).
 */
@Composable
internal fun SettingsPageTitle(title: StringResource) {
    Text(
        text = stringResource(title),
        style = AppTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.colors.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
internal fun SettingsItemIcon(
    imageVector: ImageVector,
    contentDescription: String,
    // Neutral by default: a faint grey tile with a muted glyph, so rose is kept for toggles and
    // actions and a long list of settings reads calmly.
    tint: Color = AppTheme.colors.onSurfaceVariant) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.08f))) {
        Box(modifier = Modifier.padding(8.dp)) {
            MillenniumIcon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = tint)
        }
    }
}

internal fun availableAudioFormats(current: AudioFormat, presets: List<AudioFormat>): List<AudioFormat> {
    val options = mutableListOf(current)
    presets.forEach { preset ->
        val alreadyIncluded = options.any {
            it.codec.equals(preset.codec, ignoreCase = true) &&
                it.container.equals(preset.container, ignoreCase = true)
        }
        if (!alreadyIncluded) {
            options += preset
        }
    }
    return options
}

internal fun availableAudioQualities(format: AudioFormat, current: AudioQuality): List<AudioQuality> {
    val options = format.qualities.toMutableList()
    if (options.none { it == current }) {
        options += current
    }
    return options
}

@Composable
internal fun AudioFormat.displayLabel(): String {
    return stringResource(Res.string.settings_audio_format_label, codec.uppercase(), container.uppercase())
}

internal fun AudioFormat.preferredQuality(): AudioQuality {
    return qualities.lastOrNull() ?: AudioQuality.Lossy(bitrate = 256_000)
}

internal fun AudioFormat.resolveQuality(preferred: AudioQuality): AudioQuality {
    return qualities.firstOrNull { it == preferred } ?: preferredQuality()
}

@Composable
internal fun AudioQuality.displayLabel(): String {
    return when (this) {
        is AudioQuality.Lossy -> stringResource(Res.string.settings_audio_quality_lossy, bitrate / 1000)
        is AudioQuality.Lossless -> {
            val sampleRateLabel = if (sampleRate % 1000 == 0) {
                "${sampleRate / 1000}"
            } else {
                "${sampleRate / 1000.0}"
            }
            stringResource(Res.string.settings_audio_quality_lossless, sampleRateLabel, channels)
        }
    }
}

internal fun normalizePath(path: String): String {
    return path.trim().trimEnd('/', '\\')
}

internal fun LazyListScope.settingsSectionCard(
    items: List<@Composable () -> Unit>) {
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                // An inset grouped list: a plain rounded surface, rows divided by hairlines.
                .flatPanel(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            items.forEachIndexed { index, content ->
                if (index > 0) {
                    MillenniumSeparator()
                }
                content()
            }
        }
    }
}

