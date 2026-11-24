package com.mushi.app.core.ui.component


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.window.WindowDraggableArea

import org.spotube.millennium.composables.MillenniumTopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import com.mushi.app.core.systemtray.SystemTrayService
import com.mushi.app.modules.settings.SettingsProvider
import com.mushi.app.resources.iconsax.FluentDismiss
import com.mushi.app.resources.iconsax.FluentMaximize
import com.mushi.app.resources.iconsax.FluentMinus
import com.mushi.app.resources.iconsax.FluentSquareMultiple
import com.mushi.app.resources.iconsax.Iconsax
import dev.nucleusframework.window.WindowControls
import dev.nucleusframework.window.WindowControlsRenderer
import dev.nucleusframework.window.WindowDoubleClickAction
import dev.nucleusframework.window.windowDragArea
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.app_window_close
import com.mushi.app.generated.resources.app_window_maximize
import com.mushi.app.generated.resources.app_window_minimize
import com.mushi.app.generated.resources.app_window_restore

private val WindowControlsReservedWidth = 132.dp

@Composable
actual fun ApplicationMainBar(
    title: @Composable (() -> Unit),
    subtitle: @Composable () -> Unit,
    actions: @Composable (RowScope.() -> Unit),
    backButton: Boolean,
    transparent: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .windowDragArea(doubleClickAction = WindowDoubleClickAction.ToggleMaximize)
    ) {
        MillenniumTopAppBar(
            title = title,
            actions = {
                actions()
                // Reserve room for the platform window controls instead of shrinking the
                // whole bar, so it spans the full width with no seam under the controls.
                Spacer(Modifier.width(WindowControlsReservedWidth))
            },
            navigationIcon = {
                if (backButton) {
                    ApplicationBackButton()
                }
            },
            subtitle = subtitle,
            background = if (transparent) SolidColor(Color.Transparent) else null,
            separatorColor = if (transparent) Color.Transparent else null,
            modifier = Modifier.fillMaxWidth()
        )
        LocalWindowScope.current.WindowControls(
            modifier = Modifier.align(Alignment.TopEnd).heightIn(max = 48.dp),
            renderer = WindowControlsRenderer.Platform, // default
        )
    }
}

@Composable
private fun WindowButtons(modifier: Modifier = Modifier) {
    val windowState = LocalWindowState.current
    val applicationScope = LocalApplicationScope.current
    val settingsProvider = koinInject<SettingsProvider>()
    val trayService = koinInject<SystemTrayService>()
    val settings by settingsProvider.settingsState.collectAsState(initial = null)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MillenniumIconButton(
            onClick = { windowState.isMinimized = true },
            style = MillenniumIconButtonStyle.Quiet) {
            MillenniumIcon(Iconsax.FluentMinus, stringResource(Res.string.app_window_minimize), modifier = Modifier.size(14.dp))
        }
        MillenniumIconButton(
            onClick = {
                windowState.placement = if (windowState.placement == WindowPlacement.Maximized)
                    WindowPlacement.Floating else WindowPlacement.Maximized
            },
            style = MillenniumIconButtonStyle.Quiet) {
            if (windowState.placement == WindowPlacement.Floating) {
                MillenniumIcon(Iconsax.FluentMaximize, stringResource(Res.string.app_window_maximize), modifier = Modifier.size(14.dp))
            } else {
                MillenniumIcon(Iconsax.FluentSquareMultiple, stringResource(Res.string.app_window_restore), modifier = Modifier.size(14.dp))
            }
        }
        MillenniumIconButton(
            onClick = {
                if (settings?.minimizeToTray == true) {
                    trayService.hideWindow()
                } else {
                    applicationScope.exitApplication()
                }
            },
            style = MillenniumIconButtonStyle.Quiet) {
            MillenniumIcon(Iconsax.FluentDismiss, stringResource(Res.string.app_window_close), modifier = Modifier.size(14.dp))
        }
    }
}
