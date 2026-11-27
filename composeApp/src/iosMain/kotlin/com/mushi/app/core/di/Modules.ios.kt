package com.mushi.app.core.di

import com.mushi.app.core.audioplayer.AudioPlayer
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.network.AlwaysOnlineMonitor
import com.mushi.app.core.network.NetworkMonitor
import com.mushi.app.core.paths.Paths
import com.mushi.app.modules.update.AppInstaller
import com.mushi.app.modules.update.NoAppInstaller
import com.mushi.app.core.share.IosShareService
import com.mushi.app.core.share.ShareService
import com.mushi.app.core.webview.WebViewController
import com.mushi.app.modules.library.local_tracks.media.IosLocalMediaDiscoveryService
import com.mushi.app.modules.library.local_tracks.media.LocalMediaDiscoveryService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModules = module {
    singleOf(::Paths)
    single<AppInstaller> { NoAppInstaller() }
    single<NetworkMonitor> { AlwaysOnlineMonitor() }
    singleOf(::WebViewController)
    single<AudioPlayerInterface> { AudioPlayer(Unit) }
    single<LocalMediaDiscoveryService> { IosLocalMediaDiscoveryService() }
    single<ShareService> { IosShareService() }
}
