package com.mushi.app.core.paths

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSDownloadsDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class Paths {
    actual fun getApplicationCacheDirPath(): String {
        // Return ios cache dir
        val cacheDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSCachesDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null)

        return requireNotNull(cacheDirectory?.path)
    }

    actual fun getApplicationDataDirPath(): String {
        // Return ios document dir
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null)

        return requireNotNull(documentDirectory?.path)
    }

    actual fun getUserDownloadsDirPath(): String {
        val downloadsDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDownloadsDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null)

        return (downloadsDirectory?.path ?: getApplicationDataDirPath()) + "/Mushi"
    }

    actual fun getMusicCacheDirPath(): String {
        return getApplicationCacheDirPath() + "/music_cache"
    }
}