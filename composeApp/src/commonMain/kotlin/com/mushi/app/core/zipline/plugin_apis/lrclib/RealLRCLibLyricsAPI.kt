package com.mushi.app.core.zipline.plugin_apis.lrclib

import org.koin.core.component.inject
import org.koin.core.component.KoinComponent
import com.mushi.app.core.network.HttpJson
import com.mushi.app.core.network.HttpClientFactory
import org.spotube.plugin_interfaces.plugin_apis.lyrics.LyricsAPI
import org.spotube.plugin_interfaces.plugin_apis.lyrics.LyricsLine
import org.spotube.plugin_interfaces.plugin_apis.lyrics.LyricsResponse
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class LRCLibResponse(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val plainLyrics: String?,
    val syncedLyrics: String?)

class RealLRCLibLyricsAPI : LyricsAPI, KoinComponent {
    private val httpClientFactory: HttpClientFactory by inject()
    private val httpClient by lazy {
        httpClientFactory.create {
            install(ContentNegotiation) {
                json(HttpJson)
            }
            defaultRequest {
                url("https://lrclib.net/api/get")
                // TODO: Use the app's actual version instead of hardcoding it
                userAgent("Mushi/v6.0.0 LRCLib Lyric Plugin")
            }
        }
    }

    private suspend fun getLRCLibLyrics(track: MetadataTrack): LRCLibResponse? =
        withContext(Dispatchers.IO) {
            try {
                httpClient.get {
                    url {
                        parameters.append("track_name", track.title)
                        track.artists.firstOrNull()
                            ?.let { parameters.append("artist_name", it.name) }
                        track.album?.let { parameters.append("album_name", it.title) }
                        if (track.durationMs > 0) parameters.append(
                            "duration",
                            (track.durationMs / 1000).toString()
                        ) // Convert ms to seconds
                    }
                }.body<LRCLibResponse>()
            } catch (e: Exception) {
                null
            }
        }

    override suspend fun getLyrics(track: MetadataTrack): LyricsResponse? {
        val response = getLRCLibLyrics(track) ?: return null
        return LyricsResponse(
            syncedLyrics = response.syncedLyrics?.lines()?.mapNotNull { line ->
                val match = Regex("\\[(\\d{2}):(\\d{2}\\.\\d{2})]").find(line) ?: return@mapNotNull null
                val minutes = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                val seconds = match.groupValues[2].toDoubleOrNull() ?: return@mapNotNull null
                val timeMs = (minutes * 60 * 1000 + (seconds * 1000)).toLong()
                val text = line.substring(match.range.last + 1).trim()
                LyricsLine(time = timeMs, text = text)
            },
            plainLyrics = response.plainLyrics)
    }
}