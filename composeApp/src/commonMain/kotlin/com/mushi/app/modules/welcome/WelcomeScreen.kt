package com.mushi.app.modules.welcome


import com.mushi.app.core.ui.component.MushiButton
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumCard
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumSeparator
import org.spotube.millennium.composables.MillenniumTabGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushi.app.core.ui.component.ApplicationMainBar
import com.mushi.app.modules.welcome.pages.BrandingPage
import com.mushi.app.modules.welcome.pages.DonationPage
import com.mushi.app.modules.welcome.pages.HowItWorksPage
import com.mushi.app.modules.welcome.pages.PluginsPage
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxCheckCircle
import com.mushi.app.resources.iconsax.IconsaxNext
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.welcome_skip

/**
 * First-run Welcome/Onboarding screen. It is a route
 * ([com.mushi.app.core.navigation.Routes.Welcome]) rendered inside the app shell.
 *
 * The steps are exposed through a Millennium tab group rather than ad-hoc dots or a footer
 * walk-through, so the screen reads as one deliberate panel on every window size.
 * Each step owns its content and the footer only carries the forward action.
 */
/** Below this width the welcome card trims its margins and step tabs. */
private val WelcomeCompactWidth = 480.dp

@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WelcomeTheme {
        Box(Modifier.fillMaxSize()) {
            WelcomeBackground()
            Column(modifier = Modifier.fillMaxSize()) {
                ApplicationMainBar(
                    title = {
                        WelcomeWordmark(
                            fontSize = AppTheme.typography.titleLarge.fontSize)
                    },
                    backButton = false,
                    transparent = true,
                    actions = {
                        if (!state.isLastPage) {
                            MillenniumButton(
                                onClick = viewModel::completeOnboarding,
                                style = MillenniumButtonStyle.Quiet) {
                                Text(
                                    text = stringResource(Res.string.welcome_skip),
                                    color = AppTheme.colors.onSurfaceVariant)
                            }
                        }
                    })

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center) {
                    // Phones get tighter margins so the four step tabs keep whole words.
                    val compact = maxWidth < WelcomeCompactWidth
                    val outerPadding = if (compact) 8.dp else 16.dp
                    MillenniumCard(
                        modifier = Modifier
                            .padding(outerPadding)
                            .widthIn(max = 1040.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(0.dp)) {
                        Column(Modifier.fillMaxSize()) {
                            WelcomeStepSwitcher(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(if (compact) 8.dp else 12.dp),
                                state = state,
                                compact = compact,
                                viewModel = viewModel)
                            MillenniumSeparator()
                            WelcomeActionBar(
                                state = state,
                                viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStepSwitcher(
    state: WelcomeUiState,
    compact: Boolean,
    viewModel: WelcomeViewModel,
    modifier: Modifier = Modifier) {
    val labels = welcomeStepLabels.map { stringResource(it) }
    val pagePadding = if (compact) 12.dp else 32.dp
    MillenniumTabGroup(
        listInnerSpacing = if (compact) 2.dp else null,
        tabPadding = if (compact) PaddingValues(vertical = 8.dp, horizontal = 4.dp) else null,
        tabTextStyle = if (compact) TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold) else null,
        selectedTab = state.currentPageIndex,
        onSelectedTabChange = viewModel::goToPage,
        tabs = labels.indices.toList(),
        modifier = modifier,
        tabLabel = { labels[it] }) { index ->
        when (index) {
            0 -> BrandingPage(contentPadding = pagePadding)

            1 -> DonationPage(
                donations = state.donations,
                onDonate = viewModel::openDonation,
                contentPadding = pagePadding)

            2 -> PluginsPage()

            else -> HowItWorksPage(contentPadding = pagePadding)
        }
    }
}

@Composable
private fun WelcomeActionBar(
    state: WelcomeUiState,
    viewModel: WelcomeViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f))
        // The main action on the page is the accent button, not a grey one.
        MushiButton(
            onClick = viewModel::onPrimary) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(state.primaryLabel),
                    fontWeight = FontWeight.SemiBold)
                MillenniumIcon(
                    imageVector = if (state.isLastPage) {
                        Iconsax.IconsaxCheckCircle
                    } else {
                        com.mushi.app.core.ui.theming.MushiIcons.SkipNext
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp))
            }
        }
    }
}
