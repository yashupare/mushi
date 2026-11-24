package org.spotube.plugin_interfaces.plugin_apis.core

import app.cash.zipline.ZiplineService
import kotlinx.coroutines.flow.StateFlow
import net.swiftzer.semver.SemVer

const val CoreAPI_SERVICE_NAME = "CoreAPI"

interface CoreAPI: ZiplineService {
    suspend fun checkPluginUpdates(currentVersion: SemVer): PluginUpdateInfo?
    // Show a dialog to the user with the given title and message,
    // and return true if they click "OK" or false if they click "Cancel".
    fun supportMarkdownText(currentVersion: SemVer): String

    val requiresAuthentication: Boolean
    val loggedInFlow: StateFlow<Boolean>
    suspend fun login()
    suspend fun logout()
}