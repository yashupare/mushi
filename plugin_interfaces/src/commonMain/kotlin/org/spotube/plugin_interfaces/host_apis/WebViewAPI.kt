package org.spotube.plugin_interfaces.host_apis

import app.cash.zipline.ZiplineService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

const val WebViewAPI_SERVICE_NAME = "WebViewAPI"

@Serializable
data class Cookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String? = null,
    val expiresAt: Long? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false
)
/**
 * Every call works only on the WebView this plugin opened. Reading cookies and running scripts
 * are limited to the plugin's declared `cookieDomains`; see [getCookies] and [evaluateJavaScript].
 */
interface WebViewAPI: ZiplineService {
    /** Opens [url], which must be an absolute http(s) URL (no `javascript:`, `data:`, `file:`...). */
    fun navigateTo(url: String)
    fun navigateToHTML(html: String)
    /**
     * Cookies the WebView would send to [url]. Allowed only for http(s) URLs whose host matches
     * one of the plugin's `cookieDomains` in `plugin.json` (`example.com` matches that host
     * exactly, `*.example.com` matches its subdomains but not `example.com` itself; wildcards
     * over shared suffixes from https://publicsuffix.org, like `*.co.uk`, aren't accepted).
     *
     * @throws InsufficientPermissionException (bridged; see
     *   [InsufficientPermissionException.from]) when the host isn't declared.
     */
    suspend fun getCookies(url: String): List<Cookie>
    /**
     * Runs [script] in the current page, but only if that page's host matches the plugin's
     * `cookieDomains`, or it is the plugin's own HTML from [navigateToHTML]. The page is checked
     * when the script actually runs, so a navigation in between can't slip it onto another site.
     *
     * The host prepends a one-statement check to [script], so a `"use strict"` directive at its
     * top no longer applies; wrap strict code in a function instead. A script whose result
     * doesn't come back within 15 seconds yields `null`.
     *
     * @throws InsufficientPermissionException (bridged) with [PluginPermission.WEBVIEW_SCRIPT]
     *   when the page isn't allowed.
     */
    suspend fun evaluateJavaScript(script: String): String?
    fun urlChangeFlow(): Flow<String>
    fun webviewCreatedFlow(): Flow<Unit>
    fun postMessagesFlow(): Flow<String>
    /** Closes the WebView if this plugin opened it; otherwise does nothing. */
    fun exitWebView()
}