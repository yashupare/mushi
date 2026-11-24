package com.mushi.app.core.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

// For providing navigation commands to the NavViewModel from other non-viewmodel classes like WebViewController
class NavigationCommands {
    private val _navigationCommand = Channel<Routes>(capacity = Channel.BUFFERED)
    val navigationCommandFlow = _navigationCommand.receiveAsFlow()

    private val _navigationPopCommand = Channel<Routes?>(capacity = Channel.BUFFERED)
    val navigationPopCommandFlow = _navigationPopCommand.receiveAsFlow()

    fun navigateTo(route: Routes) {
        _navigationCommand.trySend(route)
    }

    // Pops the last route. if route is provided, pops if the current route is the provided route, else pops unconditionally
    fun pop(route: Routes? = null) {
        _navigationPopCommand.trySend(route)
    }
}