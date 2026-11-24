package com.mushi.app

import android.app.Application
import com.mushi.app.core.di.initKoin
import com.mushi.app.core.paths.Paths
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
    }

    override fun onTerminate() {
        appScope.cancel()
        super.onTerminate()
    }
}