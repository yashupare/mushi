package com.mushi.app

import android.app.Application
import com.mushi.app.core.audioplayer.AudioPlayer
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.di.initKoin
import com.mushi.app.media.AudioEffects
import com.mushi.app.modules.settings.UserSettingsSource
import org.koin.core.component.get
import com.mushi.app.core.paths.Paths
import com.mushi.app.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent

class MyApplication : Application(), KoinComponent {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Paths.init(this)
        initKoin {
            androidContext(this@MyApplication)
        }
        WidgetUpdater.start(this)
        // onCreate is on the main thread, which the player belongs to
        AudioEffects(get<AudioPlayerInterface>() as AudioPlayer, get<UserSettingsSource>()).start()
    }

    override fun onTerminate() {
        appScope.cancel()
        super.onTerminate()
    }
}