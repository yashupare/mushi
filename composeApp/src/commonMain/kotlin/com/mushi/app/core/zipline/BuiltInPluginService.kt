package com.mushi.app.core.zipline

import app.cash.zipline.ZiplineService
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioAPI
import org.spotube.plugin_interfaces.plugin_apis.core.CoreAPI
import org.spotube.plugin_interfaces.plugin_apis.lyrics.LyricsAPI
import com.mushi.app.core.zipline.plugin_apis.common.RealCoreAPI
import com.mushi.app.core.zipline.plugin_apis.lrclib.RealLRCLibLyricsAPI
import com.mushi.app.core.zipline.plugin_apis.newpipe_yt.RealNewPipeAudioAPI
import com.mushi.app.modules.plugin.LRCLIB_BUILT_IN_PLUGIN
import com.mushi.app.modules.plugin.NEWPIPE_YOUTUBE_BUILT_IN_PLUGIN
import com.mushi.app.modules.plugin.PluginEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

class BuiltInPluginService(
    private val pluginInfo: PluginEntry) : PluginService, KoinComponent {

    override val pluginId: String = pluginInfo.id
    private val loggedInStateFlow = MutableStateFlow(false)
    override val loggedInFlow = loggedInStateFlow.asStateFlow()
    val servicesRegistry = mutableMapOf<KClass<*>, ZiplineService>()

//    val webViewController: WebViewController by inject()
//    val persistedStorage by lazy { RealPersistedStorageAPI(pluginInfo) }

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun runLogInFlowObservers() = scope.launch {
        val coreAPI = servicesRegistry[CoreAPI::class] as CoreAPI?
        coreAPI?.loggedInFlow?.collect { isLoggedIn ->
            loggedInStateFlow.value = isLoggedIn
        }
    }

    override suspend fun start() {
        when (pluginInfo) {
            NEWPIPE_YOUTUBE_BUILT_IN_PLUGIN -> {
                servicesRegistry[CoreAPI::class] = RealCoreAPI()
                servicesRegistry[AudioAPI::class] = RealNewPipeAudioAPI()
            }

            LRCLIB_BUILT_IN_PLUGIN -> {
                servicesRegistry[CoreAPI::class] = RealCoreAPI()
                servicesRegistry[LyricsAPI::class] = RealLRCLibLyricsAPI()
            }
        }
        runLogInFlowObservers()
    }

    override suspend fun stop() {
        for (service in servicesRegistry.values) {
            service.close()
        }
        servicesRegistry.clear()
    }

    override suspend fun <T> use(block: suspend PluginServiceScope.() -> T): T {
        //Since built-in plugins don't require any special setup, we can just execute the block with an empty scope
        return block(PluginServiceScope(servicesRegistry))
    }

}