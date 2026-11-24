package com.mushi.app.modules.webview

import androidx.compose.runtime.Composable
import com.mushi.app.core.di.rememberLogger
import com.mushi.app.core.webview.PlatformWebViewScreen
import com.mushi.app.core.webview.WebViewController

internal object WebViewScreen
@Composable
fun WebViewScreen(controller: WebViewController) {
    val logger = rememberLogger<WebViewScreen>()
    logger.d("WebViewScreen Opened")
    return PlatformWebViewScreen(webViewController = controller)
}