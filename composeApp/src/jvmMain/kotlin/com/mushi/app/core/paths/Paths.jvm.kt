package com.mushi.app.core.paths

import net.harawata.appdirs.AppDirsFactory

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Paths {
    actual fun getApplicationCacheDirPath(): String {
        return AppDirsFactory.getInstance().getUserCacheDir("Mushi", null, "Mushi")
    }

    actual fun getApplicationDataDirPath(): String {
        return AppDirsFactory.getInstance().getUserDataDir("Mushi", null, "Mushi")
    }

    actual fun getUserDownloadsDirPath(): String {
        return AppDirsFactory.getInstance().getUserDownloadsDir("Mushi", null, null)
    }

    actual fun getMusicCacheDirPath(): String {
        return AppDirsFactory.getInstance().getUserCacheDir("Mushi", null, "Mushi") + "/music_cache"
    }
}