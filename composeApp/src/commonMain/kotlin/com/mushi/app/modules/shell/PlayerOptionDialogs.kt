package com.mushi.app.modules.shell


import com.mushi.app.core.ui.component.MushiButton
import org.spotube.millennium.composables.MillenniumDialogHeader
import org.spotube.millennium.composables.MillenniumDialogTitle
import com.mushi.app.core.ui.component.AdaptiveDialogBottomSheet
import com.mushi.app.core.ui.component.AppDialogClose
import com.mushi.app.core.ui.misc.DetailRowsSkeleton
import org.spotube.millennium.composables.MillenniumDialogBody
import org.spotube.millennium.composables.MillenniumDialogFooter
import org.spotube.millennium.composables.MillenniumMarqueeProgress
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumDialog
import org.spotube.millennium.composables.MillenniumTextField
import org.spotube.millennium.theme.millenniumSurface
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import com.mushi.app.core.ui.theming.AppTheme

import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioStream
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxCheckCircle
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.core.server.CacheEntry
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.library_tab_artists
import com.mushi.app.generated.resources.media_artist
import com.mushi.app.generated.resources.player_sleep_timer
import com.mushi.app.generated.resources.player_sleep_timer_cancel
import com.mushi.app.generated.resources.player_sleep_timer_description
import com.mushi.app.generated.resources.player_sleep_timer_minutes_label
import com.mushi.app.generated.resources.player_sleep_timer_minutes_placeholder
import com.mushi.app.generated.resources.player_sleep_timer_remaining
import com.mushi.app.generated.resources.player_sleep_timer_start
import com.mushi.app.generated.resources.player_track_details
import com.mushi.app.generated.resources.player_track_details_active_url
import com.mushi.app.generated.resources.player_track_details_album
import com.mushi.app.generated.resources.player_track_details_album_genres
import com.mushi.app.generated.resources.player_track_details_album_id
import com.mushi.app.generated.resources.player_track_details_album_release_date
import com.mushi.app.generated.resources.player_track_details_album_track_count
import com.mushi.app.generated.resources.player_track_details_album_type
import com.mushi.app.generated.resources.player_track_details_available_streams
import com.mushi.app.generated.resources.player_track_details_confidence
import com.mushi.app.generated.resources.player_track_details_disc_number
import com.mushi.app.generated.resources.player_track_details_duration
import com.mushi.app.generated.resources.player_track_details_explicit
import com.mushi.app.generated.resources.player_track_details_external_uri
import com.mushi.app.generated.resources.player_track_details_id
import com.mushi.app.generated.resources.player_track_details_isrc
import com.mushi.app.generated.resources.player_track_details_loading_source
import com.mushi.app.generated.resources.player_track_details_loading_streams
import com.mushi.app.generated.resources.player_track_details_matched_source
import com.mushi.app.generated.resources.player_track_details_matches_preference
import com.mushi.app.generated.resources.player_track_details_metadata_track
import com.mushi.app.generated.resources.player_track_details_no_source
import com.mushi.app.generated.resources.player_track_details_no_streams
import com.mushi.app.generated.resources.player_track_details_popularity
import com.mushi.app.generated.resources.player_track_details_selected_stream_content_description
import com.mushi.app.generated.resources.player_track_details_served_from_cache
import com.mushi.app.generated.resources.player_track_details_served_from_cache_description
import com.mushi.app.generated.resources.player_track_details_source_not_needed
import com.mushi.app.generated.resources.player_track_details_stream_mismatch
import com.mushi.app.generated.resources.player_track_details_thumbnail_urls
import com.mushi.app.generated.resources.player_track_details_title
import com.mushi.app.generated.resources.player_track_details_track_number
import com.mushi.app.generated.resources.common_close

@Composable
fun PlayerOptionDialogs(viewModel: PlayerOptionsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isSleepTimerDialogOpen) {
        SleepTimerDialog(
            minutes = uiState.sleepTimerMinutes,
            remainingMs = uiState.sleepTimerRemainingMs,
            onMinutesChange = viewModel::updateSleepTimerMinutes,
            onStart = viewModel::startSleepTimer,
            onCancelTimer = viewModel::cancelSleepTimer,
            onDismiss = viewModel::dismissSleepTimerDialog)
    }

    uiState.trackDetails?.let { details ->
        TrackDetailsDialog(
            details = details,
            onDismiss = viewModel::dismissTrackDetails)
    }
}

@Composable
private fun SleepTimerDialog(
    minutes: String,
    remainingMs: Long?,
    onMinutesChange: (String) -> Unit,
    onStart: () -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit) {
    MillenniumDialog(visible = true, onDismissRequest = onDismiss) {
        MillenniumDialogHeader {
            MillenniumDialogTitle(stringResource(Res.string.player_sleep_timer))
            AppDialogClose(onDismiss)
        }
        MillenniumDialogBody {
            if (remainingMs != null) {
                Text(
                    text = stringResource(Res.string.player_sleep_timer_remaining, formatRemainingTime(remainingMs)),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.primary)
            } else {
                Text(
                    text = stringResource(Res.string.player_sleep_timer_description),
                    style = AppTheme.typography.bodyMedium)
            }
            val minutesState = rememberTextFieldState(minutes)
            LaunchedEffect(minutesState.text) {
                onMinutesChange(minutesState.text.toString())
            }
            MillenniumTextField(
                state = minutesState,
                label = stringResource(Res.string.player_sleep_timer_minutes_label),
                placeholder = stringResource(Res.string.player_sleep_timer_minutes_placeholder),
                modifier = Modifier.fillMaxWidth())
        }
        MillenniumDialogFooter {
            if (remainingMs != null) {
                MillenniumButton(
                    onClick = onCancelTimer,
                    style = MillenniumButtonStyle.Quiet) { Text(stringResource(Res.string.player_sleep_timer_cancel)) }
            }
            MillenniumButton(
                onClick = onDismiss,
                style = MillenniumButtonStyle.Secondary) { Text(stringResource(Res.string.common_close)) }
            MushiButton(
                onClick = onStart,
                enabled = minutes.toLongOrNull()?.let { it in 1L..600L } == true) {
                Text(stringResource(Res.string.player_sleep_timer_start))
            }
        }
    }
}

@Composable
private fun TrackDetailsDialog(
    details: PlayerTrackDetails,
    onDismiss: () -> Unit) {
    // A bottom sheet on phones and a dialog on large screens, so it never runs under the status bar.
    AdaptiveDialogBottomSheet(
        onDismiss = onDismiss,
        dialogTitle = stringResource(Res.string.player_track_details),
        title = {
            Text(
                stringResource(Res.string.player_track_details),
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        },
        actions = {
            MillenniumButton(
                onClick = onDismiss,
                style = MillenniumButtonStyle.Secondary) { Text(stringResource(Res.string.common_close)) }
        }) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                details.track.title,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant)
            if (details.isLoading) {
                DetailRowsSkeleton(Modifier.padding(vertical = 8.dp))
            }

            if (details.localCacheEntry != null) {
                LocalCacheBanner(details.localCacheEntry)
            }
            if (details.localCacheEntry == null && details.activeStreamMatchesPreference == false) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.tertiaryContainer)) {
                    Text(
                        stringResource(Res.string.player_track_details_stream_mismatch),
                        modifier = Modifier.padding(12.dp),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.onTertiaryContainer)
                }
            }

            TrackMetadataSection(details.track)

            Text(
                stringResource(Res.string.player_track_details_matched_source),
                style = AppTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            val source = details.matchedSource
            if (details.isLoading) {
                Text(stringResource(Res.string.player_track_details_loading_source), style = AppTheme.typography.bodySmall)
            } else if (source == null) {
                Text(
                    details.error ?: details.errorRes?.let { stringResource(it) } ?: if (details.localCacheEntry != null) {
                        stringResource(Res.string.player_track_details_source_not_needed)
                    } else {
                        stringResource(Res.string.player_track_details_no_source)
                    },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant)
            } else {
                DetailRow(stringResource(Res.string.player_track_details_title), source.title)
                DetailRow(stringResource(Res.string.player_track_details_id), source.id)
                DetailRow(stringResource(Res.string.media_artist), source.artist)
                DetailRow(stringResource(Res.string.player_track_details_album), source.album)
                DetailRow(stringResource(Res.string.player_track_details_confidence), "${(source.confidence * 100).toInt()}%")
                DetailRow(stringResource(Res.string.player_track_details_external_uri), source.externalUri)
            }

            Text(
                stringResource(Res.string.player_track_details_available_streams),
                style = AppTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            when {
                details.localCacheEntry != null && details.streams.isEmpty() -> Text(
                    stringResource(Res.string.player_track_details_served_from_cache_description),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant)

                details.isLoading -> Text(stringResource(Res.string.player_track_details_loading_streams), style = AppTheme.typography.bodySmall)
                details.streams.isEmpty() -> Text(
                    details.error ?: details.errorRes?.let { stringResource(it) } ?: stringResource(Res.string.player_track_details_no_streams),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant)

                else -> details.streams.forEach { stream ->
                    val isSelected = stream == details.preferredStream
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    AppTheme.colors.primaryContainer
                                } else {
                                    AppTheme.colors.surfaceContainerHigh
                                })) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = streamFormatAndQuality(stream),
                                    style = AppTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        AppTheme.colors.onPrimaryContainer
                                    } else {
                                        AppTheme.colors.onSurface
                                    },
                                    modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    MillenniumIcon(
                                        imageVector = Iconsax.IconsaxCheckCircle,
                                        contentDescription = stringResource(Res.string.player_track_details_selected_stream_content_description),
                                        tint = AppTheme.colors.primary)
                                }
                            }
                            Text(
                                "${stream.protocol} • ${stream.codec} • ${stream.container}",
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.onSurfaceVariant)
                            Text(
                                stream.url,
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.onSurfaceVariant)
                            if (isSelected) {
                                Text(
                                    stringResource(Res.string.player_track_details_matches_preference),
                                    style = AppTheme.typography.labelSmall,
                                    color = AppTheme.colors.primary)
                            }
                        }
                    }
                }
            }

            details.activeStreamUrl?.let { activeUrl ->
                DetailRow(stringResource(Res.string.player_track_details_active_url), activeUrl)
            }
        }
    }
}

@Composable
private fun LocalCacheBanner(entry: CacheEntry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.secondaryContainer)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            MillenniumIcon(
                imageVector = com.mushi.app.core.ui.theming.MushiIcons.Download,
                contentDescription = null,
                tint = AppTheme.colors.onSecondaryContainer)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(Res.string.player_track_details_served_from_cache),
                    style = AppTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.onSecondaryContainer)
                Text(
                    "${entry.filename} • ${formatFileSize(entry.sizeBytes)}",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.onSecondaryContainer)
            }
        }
    }
}

@Composable
private fun TrackMetadataSection(track: MetadataTrack) {
    Text(
        stringResource(Res.string.player_track_details_metadata_track),
        style = AppTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold)
    DetailRow(stringResource(Res.string.player_track_details_id), track.id)
    DetailRow(stringResource(Res.string.player_track_details_title), track.title)
    DetailRow(stringResource(Res.string.library_tab_artists), track.artists.joinToString { it.name })
    DetailRow(stringResource(Res.string.player_track_details_album), track.album?.title)
    DetailRow(stringResource(Res.string.player_track_details_album_id), track.album?.id)
    DetailRow(stringResource(Res.string.player_track_details_album_type), track.album?.albumType?.name)
    DetailRow(stringResource(Res.string.player_track_details_album_release_date), track.album?.releaseDate?.substringBefore('T'))
    DetailRow(stringResource(Res.string.player_track_details_album_track_count), track.album?.trackCount?.toString())
    DetailRow(stringResource(Res.string.player_track_details_album_genres), track.album?.genres?.joinToString())
    DetailRow(stringResource(Res.string.player_track_details_duration), formatRemainingTime(track.durationMs))
    DetailRow(stringResource(Res.string.player_track_details_track_number), track.trackNumber?.toString())
    DetailRow(stringResource(Res.string.player_track_details_disc_number), track.discNumber?.toString())
    DetailRow(stringResource(Res.string.player_track_details_explicit), track.explicit?.toString())
    DetailRow(stringResource(Res.string.player_track_details_popularity), track.popularity?.toString())
    DetailRow(stringResource(Res.string.player_track_details_isrc), track.isrcCode)
    DetailRow(stringResource(Res.string.player_track_details_external_uri), track.externalUri)
    DetailRow(stringResource(Res.string.player_track_details_thumbnail_urls), track.thumbnails?.joinToString { it.url })
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            modifier = Modifier.weight(0.38f),
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colors.onSurfaceVariant)
        Text(
            value,
            modifier = Modifier.weight(0.62f),
            style = AppTheme.typography.bodySmall)
    }
}

private fun streamFormatAndQuality(stream: AudioStream): String = when (stream) {
    is AudioStream.Lossy -> "${stream.bitrate / 1_000} kbps"
    is AudioStream.Lossless -> {
        val rate = if (stream.sampleRate % 1_000 == 0) {
            "${stream.sampleRate / 1_000}"
        } else {
            "${stream.sampleRate / 1_000.0}"
        }
        "$rate kHz • ${stream.channels} ch"
    }
}

private fun formatRemainingTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1_000).coerceAtLeast(0)
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

private fun formatFileSize(sizeBytes: Long): String = when {
    sizeBytes >= 1024L * 1024L -> "${sizeBytes / (1024L * 1024L)} MB"
    sizeBytes >= 1024L -> "${sizeBytes / 1024L} KB"
    else -> "$sizeBytes B"
}
