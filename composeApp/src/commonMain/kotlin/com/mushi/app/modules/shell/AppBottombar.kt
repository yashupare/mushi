package com.mushi.app.modules.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import com.composeunstyled.Text
import org.spotube.millennium.composables.MillenniumIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.Composable
import com.mushi.app.core.ui.pressScale
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.NavigationItem
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.bottombarTabs
import com.mushi.app.core.navigation.NavigationState
import com.mushi.app.core.navigation.Navigator
import com.mushi.app.core.navigation.Routes
import com.mushi.app.modules.downloads.DownloadBadgeIndicator

@Composable
fun AppBottombar(
    navigator: Navigator,
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
    libraryBadge: @Composable () -> Unit = { DownloadBadgeIndicator() }
) {
    val haptics = com.mushi.app.core.ui.rememberHaptics()

    // Flat bar: no hairline, pills or glow. The active tab is the accent colour and
    // semibold; the others are muted. The surface runs under the gesture bar, the tabs don't.
    Column(
        modifier = modifier
            .background(AppTheme.colors.surfaceContainer)
    ) {
        // No hairline on top: the mini player sits directly above, and its own colour separates it.
        val tabs = bottombarTabs.filterIsInstance<NavigationItem.Tab<*>>()
        val selectedIndex = tabs.indexOfFirst { navigationState.topLevelRoute == it.route }
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp)
        ) {
            // A soft pill behind the active icon that glides between tabs on a spring.
            val cellWidth = maxWidth / tabs.size.coerceAtLeast(1)
            val indicatorX by androidx.compose.animation.core.animateDpAsState(
                targetValue = cellWidth * selectedIndex.coerceAtLeast(0) + (cellWidth - TabIndicatorWidth) / 2,
                animationSpec = com.mushi.app.core.ui.motionOrSnap(com.mushi.app.core.ui.MushiMotion.spatialDefault()),
                label = "tab_indicator")
            if (selectedIndex >= 0) {
                Box(
                    Modifier
                        .offset(x = indicatorX, y = TabIndicatorTop)
                        .size(width = TabIndicatorWidth, height = TabIndicatorHeight)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .background(AppTheme.colors.primary.copy(alpha = 0.16f)))
            }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs
                .forEach { tab ->
                    val selected = navigationState.topLevelRoute == tab.route
                    val title = stringResource(tab.title)
                    val color by androidx.compose.animation.animateColorAsState(
                        targetValue = if (selected) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                        animationSpec = com.mushi.app.core.ui.MushiMotion.effectsDefault(),
                        label = "tab_color")
                    val iconScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1f,
                        animationSpec = com.mushi.app.core.ui.MushiMotion.spatialFast(),
                        label = "tab_icon_scale")
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pressScale(0.9f)
                            .clickable {
                                if (!selected) haptics.tick()
                                navigator.navigate(tab.route)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box {
                            MillenniumIcon(
                                imageVector = if (selected) tab.selectedIcon else tab.icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    },
                                tint = color
                            )
                            if (tab.route == Routes.Library) {
                                Box(modifier = Modifier.align(Alignment.TopEnd)) { libraryBadge() }
                            }
                        }
                        Text(
                            text = title,
                            style = AppTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = color,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
        }
        }
    }
}

/** Size and vertical position of the active-tab pill; it sits behind the 24 dp icon. */
private val TabIndicatorWidth = 58.dp
private val TabIndicatorHeight = 30.dp
private val TabIndicatorTop = 6.5.dp
