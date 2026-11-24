package com.mushi.app.modules.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushi.app.modules.support.donationOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val repository: WelcomeRepository) : ViewModel() {
    private val currentPageIndex = MutableStateFlow(0)

    val uiState: StateFlow<WelcomeUiState> =
        currentPageIndex.map { index ->
            val pageCount = welcomePrimaryLabels.size
            WelcomeUiState(
                currentPageIndex = index,
                pageCount = pageCount,
                isLastPage = index == pageCount - 1,
                primaryLabel = welcomePrimaryLabels[index.coerceIn(0, pageCount - 1)],
                donations = donationOptions)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = WelcomeUiState(
                    currentPageIndex = 0,
                    pageCount = welcomePrimaryLabels.size,
                    isLastPage = welcomePrimaryLabels.size == 1,
                    primaryLabel = welcomePrimaryLabels.first(),
                    donations = donationOptions))

    fun goToPage(index: Int) {
        currentPageIndex.update { index.coerceIn(0, welcomePrimaryLabels.lastIndex) }
    }

    fun onPrimary() {
        if (currentPageIndex.value == welcomePrimaryLabels.lastIndex) {
            completeOnboarding()
        } else {
            goToPage(currentPageIndex.value + 1)
        }
    }

    fun openDonation(url: String) {
        // Only destinations owned by our static funding list can be opened here.
        if (donationOptions.none { it.url == url }) return
        runCatching { com.mushi.app.openUrlInBrowser(url) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }
}
