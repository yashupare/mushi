package com.mushi.app.core.zipline.plugin_apis.common

import org.spotube.plugin_interfaces.plugin_apis.core.CoreAPI
import org.spotube.plugin_interfaces.plugin_apis.core.PluginUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.swiftzer.semver.SemVer

class RealCoreAPI : CoreAPI {
    override suspend fun checkPluginUpdates(currentVersion: SemVer): PluginUpdateInfo? {
        return null
    }

    override fun supportMarkdownText(currentVersion: SemVer): String {
        return "Thanks for using Mushi!"
    }

    override val requiresAuthentication = false

    private val loggedInState = MutableStateFlow(false)
    override val loggedInFlow = loggedInState.asStateFlow()

    override suspend fun login() {
        // No-op
    }

    override suspend fun logout() {
        // No-op
    }
}