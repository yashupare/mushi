package com.mushi.app.core.paths

import android.content.Context
import android.os.Environment

actual class Paths {
    private val context: Context
        get() = requireNotNull(appContext) { "Paths.init(context) must be called before use" }

    actual fun getApplicationCacheDirPath(): String {
        return context.cacheDir.absolutePath
    }

    actual fun getApplicationDataDirPath(): String {
        return context.filesDir.absolutePath
    }

    actual fun getUserDownloadsDirPath(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + java.io.File.separator + "Mushi"
    }

    actual fun getMusicCacheDirPath(): String {
        return context.cacheDir.absolutePath + "/music_cache"
    }

    companion object {
        @Volatile
        private var appContext: Context? = null

        fun init(context: Context) {
            appContext = context.applicationContext
        }
    }
}