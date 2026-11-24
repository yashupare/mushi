package com.mushi.app.core.webview

import dev.nucleusframework.webview.web.WebViewState

expect fun platformWebviewConfig(webView: WebViewState, pluginId: String?)

expect suspend fun platformClearWebviewData(pluginId: String?)

/**
 * Whether this platform's WebView cookie store returns cookies without their domain, and only
 * the ones it would send to the URL asked for. True on Android (`CookieManager.getCookie(url)`
 * gives a bare `name=value` header). Elsewhere every cookie carries its domain.
 */
expect val webViewCookiesWithoutDomainAreUrlScoped: Boolean
