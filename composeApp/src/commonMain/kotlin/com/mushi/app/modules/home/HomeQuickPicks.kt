package com.mushi.app.modules.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composeunstyled.Text
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseItem
import com.mushi.app.core.ui.pressScale
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.home_greeting_afternoon
import com.mushi.app.generated.resources.home_greeting_evening
import com.mushi.app.generated.resources.home_greeting_morning
import kotlin.time.Clock
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** "Good morning", "Good afternoon" or "Good evening", by the device's local time. */
@Composable
internal fun rememberGreeting(): StringResource {
    val hour = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    }
    return when (hour) {
        in 5..11 -> Res.string.home_greeting_morning
        in 12..17 -> Res.string.home_greeting_afternoon
        else -> Res.string.home_greeting_evening
    }
}

/** What a quick pick needs from a browse item; tracks and users aren't shown as quick picks. */
internal data class QuickPick(val item: MetadataBrowseItem, val title: String, val imageUrl: String?)

internal fun MetadataBrowseItem.asQuickPick(): QuickPick? = when (this) {
    is MetadataBrowseItem.Album -> QuickPick(this, data.title, data.thumbnails.firstOrNull()?.url)
    is MetadataBrowseItem.Playlist -> QuickPick(this, data.title, data.thumbnails.firstOrNull()?.url)
    is MetadataBrowseItem.Artist -> QuickPick(this, data.name, data.thumbnails.firstOrNull()?.url)
    else -> null
}

/**
 * A dense two-column grid of compact tiles, like Spotify's and YouTube Music's top shelf: a
 * big thumbnail, a bold title, one tap to open. It sits right under the genre chips.
 */
@Composable
internal fun QuickPicksGrid(
    picks: List<QuickPick>,
    onClick: (MetadataBrowseItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val audioPlayerQueue: com.mushi.app.core.audioplayer.AudioPlayerQueue = org.koin.compose.koinInject()
    val playing by audioPlayerQueue.currentCollectionEntryFlow.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        picks.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { pick ->
                    val itemId = when (val item = pick.item) {
                        is MetadataBrowseItem.Album -> item.data.id
                        is MetadataBrowseItem.Playlist -> item.data.id
                        else -> null
                    }
                    QuickPickTile(
                        pick = pick,
                        isPlaying = itemId != null && itemId == playing?.id,
                        onClick = { onClick(pick.item) },
                        modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickPickTile(
    pick: QuickPick,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .height(60.dp)
            .pressScale(0.97f)
            .clip(shape)
            .background(AppTheme.colors.onSurface.copy(alpha = 0.07f))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(60.dp)
                .background(AppTheme.colors.onSurface.copy(alpha = 0.1f))
        ) {
            AsyncImage(
                model = pick.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Crop)
            if (isPlaying) {
                com.mushi.app.core.ui.component.NowPlayingBadge(Modifier.align(Alignment.Center))
            }
        }
        Text(
            text = pick.title,
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
    }
}
