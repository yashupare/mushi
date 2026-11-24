package com.mushi.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.yushosei.newpipe.extractor.NewPipe
import com.yushosei.newpipe.util.DefaultDownloaderImpl
import com.mushi.app.core.di.initKoin
import com.mushi.app.core.webview.SafariUserAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
    SafariUserAgent.prime()
}) { InitNewPipe { App() } }

@Composable
fun InitNewPipe(
    content: @Composable () -> Unit
) {
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        NewPipe.init(DefaultDownloaderImpl.initDefault())
        isInitialized = true
    }

    if (isInitialized) {
        content()
    }
}