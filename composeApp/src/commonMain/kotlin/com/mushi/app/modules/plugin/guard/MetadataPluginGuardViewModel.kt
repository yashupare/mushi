package com.mushi.app.modules.plugin.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.modules.library.LibraryState
import com.mushi.app.modules.library.LibraryTab
import com.mushi.app.modules.plugin.PluginAbility
import com.mushi.app.modules.plugin.PluginManager
import com.mushi.app.modules.plugin.PluginManagerStates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface MetadataPluginGuardState {
    data object Loading : MetadataPluginGuardState
    data object Available : MetadataPluginGuardState

    /** No metadata plugin is selected. [hasInstalledPlugins] tells whether the user only needs
     * to pick one (true) or has to install one first (false). */
    data class Missing(val hasInstalledPlugins: Boolean) : MetadataPluginGuardState
}

class MetadataPluginGuardViewModel(
    pluginManager: PluginManager,
    private val navigationCommands: NavigationCommands,
    private val libraryState: LibraryState
) : ViewModel() {
    val uiState: StateFlow<MetadataPluginGuardState> = pluginManager.state
        .map { state ->
            when (state) {
                is PluginManagerStates.Loading -> MetadataPluginGuardState.Loading
                is PluginManagerStates.Data ->
                    if (PluginAbility.METADATA in state.selectedPlugins) {
                        MetadataPluginGuardState.Available
                    } else {
                        MetadataPluginGuardState.Missing(
                            hasInstalledPlugins = state.plugins.any { PluginAbility.METADATA in it.abilities })
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MetadataPluginGuardState.Loading)

    fun openPlugins() {
        navigationCommands.navigateTo(Routes.Plugins)
    }

    /** Local tracks are a plugin-free player, so offer them as the way forward. */
    fun openLocalTracks() {
        libraryState.onTabSelected(LibraryTab.LocalTracks)
        navigationCommands.navigateTo(Routes.Library)
    }
}
