package com.mushi.app.core.zipline

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioAPI
import org.spotube.plugin_interfaces.plugin_apis.core.CoreAPI
import org.spotube.plugin_interfaces.plugin_apis.lyrics.LyricsAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylistAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.search.MetadataSearchAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrackAPI
import org.spotube.plugin_interfaces.plugin_apis.metadata.user.MetadataUserAPI
import org.spotube.plugin_interfaces.plugin_apis.scrobble.ScrobbleAPI
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

class PluginServiceScope(private val registry: Map<KClass<*>, ZiplineService>) {
    val coreAPI: CoreAPI get() = getService()
    val metadataUserAPI: MetadataUserAPI get() = getService()
    val metadataTrackAPI: MetadataTrackAPI get() = getService()
    val metadataAlbumAPI: MetadataAlbumAPI get() = getService()
    val metadataArtistAPI: MetadataArtistAPI get() = getService()
    val metadataPlaylistAPI: MetadataPlaylistAPI get() = getService()
    val metadataBrowseAPI: MetadataBrowseAPI get() = getService()
    val metadataSearchAPI: MetadataSearchAPI get() = getService()
    val audioAPI: AudioAPI get() = getService()
    val lyricsAPI: LyricsAPI get() = getService()
    val scrobbleAPI: ScrobbleAPI get() = getService()

    private inline fun <reified T : ZiplineService> getService(): T {
        return registry[T::class] as? T
            ?: error("${T::class.simpleName} is not initialized or loaded.")
    }
}

interface PluginService {
    val pluginId: String
    val loggedInFlow: StateFlow<Boolean>

    suspend fun start()
    suspend fun stop()
    suspend fun <T> use(block: suspend PluginServiceScope.() -> T): T
}