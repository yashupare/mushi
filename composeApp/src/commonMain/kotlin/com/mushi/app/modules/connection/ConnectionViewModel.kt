package com.mushi.app.modules.connection

import androidx.lifecycle.ViewModel
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.network.NetworkMonitor
import com.mushi.app.modules.library.LibraryState
import com.mushi.app.modules.library.LibraryTab

/** The network state, and the places that still work without it. */
class ConnectionViewModel(
    monitor: NetworkMonitor,
    private val navigationCommands: NavigationCommands,
    private val libraryState: LibraryState) : ViewModel() {

    val online = monitor.online
    val reconnects = monitor.reconnects

    fun openDownloads() = openLibrary(LibraryTab.Downloads)

    fun openLocalTracks() = openLibrary(LibraryTab.LocalTracks)

    private fun openLibrary(tab: LibraryTab) {
        libraryState.onTabSelected(tab)
        navigationCommands.navigateTo(Routes.Library)
    }
}
