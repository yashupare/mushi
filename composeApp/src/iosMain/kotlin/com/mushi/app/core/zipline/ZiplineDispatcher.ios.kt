package com.mushi.app.core.zipline

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSThread
import kotlin.coroutines.ContinuationInterceptor

// Same as JVM/Android. Zipline lets QuickJS recurse up to ~6 MiB of native stack, but
// iOS gives secondary threads (incl. newSingleThreadContext's) only 512 KiB, so loading
// a plugin overflowed into the guard page and crashed the app with SIGBUS.
private const val ZIPLINE_THREAD_STACK_SIZE: ULong = 8_388_608u // 8 MiB

actual fun createZiplineDispatcher(): ZiplineDispatcher {
    val dispatcher = CompletableDeferred<CoroutineDispatcher>()
    val stopped = CompletableDeferred<Unit>()
    // NSThread is the only way to choose a thread's stack size here; it hosts an event
    // loop (runBlocking) whose dispatcher accepts work from any thread.
    val thread = NSThread {
        runBlocking {
            dispatcher.complete(coroutineContext[ContinuationInterceptor] as CoroutineDispatcher)
            stopped.await()
        }
    }
    thread.name = "Zipline"
    thread.stackSize = ZIPLINE_THREAD_STACK_SIZE
    thread.start()
    return ZiplineDispatcher(runBlocking { dispatcher.await() }) {
        stopped.complete(Unit)
    }
}
