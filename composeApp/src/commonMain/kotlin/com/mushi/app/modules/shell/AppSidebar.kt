package com.mushi.app.modules.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.LocalTextStyle
import org.koin.compose.viewmodel.koinViewModel
import org.spotube.millennium.composables.LocalMillenniumSidebar
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.theme.MillenniumControlState
import org.spotube.millennium.theme.MillenniumTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.NavigationItem
import com.mushi.app.core.navigation.NavigationState
import com.mushi.app.core.navigation.Navigator
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.modules.library.LibraryState
import com.mushi.app.modules.library.LibraryTab
import com.mushi.app.modules.support.LocalSupportPrompt
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxHeart2
import com.mushi.app.resources.iconsax.IconsaxSetting2
import com.mushi.app.resources.iconsax.IconsaxSidebarLeftBroken
import com.mushi.app.resources.iconsax.IconsaxSidebarRightBroken
import com.mushi.app.sidebarTabs
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumSidebar
import org.spotube.millennium.composables.MillenniumSidebarContent
import org.spotube.millennium.composables.MillenniumSidebarFooter
import org.spotube.millennium.composables.MillenniumSidebarGroup
import org.spotube.millennium.composables.MillenniumSidebarGroupContent
import org.spotube.millennium.composables.MillenniumSidebarGroupLabel
import org.spotube.millennium.composables.MillenniumSidebarHeader
import org.spotube.millennium.composables.MillenniumSidebarItem
import org.spotube.millennium.composables.MillenniumSidebarMenu
import org.spotube.millennium.composables.MillenniumSidebarTrigger
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.cookie_regular
import com.mushi.app.generated.resources.settings_screen_title
import com.mushi.app.generated.resources.support_button_label
import com.mushi.app.generated.resources.shell_sidebar_toggle_content_description

@Composable
fun AppSidebar(
    navigator: Navigator,
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
    libraryState: LibraryState = koinInject(),
    connectStatusViewModel: ConnectStatusViewModel = koinViewModel()
) {
    val connectStatus by connectStatusViewModel.uiState.collectAsStateWithLifecycle()
    val remoteStatus = connectStatus.remote?.let { remoteStatusLabel(it) }
    val jamStatus = connectStatus.jam?.let { jamStatusLabel(it) }
    var expanded by rememberSaveable { mutableStateOf(true) }
    val groupExpansion = remember { mutableStateMapOf<String, Boolean>() }
    val currentLibraryTab by libraryState.currentTab.collectAsState()
    val openSupport = LocalSupportPrompt.current

    MillenniumSidebar(
        modifier = modifier,
        collapsed = !expanded,
        shape = RectangleShape,
        onCollapsedChange = { expanded = !it },
    ) {
        MillenniumSidebarHeader {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (expanded) Arrangement.SpaceBetween else Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = "Mushi",
                        style = AppTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily(
                                listOf(Font(Res.font.cookie_regular, weight = FontWeight.Normal))
                            )
                        ),
                        color = AppTheme.colors.onSurface
                    )
                }
                MillenniumSidebarTrigger {
                    MillenniumIconButton(
                        if (it.collapsed) Iconsax.IconsaxSidebarRightBroken else Iconsax.IconsaxSidebarLeftBroken,
                        contentDescription = stringResource(Res.string.shell_sidebar_toggle_content_description),
                        style = MillenniumIconButtonStyle.Quiet,
                        onClick = { it.toggle() }
                    )
                }
            }
        }
        MillenniumSidebarContent(
            contentSpacing = 0.dp
        ) {
            sidebarTabs.forEach { entry ->
                when (entry) {
                    is NavigationItem.Group<*> -> {
                        val isGroupExpanded = groupExpansion[entry.title.key] ?: true
                        MillenniumSidebarGroup(
                            expanded = isGroupExpanded,
                            collapsible = true,
                            groupSpacing = 0.dp,
                            onExpandedChange = { groupExpansion[entry.title.key] = it }) {
                            MillenniumSidebarGroupLabel(stringResource(entry.title))
                            MillenniumSidebarGroupContent(
                                contentSpacing = 0.dp
                            ) {
                                MillenniumSidebarMenu(contentSpacing = 0.dp) {
                                    entry.items.forEach { item ->
                                        val selected =
                                            navigationState.topLevelRoute == item.route &&
                                                    (item.route != Routes.Library || currentLibraryTab == item.data)
                                        SidebarNavItem(
                                            item = item,
                                            selected = selected,
                                            status = when (item.route) {
                                                Routes.Devices -> remoteStatus
                                                Routes.Jam -> jamStatus
                                                else -> null
                                            },
                                            onClick = {
                                                if (item.route == Routes.Library) {
                                                    (item.data as? LibraryTab)?.let {
                                                        libraryState.currentTab.value = it
                                                    }
                                                }
                                                navigator.navigate(item.route)
                                            })
                                    }
                                }
                            }
                        }
                    }

                    is NavigationItem.Tab<*> -> {
                        SidebarNavItem(
                            item = entry,
                            selected = navigationState.topLevelRoute == entry.route,
                            onClick = { navigator.navigate(entry.route) })
                    }
                }
            }
        }
        // Compact the footer chrome only (padding + gap between items); items keep their size.
        MillenniumSidebarFooter(
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            contentSpacing = 2.dp
        ) {
            val supportLabel = stringResource(Res.string.support_button_label)
            MillenniumSidebarItem(
                label = supportLabel,
                onClick = { openSupport() },
                contentDescription = supportLabel,
                icon = Iconsax.IconsaxHeart2
            )
            MillenniumSidebarItem(
                label = stringResource(Res.string.settings_screen_title),
                onClick = { navigator.navigate(Routes.Settings) },
                selected = navigationState.topLevelRoute == Routes.Settings,
                contentDescription = stringResource(Res.string.settings_screen_title),
                icon = com.mushi.app.core.ui.theming.MushiIcons.Tune
            )
        }
    }
}

@Composable
private fun SidebarNavItem(
    item: NavigationItem.Tab<*>,
    selected: Boolean,
    onClick: () -> Unit,
    /** Live status (e.g. "Playing on Krtx"); when set the row gets a dot and a second line. */
    status: String? = null
) {
    val title = stringResource(item.title)
    if (status != null) {
        LiveSidebarNavItem(item, title, status, selected, onClick)
        return
    }
    MillenniumSidebarItem(
        label = title,
        onClick = onClick,
        selected = selected,
        contentDescription = title,
        icon = item.icon
    )
}

@Composable
private fun LiveSidebarNavItem(
    item: NavigationItem.Tab<*>,
    title: String,
    status: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    MillenniumSidebarItem(
        onClick = onClick,
        selected = selected,
        contentDescription = "$title, $status"
    ) {
        // Match the text overload's icon metrics so live rows line up with their siblings.
        val metrics = MillenniumTheme.sidebarTheme.item.resolve(MillenniumControlState.Default)
        val contentColor = LocalTextStyle.current.color
        Box {
            MillenniumIcon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(metrics.iconSize)
            )
            LiveDot(Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-3).dp))
        }
        if (LocalMillenniumSidebar.current?.collapsed != true) {
            Spacer(Modifier.width(metrics.iconLabelSpacing))
            Column(Modifier.weight(1f)) {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = status,
                    style = AppTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
