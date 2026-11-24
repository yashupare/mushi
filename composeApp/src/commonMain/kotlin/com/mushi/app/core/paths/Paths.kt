package com.mushi.app.core.paths

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Paths() {
    fun getApplicationCacheDirPath(): String
    fun getApplicationDataDirPath(): String
    fun getUserDownloadsDirPath(): String
    fun getMusicCacheDirPath(): String
}