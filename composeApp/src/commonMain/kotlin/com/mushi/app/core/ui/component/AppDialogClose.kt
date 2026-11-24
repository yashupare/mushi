package com.mushi.app.core.ui.component

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import com.mushi.app.resources.iconsax.FluentDismiss
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.common_close_dialog

/**
 * The close control for dialogs: a plain quiet icon button. Replaces the library's own dialog
 * close, which draws a glossy red square that doesn't belong in the flat design.
 */
@Composable
fun AppDialogClose(onClick: () -> Unit) {
    MillenniumIconButton(onClick = onClick, style = MillenniumIconButtonStyle.Quiet) {
        MillenniumIcon(
            imageVector = Iconsax.FluentDismiss,
            contentDescription = stringResource(Res.string.common_close_dialog))
    }
}
