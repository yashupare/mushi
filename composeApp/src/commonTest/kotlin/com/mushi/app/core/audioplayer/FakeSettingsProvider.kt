package com.mushi.app.core.audioplayer

import com.mushi.app.modules.settings.SettingsProvider
import com.mushi.app.modules.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsProvider(
    initialSettings: UserSettings? = UserSettings()
) : SettingsProvider {
    private val _settingsState = MutableStateFlow(initialSettings)
    override val settingsState: StateFlow<UserSettings?> = _settingsState.asStateFlow()

    fun updateSettings(transform: UserSettings.() -> UserSettings) {
        _settingsState.value = _settingsState.value?.transform()
    }

    fun setSettings(settings: UserSettings?) {
        _settingsState.value = settings
    }
}
