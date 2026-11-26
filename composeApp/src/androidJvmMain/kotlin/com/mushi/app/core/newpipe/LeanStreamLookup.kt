package com.mushi.app.core.newpipe

import co.touchlab.kermit.Logger
import org.schabi.newpipe.extractor.downloader.Response

/**
 * NewPipe's stream extractor also downloads YouTube's `/next` page (related videos, comments and
 * the like). Playback needs none of it, yet it took about half of every lookup. While [run] is
 * fetching, [NewPipeDownloader] answers that one request with an empty page instead.
 *
 * This leans on how the extractor behaves inside, so any lean lookup that throws or comes back
 * unusable is repeated in full.
 */
internal object LeanStreamLookup {
    private val logger = Logger.withTag("LeanStreamLookup")

    // Per thread: the extractor downloads on the thread that called it, and other lookups
    // running at the same time must not be affected.
    private val skipping = ThreadLocal<Boolean>()

    fun <T> run(isUsable: (T) -> Boolean, fetch: () -> T): T {
        val lean = try {
            skipping.set(true)
            Result.success(fetch())
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            skipping.remove()
        }
        lean.onSuccess { if (isUsable(it)) return it }
        logger.w(lean.exceptionOrNull()) { "Lean stream lookup was not usable; repeating it in full" }
        return fetch()
    }

    /** The stand-in response for [url], or null when it has to be downloaded. */
    fun standInFor(url: String): Response? {
        if (skipping.get() != true || !url.contains("/youtubei/v1/next")) return null
        return Response(200, "OK", mapOf("content-type" to listOf("application/json")), EMPTY_PAGE, url)
    }

    // The extractor rejects suspiciously short JSON bodies, hence the padding.
    private val EMPTY_PAGE = """{"responseContext":{},"contents":{},"padding":"${"x".repeat(100)}"}"""
}
