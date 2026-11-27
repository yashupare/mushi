package com.mushi.app.core.di

import org.koin.core.module.dsl.onClose
import com.mushi.app.core.network.HttpClientFactory
import co.touchlab.kermit.Logger
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.AudioPlayerQueueRepository
import com.mushi.app.core.audioplayer.DeviceAudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueStateRepository
import com.mushi.app.core.db.Database
import com.mushi.app.core.discovery.DeviceDiscoveryService
import com.mushi.app.core.discord.DiscordRpcService
import com.mushi.app.core.history.ListeningHistoryRecorder
import com.mushi.app.core.history.ListeningHistoryRepository
import com.mushi.app.core.moments.ConstellationPlayer
import com.mushi.app.core.moments.MomentsRepository
import com.mushi.app.modules.home.SkyViewModel
import com.mushi.app.modules.settings.BackupViewModel
import com.mushi.app.modules.shell.KeepMomentViewModel
import com.mushi.app.modules.shell.SleepTimerProgress
import com.mushi.app.modules.history.HistoryViewModel
import com.mushi.app.modules.library.playlist.ImportPlaylistViewModel
import com.mushi.app.core.i18n.getString
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.import_default_name
import com.mushi.app.core.jam.JamRoomClient
import com.mushi.app.core.jam.JamRoomService
import com.mushi.app.core.navigation.navigationModule
import com.mushi.app.core.remote.RemoteControlClient
import com.mushi.app.core.remote.RemoteControlHandler
import com.mushi.app.core.remote.RemoteControlService
import com.mushi.app.core.remote.RemotePlaybackController
import com.mushi.app.core.scrobble.ScrobbleManager
import com.mushi.app.core.playback.CollectionPlaybackHelper
import com.mushi.app.core.server.AlternativeTracksRepository
import com.mushi.app.core.server.CacheManager
import com.mushi.app.core.server.LocalServer
import com.mushi.app.core.server.MatchedTracksRepository
import com.mushi.app.core.server.TrackSourceRepository
import com.mushi.app.core.server.StreamingUrlRepository
import com.mushi.app.core.webview.WebViewController
import com.mushi.app.modules.album.AlbumRepository
import com.mushi.app.modules.album.AlbumViewModel
import com.mushi.app.modules.artist.ArtistRepository
import com.mushi.app.modules.artist.ArtistViewModel
import com.mushi.app.modules.blacklist.BlacklistRepository
import com.mushi.app.modules.blacklist.BlacklistViewModel
import com.mushi.app.modules.devices.DevicesViewModel
import com.mushi.app.modules.devices.RemoteControlViewModel
import com.mushi.app.modules.shell.ConnectStatusViewModel
import com.mushi.app.modules.jam.JamViewModel
import com.mushi.app.modules.downloads.DownloadManager
import com.mushi.app.modules.downloads.DownloadsViewModel
import com.mushi.app.modules.home.HomeScreenRepository
import com.mushi.app.modules.home.HomeScreenViewModel
import com.mushi.app.modules.home.ContinueViewModel
import com.mushi.app.modules.plugin.MetadataAuthViewModel
import com.mushi.app.modules.library.LibraryRepository
import com.mushi.app.modules.library.LibraryState
import com.mushi.app.modules.library.album.LibraryAlbumsViewModel
import com.mushi.app.modules.library.artist.LibraryArtistsViewModel
import com.mushi.app.modules.library.local_tracks.LibraryLocalTracksViewModel
import com.mushi.app.modules.library.local_tracks.LocalFolderViewModel
import com.mushi.app.modules.library.local_tracks.media.LocalMediaCacheRepository
import com.mushi.app.modules.library.local_tracks.media.LocalMediaFoldersConfig
import com.mushi.app.modules.library.local_tracks.media.LocalMediaLibraryCoordinator
import com.mushi.app.modules.library.playlist.LibraryPlaylistsViewModel
import com.mushi.app.modules.lyrics.LyricsViewModel
import com.mushi.app.modules.playlist.AddTracksToPlaylistViewModel
import com.mushi.app.modules.playlist.PlaylistRepository
import com.mushi.app.modules.playlist.PlaylistViewModel
import com.mushi.app.modules.plugin.AudioPluginSource
import com.mushi.app.modules.plugin.PluginManager
import com.mushi.app.modules.plugin.PluginProvider
import com.mushi.app.modules.plugin.PluginViewModel
import com.mushi.app.modules.plugin.guard.MetadataPluginGuardViewModel
import com.mushi.app.modules.plugin.ScrobblePluginSource
import com.mushi.app.modules.saved_tracks.SavedTracksRepository
import com.mushi.app.modules.saved_tracks.SavedTracksViewModel
import com.mushi.app.modules.search.SearchRepository
import com.mushi.app.modules.search.SearchScreenViewModel
import com.mushi.app.modules.update.AppUpdateService
import com.mushi.app.modules.update.PluginUpdateService
import com.mushi.app.modules.settings.SettingsProvider
import com.mushi.app.modules.settings.SettingsRepository
import com.mushi.app.modules.settings.UserSettingsStore
import com.mushi.app.modules.settings.UserSettingsSource
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.JamSettingsViewModel
import com.mushi.app.modules.settings.LocalMediaFoldersViewModel
import com.mushi.app.modules.shell.AppShellViewModel
import com.mushi.app.modules.shell.PlayerOptionsViewModel
import com.mushi.app.modules.shell.alternative_track.AlternativeTrackContentViewModel
import com.mushi.app.modules.shell.player_queue.PlayerQueueContentViewModel
import com.mushi.app.modules.welcome.WelcomeRepository
import com.mushi.app.modules.welcome.WelcomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import com.mushi.app.core.zipline.permissions.PublicSuffixListRepository
import com.mushi.app.core.server.NextTrackPreloader
import com.mushi.app.core.server.TrackPreloader
import kotlin.time.Duration
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob

expect val platformModules: Module

val sharedModules = module {
    includes(navigationModule)
    singleOf(::Database)
    single { HttpClientFactory() } withOptions { onClose { it?.close() } }

    singleOf(::WebViewController)

    // Shell
    viewModel { AppShellViewModel(get()) }
    viewModelOf(::PlayerOptionsViewModel)
    viewModelOf(::PlayerQueueContentViewModel)
    viewModelOf(::AlternativeTrackContentViewModel)

    // Home
    singleOf(::HomeScreenRepository)
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::ContinueViewModel)
    viewModelOf(::MetadataAuthViewModel)

    // Search
    singleOf(::SearchRepository)
    viewModelOf(::SearchScreenViewModel)

    // Library
    singleOf(::LibraryState)
    singleOf(::LibraryRepository)
    singleOf(::LocalMediaCacheRepository)
    singleOf(::LocalMediaFoldersConfig)
    singleOf(::LocalMediaLibraryCoordinator)
    viewModelOf(::LibraryPlaylistsViewModel)
    viewModelOf(::LibraryAlbumsViewModel)
    viewModelOf(::LibraryArtistsViewModel)
    viewModelOf(::LibraryLocalTracksViewModel)
    viewModel { (folderPath: String) -> LocalFolderViewModel(folderPath, get(), get()) }

    // Plugin system
    single { PublicSuffixListRepository.create(get(), get()) } withOptions { createdAtStart() }
    singleOf(::PluginManager) {
        bind<PluginProvider>()
        bind<AudioPluginSource>()
        bind<ScrobblePluginSource>()
    }
    viewModelOf(::PluginViewModel)
    viewModelOf(::MetadataPluginGuardViewModel)

    // Settings
    singleOf(::SettingsRepository) {
        bind<UserSettingsSource>()
        bind<UserSettingsStore>()
    }
    viewModelOf(::SettingsViewModel) { bind<SettingsProvider>() }
    viewModelOf(::JamSettingsViewModel)
    viewModel {
        val repository = get<SettingsRepository>()
        LocalMediaFoldersViewModel(repository.userSettings) { folders ->
            val current = repository.getCurrentSettings()
            repository.updateSettings(current.copy(localMediaFolders = folders))
        }
    }

    // Welcome / Onboarding
    singleOf(::WelcomeRepository)
    viewModelOf(::WelcomeViewModel)

    // Updates
    single { AppUpdateService(get(), get()) } withOptions { createdAtStart() }
    single { PluginUpdateService(get(), get()) } withOptions { createdAtStart() }

    // Downloads
    singleOf(::DownloadManager)
    viewModelOf(::DownloadsViewModel)

    // Playlist
    singleOf(::PlaylistRepository)
    viewModel { (playlistId: String) ->
        PlaylistViewModel(
            playlistId = playlistId,
            repository = get(),
            savedTracksRepository = get(),
            libraryRepository = get(),
            playbackHelper = get(),
            audioPlayerQueue = get(),
            blacklistRepository = get(),
            shareService = get(),
            downloadManager = get(),
            remotePlaybackController = get(),
            trackPreloader = get())
    }
    viewModel { (playlistId: String) -> AddTracksToPlaylistViewModel(playlistId, get(), get()) }

    // Saved Tracks
    singleOf(::SavedTracksRepository)
    viewModel {
        SavedTracksViewModel(
            repository = get(),
            playbackHelper = get(),
            audioPlayerQueue = get(),
            blacklistRepository = get(),
            libraryRepository = get(),
            shareService = get(),
            downloadManager = get(),
            remotePlaybackController = get())
    }

    // Artist
    singleOf(::ArtistRepository)
    viewModel { (artistId: String) ->
        ArtistViewModel(
            artistId = artistId,
            repository = get(),
            libraryRepository = get(),
            savedTracksRepository = get(),
            audioPlayerQueue = get(),
            blacklistRepository = get(),
            shareService = get(),
            downloadManager = get(),
            remotePlaybackController = get())
    }

    // Blacklist
    singleOf(::BlacklistRepository)
    viewModelOf(::BlacklistViewModel)
    viewModel { DevicesViewModel(get()) }
    viewModelOf(::RemoteControlViewModel)
    viewModelOf(::ConnectStatusViewModel)
    viewModel {
        JamViewModel(
            jamRoomService = get(),
            shareService = get(),
            settingsProvider = get())
    }

    // Album
    singleOf(::AlbumRepository)
    viewModel { (albumId: String) ->
        AlbumViewModel(
            trackPreloader = get(),
            albumId = albumId,
            repository = get(),
            savedTracksRepository = get(),
            playbackHelper = get(),
            libraryRepository = get(),
            audioPlayerQueue = get(),
            blacklistRepository = get(),
            shareService = get(),
            downloadManager = get(),
            remotePlaybackController = get())
    }

    // Lyrics
    viewModelOf(::LyricsViewModel)

    // Local playback proxy server
    single {
        CollectionPlaybackHelper(
            albumRepository = get(),
            playlistRepository = get(),
            savedTracksRepository = get(),
            artistRepository = get(),
            audioPlayerQueue = get(),
            blacklistRepository = get())
    }
    singleOf(::MatchedTracksRepository) { bind<TrackSourceRepository>() }
    single {
        StreamingUrlRepository(
            pluginManager = get(),
            audioPlayerQueue = get(),
            matchedTracksRepository = get(),
            settingsRepository = get())
    }
    singleOf(::CacheManager)
    single {
        val urls = get<StreamingUrlRepository>()
        val cache = get<CacheManager>()
        val settings = get<UserSettingsSource>()
        val logger = Logger.withTag("TrackPreloader")
        TrackPreloader(
            lookUp = { track ->
                runCatching {
                    // A song already in the music cache plays from disk, no stream needed
                    val onDisk = settings.userSettings.value.enableMusicCaching &&
                        cache.findCachedEntry(track.id) != null
                    if (!onDisk) urls.resolveStreamInfo(track)
                }.onFailure { logger.w(it) { "Couldn't preload ${track.id}" } }
            },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()))
    }
    single {
        val player = get<AudioPlayerInterface>()
        val queue = get<AudioPlayerQueue>()
        val preloader = get<TrackPreloader>()
        NextTrackPreloader(
            queue = queue.queueFlow,
            currentEntry = queue.currentQueueEntryFlow,
            playerState = player.playerStateFlow,
            loopState = player.loopStateFlow,
            // The preloader's own settle time has already passed by the time this is called
            preload = { preloader.preloadOnceSettled(it, Duration.ZERO) },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()))
    } withOptions { createdAtStart() }
    singleOf(::AlternativeTracksRepository)
    singleOf(::LocalServer) withOptions {
        createdAtStart()
    }
    single { RemoteControlHandler(get(), get(), get(), get()) }
    single { RemoteControlClient(get()) }
    singleOf(::DeviceDiscoveryService)
    single { RemoteControlService(get(), get(), get()) } withOptions {
        createdAtStart()
    }
    single { RemotePlaybackController(get(), get(), get(), get(), get()) }
    singleOf(::JamRoomClient)
    single {
        JamRoomService(
            get(), get(), get(), get(), get(), get(), get(),
            evictCachedAudio = get<CacheManager>()::invalidateCacheEntry)
    }
    singleOf(::AudioPlayerQueueRepository) { bind<QueueStateRepository>() }
    single<AudioPlayerQueue> {
        DeviceAudioPlayerQueue(get(), get(), get(), get(), get())
    }

    single { DiscordRpcService(get(), get()) } withOptions { createdAtStart() }

    // Kept moments and constellations
    singleOf(::MomentsRepository)
    single { ConstellationPlayer(get(), get()) }
    singleOf(::SleepTimerProgress)
    viewModelOf(::KeepMomentViewModel)
    viewModelOf(::SkyViewModel)
    viewModelOf(::BackupViewModel)

    // Playlist import
    viewModel {
        val searches = get<SearchRepository>()
        val library = get<LibraryRepository>()
        ImportPlaylistViewModel(
            search = { query -> searches.searchTracks(query).items.map { it.data } },
            createPlaylist = { name ->
                library.createPlaylist(
                    name = name,
                    description = null,
                    isPublic = false,
                    isCollaborating = false,
                    imageBase64 = "",
                    trackIds = emptyList())?.id
            },
            addTracks = library::addTracksToPlaylist,
            defaultName = { getString(Res.string.import_default_name) })
    }

    // Listening history
    singleOf(::ListeningHistoryRepository)
    viewModelOf(::HistoryViewModel)
    single {
        val repository = get<ListeningHistoryRepository>()
        ListeningHistoryRecorder(get(), get(), repository::record, repository::savePosition)
    } withOptions { createdAtStart() }

    // Scrobbling
    single { ScrobbleManager(get(), get(), get()) } withOptions { createdAtStart() }

    factory { (tag: String?) ->
        if (tag != null) {
            Logger.withTag(tag)
        } else {
            // Default logger if no tag is provided
            Logger
        }
    }
}
