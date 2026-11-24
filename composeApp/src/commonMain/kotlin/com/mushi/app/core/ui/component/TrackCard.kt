package com.mushi.app.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import compose.icons.FeatherIcons
import compose.icons.feathericons.Play
import compose.icons.feathericons.PlusSquare
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import com.mushi.app.core.ui.component.cards.PlayableCard
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun TrackCard(
    track: MetadataTrack,
    modifier: Modifier = Modifier,
    audioPlayerQueue: AudioPlayerQueue = koinInject()) {
    val scope = rememberCoroutineScope()
    val queueEntry = QueueEntry.StreamingTrack(track = track, url = "")

    PlayableCard(
        title = track.title,
        subtitle = track.artists.joinToString { it.name },
        imageURL = (track.album?.thumbnails ?: track.thumbnails)?.firstOrNull()?.url,
        onPlay = {
            scope.launch {
                audioPlayerQueue.load(
                    entries = listOf(queueEntry),
                    autoPlay = true,
                    startPosition = 0,
                    collectionEntry = null)
            }
        },
        onAddToQueue = {
            scope.launch {
                audioPlayerQueue.addToQueue(queueEntry)
            }
        },
        modifier = modifier
    )
}

