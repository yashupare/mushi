package org.spotube.plugin_interfaces.plugin_apis.lyrics

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

const val LyricsAPI_SERVICE_NAME = "LyricsAPI"

interface LyricsAPI: ZiplineService {

    suspend fun getLyrics(track: MetadataTrack): LyricsResponse?
}