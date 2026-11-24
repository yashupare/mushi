package com.mushi.app.modules.plugin

import org.jetbrains.compose.resources.DrawableResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.plugin_logo_lrclib
import com.mushi.app.generated.resources.plugin_logo_newpipe

val NEWPIPE_YOUTUBE_BUILT_IN_PLUGIN = PluginEntry(
    name = "NewPipe YouTube",
    version = "0.1.0",
    apiVersion = PLUGIN_API_VERSION,
    description = "NewPipe's YouTube plugin for fetching audio streams.",
    author = "Mushi Team",
    capabilities = listOf(
        PluginCapability.NETWORK_REQUESTS,
        PluginCapability.PERSISTENT_STORAGE
    ),
    abilities = listOf(PluginAbility.AUDIO),
    contact = "",
    repository = "",
    bugs = "",
    license = "")
val LRCLIB_BUILT_IN_PLUGIN = PluginEntry(
    name = "LRCLib Lyrics",
    version = "0.1.0",
    apiVersion = PLUGIN_API_VERSION,
    description = "LRCLib Lyrics plugin for fetching lyrics.",
    author = "Mushi Team",
    capabilities = listOf(
        PluginCapability.NETWORK_REQUESTS),
    abilities = listOf(PluginAbility.LYRICS),
    contact = "",
    repository = "",
    bugs = "",
    license = "")
val BUILT_IN_PLUGINS = listOf(
    NEWPIPE_YOUTUBE_BUILT_IN_PLUGIN,
    LRCLIB_BUILT_IN_PLUGIN)

/**
 * The bundled placeholder logo for a built-in plugin, or `null` if it has none.
 *
 * Built-in plugins are not installed to disk, so they have no `logo.png`; these
 * resources (in `composeResources/drawable`) stand in as their logos.
 */
fun builtInPluginLogo(plugin: PluginEntry): DrawableResource? = when (plugin.id) {
    NEWPIPE_YOUTUBE_BUILT_IN_PLUGIN.id -> Res.drawable.plugin_logo_newpipe
    LRCLIB_BUILT_IN_PLUGIN.id -> Res.drawable.plugin_logo_lrclib
    else -> null
}

private val BUILT_IN_PLUGIN_IDS = BUILT_IN_PLUGINS.mapTo(HashSet()) { it.id }

/**
 * Whether this is one of [BUILT_IN_PLUGINS]. Matches by [PluginEntry.id], not by equality:
 * an entry saved by an older app version can differ from the current built-in definition
 * (version, description, new fields) and must still count as built-in.
 */
val PluginEntry.isBuiltIn: Boolean
    get() = id in BUILT_IN_PLUGIN_IDS
