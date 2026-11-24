package com.mushi.app.core.di

import com.mushi.app.core.audioplayer.AudioPlayer
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.mediacontrol.SystemMediaControlService
import com.mushi.app.core.paths.Paths
import com.mushi.app.core.share.JvmShareService
import com.mushi.app.core.share.ShareService
import com.mushi.app.core.systemtray.SystemTrayService
import com.mushi.app.modules.library.local_tracks.media.JvmLocalMediaDiscoveryService
import com.mushi.app.modules.library.local_tracks.media.LocalMediaDiscoveryService
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import com.mushi.app.core.db.Database
import com.mushi.app.core.window.WindowStateRepository

actual val platformModules = module {
    singleOf(::Paths)
    single { WindowStateRepository(get<Database>()) }
    single<AudioPlayerInterface> { AudioPlayer(Unit) }
    single<LocalMediaDiscoveryService> { JvmLocalMediaDiscoveryService() }
    single<ShareService> { JvmShareService() }
    single { SystemTrayService(get(), get(), get()) }
    single { SystemMediaControlService(get()) } withOptions { createdAtStart() }
}
