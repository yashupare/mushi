package org.spotube.plugin_interfaces.plugin_apis.core

import kotlinx.serialization.Serializable

@Serializable
data class PluginUpdateInfo(
    val latestVersion: String,
    val directDownloadUrl: String,
    val releaseNotes: String
)