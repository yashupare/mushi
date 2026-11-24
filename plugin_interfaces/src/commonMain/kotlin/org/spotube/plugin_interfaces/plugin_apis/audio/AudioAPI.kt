package org.spotube.plugin_interfaces.plugin_apis.audio

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

const val AudioAPI_SERVICE_NAME = "AudioAPI"

interface AudioAPI: ZiplineService {
    val supportedQualities: List<AudioFormat>
    suspend fun getStreamsByTrack(track: MetadataTrack): List<AudioSource>
    suspend fun getStreamsOfAudioSource(source: AudioSource.Basic): List<AudioSource.Streamed>
}