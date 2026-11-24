package org.spotube.plugin_interfaces.plugin_apis.scrobble

import app.cash.zipline.ZiplineService

const val ScrobbleAPI_SERVICE_NAME = "ScrobbleAPI"

interface ScrobbleAPI: ZiplineService {
    suspend fun scrobble(track: ScrobbleTrack)
}