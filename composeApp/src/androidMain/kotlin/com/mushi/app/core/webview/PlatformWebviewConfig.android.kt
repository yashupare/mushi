package com.mushi.app.core.webview

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import dev.nucleusframework.webview.web.WebViewState

actual fun platformWebviewConfig(webView: WebViewState, pluginId: String?) {
    val nativeWebView = webView.webView?.nativeWebView as? WebView ?: return
    nativeWebView.settings.domStorageEnabled = true
}

actual suspend fun platformClearWebviewData(pluginId: String?) {
    if (pluginId == null) return
    
    CookieManager.getInstance().removeAllCookies(null)
    CookieManager.getInstance().flush()
    WebStorage.getInstance().deleteAllData()
}
actual val webViewCookiesWithoutDomainAreUrlScoped: Boolean = true
