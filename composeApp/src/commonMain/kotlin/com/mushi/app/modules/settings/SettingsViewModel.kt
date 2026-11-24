package com.mushi.app.modules.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.mushi.app.core.paths.Paths

interface SettingsProvider {
    val settingsState: StateFlow<UserSettings?>
}

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val paths: Paths
) : ViewModel(), SettingsProvider {
    override val settingsState = repository.userSettings

    /** Where downloads go when no override folder is set. */
    val defaultDownloadFolder: String = paths.getUserDownloadsDirPath()

    fun updateSettings(transform: UserSettings.() -> UserSettings) {
        viewModelScope.launch {
            // Get current state, apply transform, and save
            val currentSettings = settingsState.value ?: return@launch
            val newSettings = currentSettings.transform()
            repository.updateSettings(newSettings)
        }
    }
}