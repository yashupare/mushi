package com.mushi.app.core.webview

import com.mushi.app.core.paths.Paths
import dev.nucleusframework.webview.web.WebViewState
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.context.GlobalContext

actual fun platformWebviewConfig(webView: WebViewState, pluginId: String?) {
    val paths = GlobalContext.get().get<Paths>()

    val baseDir = "${paths.getApplicationCacheDirPath()}/webview_data".toPath()
    val dataDir = if (pluginId != null) baseDir / pluginId else baseDir
    webView.webSettings.desktopWebSettings.dataDirectory = dataDir.toString()
}

actual suspend fun platformClearWebviewData(pluginId: String?) {
    if (pluginId == null) return
    val paths = GlobalContext.get().get<Paths>()
    val dataDirStr = "${paths.getApplicationCacheDirPath()}/webview_data/$pluginId"
    val dataDir = dataDirStr.toPath()
    if (FileSystem.SYSTEM.exists(dataDir)) {
        FileSystem.SYSTEM.deleteRecursively(dataDir)
    }
}
actual val webViewCookiesWithoutDomainAreUrlScoped: Boolean = false
