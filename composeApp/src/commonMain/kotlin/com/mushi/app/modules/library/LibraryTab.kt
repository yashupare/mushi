package com.mushi.app.modules.library

import androidx.compose.ui.graphics.vector.ImageVector
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxCd
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.resources.iconsax.IconsaxMusicDashboard
import com.mushi.app.resources.iconsax.IconsaxMusicLibrary
import com.mushi.app.resources.iconsax.User
import org.jetbrains.compose.resources.StringResource
import com.mushi.app.generated.resources.*

enum class LibraryTab(val icon: ImageVector) {
    Playlists(com.mushi.app.core.ui.theming.MushiIcons.Playlist),
    Albums(com.mushi.app.core.ui.theming.MushiIcons.Album),
    Artists(com.mushi.app.core.ui.theming.MushiIcons.User),
    LocalTracks(com.mushi.app.core.ui.theming.MushiIcons.Playlist),
    Downloads(com.mushi.app.core.ui.theming.MushiIcons.Download),
    History(com.mushi.app.core.ui.theming.MushiIcons.MusicNote);

    val titleResource: StringResource
        get() = when (this) {
            Playlists -> Res.string.library_tab_playlists
            Albums -> Res.string.library_tab_albums
            Artists -> Res.string.library_tab_artists
            LocalTracks -> Res.string.library_tab_local_tracks
            Downloads -> Res.string.library_tab_downloads
            History -> Res.string.library_tab_history
        }

    val filterPlaceholder: StringResource
        get() = when (this) {
            Playlists -> Res.string.library_filter_playlists
            Albums -> Res.string.library_filter_albums
            Artists -> Res.string.library_filter_artists
            LocalTracks -> Res.string.library_filter_local_tracks
            Downloads, History -> Res.string.search_filter_placeholder
        }
}
