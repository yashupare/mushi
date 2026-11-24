package org.spotube.js_plugin_example.plugin_apis.core

import org.spotube.plugin_interfaces.plugin_apis.core.CoreAPI
import org.spotube.plugin_interfaces.plugin_apis.core.PluginUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RealCoreAPI: CoreAPI {
    override suspend fun checkPluginUpdates(currentVersion: net.swiftzer.semver.SemVer): PluginUpdateInfo? {
        return null
    }

    override fun supportMarkdownText(currentVersion: net.swiftzer.semver.SemVer): String {
        return "Support us please!"
    }

    override val requiresAuthentication = true

    private val stateFlow = MutableStateFlow(false)
    override val loggedInFlow: StateFlow<Boolean> = stateFlow.asStateFlow()

    override suspend fun login() {
        stateFlow.value = true
    }

    override suspend fun logout() {
        stateFlow.value = false
    }

}