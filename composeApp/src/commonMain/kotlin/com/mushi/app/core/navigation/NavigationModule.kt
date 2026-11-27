package com.mushi.app.core.navigation

import androidx.navigation3.runtime.NavKey
import com.mushi.app.modules.album.AlbumScreen
import com.mushi.app.modules.artist.ArtistScreen
import com.mushi.app.modules.blacklist.BlacklistScreen
import com.mushi.app.modules.devices.DevicesScreen
import com.mushi.app.modules.devices.RemoteControlScreen
import com.mushi.app.modules.jam.JamScreen
import com.mushi.app.modules.home.HomeScreen
import com.mushi.app.modules.library.LibraryScreen
import com.mushi.app.modules.library.playlist.ImportPlaylistScreen
import com.mushi.app.modules.library.local_tracks.LocalFolderScreen
import com.mushi.app.modules.library.local_tracks.LocalFolderViewModel
import com.mushi.app.modules.lyrics.LyricsScreen
import com.mushi.app.modules.playlist.PlaylistScreen
import com.mushi.app.modules.plugin.PluginScreen
import com.mushi.app.modules.plugin.guard.MetadataPluginGuard
import com.mushi.app.modules.saved_tracks.SAVED_TRACKS_COLLECTION_ID
import com.mushi.app.modules.saved_tracks.SavedTracksScreen
import com.mushi.app.modules.search.SearchScreen
import com.mushi.app.modules.settings.SettingsScreen
import com.mushi.app.modules.settings.JamSettingsScreen
import com.mushi.app.modules.welcome.WelcomeScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.home_title_browse
import com.mushi.app.generated.resources.nav_search

@Serializable
sealed interface Routes : NavKey {
    @Serializable
    data object Home : Routes

    @Serializable
    data object Library : Routes

    @Serializable
    data object Search : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object Plugins : Routes

    @Serializable
    data object WebView : Routes

    @Serializable
    data object Lyrics : Routes

    @Serializable
    data class Playlist(val playlistId: String) : Routes

    @Serializable
    data class Artist(val artistId: String) : Routes

    @Serializable
    data class Album(val albumId: String) : Routes

    @Serializable
    data object SavedTracks : Routes

    @Serializable
    data object Blacklist : Routes

    @Serializable
    data object RemoteControl : Routes

    @Serializable
    data object Devices : Routes

    @Serializable
    data object Jam : Routes

    @Serializable
    data object JamSettings : Routes

    @Serializable
    data object Welcome : Routes

    @Serializable
    data object ImportPlaylist : Routes

    @Serializable
    data class LocalFolder(val folderPath: String) : Routes
}

@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {
    singleOf(::NavigationCommands)
    navigation<Routes.Home> {
        // requireLogin: show the sign-in prompt straight away instead of a failed-request error.
        MetadataPluginGuard(
            requireLogin = true,
            title = stringResource(Res.string.home_title_browse)
        ) {
            HomeScreen(viewModel = koinViewModel())
        }
    }
    navigation<Routes.Search> {
        MetadataPluginGuard(
            requireLogin = true,
            title = stringResource(Res.string.nav_search)
        ) {
            SearchScreen()
        }
    }
    navigation<Routes.Library> {
        // Not guarded as a whole: Local tracks and Downloads work without a plugin.
        LibraryScreen()
    }
    navigation<Routes.LocalFolder> {
        LocalFolderScreen(
            viewModel = koinViewModel(
                key = it.folderPath,
                parameters = { parametersOf(it.folderPath) }
            ),
            onBack = { get<NavigationCommands>().pop() })
    }
    navigation<Routes.Settings> {
        SettingsScreen(settingsViewModel = koinViewModel())
    }
    navigation<Routes.Plugins> {
        PluginScreen(viewModel = koinViewModel())
    }
    navigation<Routes.WebView> {}
    navigation<Routes.Playlist> {
        MetadataPluginGuard(backButton = true) {
            PlaylistScreen(
                playlistId = it.playlistId,
                viewModel = koinViewModel(
                    key = it.playlistId,
                    parameters = { parametersOf(it.playlistId) }
                ),
                audioPlayerQueue = get(),
                audioPlayer = get(),
                navigationCommands = get())
        }
    }
    navigation<Routes.Artist> {
        MetadataPluginGuard(backButton = true) {
            ArtistScreen(
                viewModel = koinViewModel(
                    key = it.artistId,
                    parameters = { parametersOf(it.artistId) }
                ),
                audioPlayerQueue = get(),
                audioPlayer = get(),
                navigationCommands = get())
        }
    }
    navigation<Routes.Album> {
        MetadataPluginGuard(backButton = true) {
            AlbumScreen(
                albumId = it.albumId,
                viewModel = koinViewModel(
                    key = it.albumId,
                    parameters = { parametersOf(it.albumId) }
                ),
                audioPlayerQueue = get(),
                audioPlayer = get(),
                navigationCommands = get())
        }
    }
    navigation<Routes.Lyrics> {
        LyricsScreen(viewModel = koinViewModel())
    }
    navigation<Routes.SavedTracks> {
        MetadataPluginGuard(backButton = true) {
            SavedTracksScreen(
                viewModel = koinViewModel(
                    key = SAVED_TRACKS_COLLECTION_ID,
                    parameters = { parametersOf(SAVED_TRACKS_COLLECTION_ID) }
                ),
                audioPlayerQueue = get(),
                audioPlayer = get(),
                navigationCommands = get())
        }
    }
    navigation<Routes.Blacklist> {
        BlacklistScreen()
    }
    navigation<Routes.Devices> {
        DevicesScreen()
    }
    navigation<Routes.RemoteControl> {
        RemoteControlScreen(
            onDisconnect = { get<NavigationCommands>().pop() }
        )
    }
    navigation<Routes.Jam> {
        JamScreen(navigationCommands = get())
    }
    navigation<Routes.JamSettings> {
        JamSettingsScreen(viewModel = koinViewModel())
    }
    navigation<Routes.ImportPlaylist> {
        MetadataPluginGuard(backButton = true, requireLogin = true) {
            val navigation = get<NavigationCommands>()
            ImportPlaylistScreen(
                viewModel = koinViewModel(),
                onOpenPlaylist = { navigation.navigateTo(Routes.Playlist(it)) })
        }
    }
    navigation<Routes.Welcome> {
        WelcomeScreen(viewModel = koinViewModel())
    }
}
