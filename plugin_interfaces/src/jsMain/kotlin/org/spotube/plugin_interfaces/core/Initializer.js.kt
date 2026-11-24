package org.spotube.plugin_interfaces.core

import app.cash.zipline.Zipline
import org.spotube.plugin_interfaces.core.browser_apis.polyfill

private val zipline by lazy { Zipline.get() }

class InitializerImpl(private val block: suspend () -> Unit) : Initializer {
    override suspend fun initialize() {
        block()
    }
}

actual fun runPluginInitialized(block: suspend () -> Unit) {
    polyfill()
    zipline.bind<Initializer>(Initializer_SERVICE_NAME, InitializerImpl { block() })
}