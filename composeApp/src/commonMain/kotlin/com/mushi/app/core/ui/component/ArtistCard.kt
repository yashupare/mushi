package com.mushi.app.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.ui.component.cards.AvatarCard
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@Composable
fun ArtistCard(
    artist: MetadataArtist.Basic,
    modifier: Modifier = Modifier,
    isBlacklisted: Boolean = false,
    listMode: Boolean = false
) {
    val navigationCommands = koinInject<NavigationCommands>()

    AvatarCard(
        title = artist.name,
        subtitle = stringResource(if (isBlacklisted) Res.string.media_blacklisted else Res.string.media_artist),
        imageURL = artist.thumbnails.firstOrNull()?.url,
        onClick = {
            navigationCommands.navigateTo(Routes.Artist(artist.id))
        },
        modifier = modifier,
        isBlacklisted = isBlacklisted,
        listMode = listMode
    )
}
