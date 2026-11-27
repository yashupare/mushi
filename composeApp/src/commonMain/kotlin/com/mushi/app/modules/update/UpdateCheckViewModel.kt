package com.mushi.app.modules.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class UpdateCheckStatus { Idle, Checking, UpToDate, Failed }

/** The "Check for updates" row in Settings. A found update opens the app's update dialog. */
class UpdateCheckViewModel(private val updates: AppUpdateService) : ViewModel() {
    private val _status = MutableStateFlow(UpdateCheckStatus.Idle)
    val status: StateFlow<UpdateCheckStatus> = _status.asStateFlow()

    fun check() {
        if (_status.value == UpdateCheckStatus.Checking) return
        _status.value = UpdateCheckStatus.Checking
        viewModelScope.launch {
            _status.value = when (updates.checkNow()) {
                UpdateCheckResult.Found -> UpdateCheckStatus.Idle
                UpdateCheckResult.UpToDate -> UpdateCheckStatus.UpToDate
                UpdateCheckResult.Failed -> UpdateCheckStatus.Failed
            }
        }
    }
}
