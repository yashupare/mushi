package org.spotube.plugin_interfaces.core

import app.cash.zipline.ZiplineService

const val Initializer_SERVICE_NAME = "Initializer"

interface Initializer : ZiplineService {
    suspend fun initialize()
}

expect fun runPluginInitialized(block: suspend () -> Unit)