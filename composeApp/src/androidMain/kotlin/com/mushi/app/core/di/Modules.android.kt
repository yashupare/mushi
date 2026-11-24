package com.mushi.app.core.di

import android.content.Context
import com.mushi.app.core.audioplayer.AudioPlayer
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.paths.Paths
import com.mushi.app.core.share.AndroidShareService
import com.mushi.app.core.share.ShareService
import com.mushi.app.media.MediaBrowseHelper
import com.mushi.app.modules.library.local_tracks.media.AndroidLocalMediaDiscoveryService
import com.mushi.app.modules.library.local_tracks.media.LocalMediaDiscoveryService
import org.koin.dsl.module

actual val platformModules = module {
    single { Paths() }
    single<AudioPlayerInterface> { AudioPlayer(get<Context>()) }
    single<LocalMediaDiscoveryService> { AndroidLocalMediaDiscoveryService(get()) }
    single<ShareService> { AndroidShareService(get()) }
    single {
        MediaBrowseHelper(
            pluginManager = get(),
            homeScreenRepository = get(),
            libraryRepository = get(),
            playlistRepository = get(),
            albumRepository = get(),
            savedTracksRepository = get(),
            searchRepository = get(),
            collectionPlaybackHelper = get(),
            audioPlayerQueue = get(),
            settingsRepository = get(),
            blacklistRepository = get())
    }
}
