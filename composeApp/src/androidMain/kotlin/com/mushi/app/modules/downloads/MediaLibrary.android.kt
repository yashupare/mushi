package com.mushi.app.modules.downloads

import android.content.Context
import android.media.MediaScannerConnection
import org.koin.core.context.GlobalContext

internal actual fun addToMediaLibrary(path: String) {
    val context = GlobalContext.get().get<Context>()
    MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
}
