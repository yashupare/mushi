package com.mushi.app.core.zipline

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual fun createZiplineDispatcher(): ZiplineDispatcher {
    val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(null, runnable, "Zipline", 8L * 1024 * 1024) // 8 MiB stack for QuickJS compile()
    }
    val dispatcher = executor.asCoroutineDispatcher()
    return ZiplineDispatcher(dispatcher) {
        dispatcher.close()
        executor.shutdown()
    }
}

