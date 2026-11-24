package com.mushi.app.modules.welcome

import com.mushi.app.modules.support.DonationOption
import org.jetbrains.compose.resources.StringResource

data class WelcomeUiState(
    val currentPageIndex: Int,
    val pageCount: Int,
    val isLastPage: Boolean,
    val primaryLabel: StringResource,
    val donations: List<DonationOption>)
