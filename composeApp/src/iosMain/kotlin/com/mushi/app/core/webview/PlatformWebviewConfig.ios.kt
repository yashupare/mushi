package com.mushi.app.core.webview

import dev.nucleusframework.webview.web.WebViewState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSHTTPCookieStorage
import platform.Foundation.distantPast
import platform.WebKit.WKWebsiteDataStore
import platform.WebKit.WKWebsiteDataTypeCookies
import platform.WebKit.WKWebsiteDataTypeLocalStorage

actual fun platformWebviewConfig(webView: WebViewState, pluginId: String?) {
    // Applied to the WKWebView itself, so it covers redirects and XHR too, not just the first request.
    SafariUserAgent.value?.let { webView.webSettings.customUserAgentString = it }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun platformClearWebviewData(pluginId: String?) {
    if (pluginId == null) return
    
    val dataStore = WKWebsiteDataStore.defaultDataStore()
    val dataTypes = setOf(WKWebsiteDataTypeCookies, WKWebsiteDataTypeLocalStorage)
    dataStore.removeDataOfTypes(dataTypes, NSDate.distantPast) {}
    
    NSHTTPCookieStorage.sharedHTTPCookieStorage.removeCookiesSinceDate(NSDate.distantPast)
}
actual val webViewCookiesWithoutDomainAreUrlScoped: Boolean = false
