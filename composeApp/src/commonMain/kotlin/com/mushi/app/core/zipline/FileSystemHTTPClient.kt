package com.mushi.app.core.zipline

import app.cash.zipline.loader.ZiplineHttpClient
import com.mushi.app.core.di.injectLogger
import io.ktor.http.URLBuilder
import io.ktor.http.decodeURLQueryComponent
import okio.ByteString
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.koin.core.component.KoinComponent

class FileSystemHTTPClient(private val baseDir: Path) : ZiplineHttpClient(), KoinComponent {
    private val logger by injectLogger<FileSystemHTTPClient>()
    private val okio = FileSystem.SYSTEM

    override suspend fun download(
        url: String,
        requestHeaders: List<Pair<String, String>>
    ): ByteString {
        try {
            val parsedUrl = URLBuilder(url)
            var fullPath = parsedUrl.encodedParameters["path"]?.decodeURLQueryComponent()?.toPath()

            if (fullPath == null && parsedUrl.encodedPathSegments.isNotEmpty()) {
                fullPath = baseDir / parsedUrl.encodedPathSegments.last()
                logger.d { "Constructed full path from URL path: $fullPath" }
            }

            if (fullPath == null) {
                throw IllegalArgumentException("[FileSystemHTTPClient] Invalid URL: $url. Expected a 'path' query parameter or a simple path in the URL.")
            }

            logger.d { "Reading: $fullPath" }
            return okio.read(fullPath) {
                val str = readByteString()
                // Print the length of the content being read for debugging
                logger.d { "Read ${str.size / 1024.0} KB from $fullPath" }
                // Print the sha256 hash of the content for verification
                val hash = str.sha256().hex()
                logger.d { "SHA-256 hash of content: $hash" }
                str
            }
        } catch (e: Exception) {
            logger.e(e) { "Error reading file for URL: $url. Exception: ${e.message}" }
            throw e
        }
    }
}