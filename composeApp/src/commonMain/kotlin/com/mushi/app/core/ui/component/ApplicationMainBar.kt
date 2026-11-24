package com.mushi.app.core.ui.component


import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import androidx.compose.foundation.layout.RowScope

import androidx.compose.runtime.Composable
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.resources.iconsax.ArrowLeft3
import com.mushi.app.resources.iconsax.Iconsax
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@Composable
fun ApplicationBackButton() {
    val navigationCommands: NavigationCommands = koinInject()

    MillenniumIconButton(
        onClick = {
            navigationCommands.pop()
        },
        style = MillenniumIconButtonStyle.Quiet) {
        MillenniumIcon(
            imageVector = Iconsax.ArrowLeft3,
            contentDescription = stringResource(Res.string.common_back)
        )
    }
}

@Composable
expect fun ApplicationMainBar(
    title: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    backButton: Boolean = true,
    transparent: Boolean = false)
