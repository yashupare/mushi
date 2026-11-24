package com.mushi.app.core.ui.component


import org.spotube.millennium.composables.MillenniumDialog
import org.spotube.millennium.composables.MillenniumDialogBody
import org.spotube.millennium.composables.MillenniumDialogFooter
import org.spotube.millennium.composables.MillenniumDialogHeader
import org.spotube.millennium.composables.MillenniumDialogTitle
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.mushi.app.core.ui.theming.AppTheme
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumModalBottomSheet
import org.spotube.millennium.composables.MillenniumSeparator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.composeunstyled.SheetDetent
import com.composeunstyled.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.ui.unit.dp
import com.mushi.app.resources.iconsax.FluentDismiss
import com.mushi.app.resources.iconsax.Iconsax
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.*

@Composable
fun AdaptiveDialogBottomSheet(
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    // Plain-text title for the large-screen dialog's own titlebar; when set it replaces [title]
    // there so the titlebar isn't left empty above a duplicated in-body title.
    dialogTitle: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit) {
    val isLargeScreen = currentWindowWidthBreakpoint() isAtLeast WindowBreakpoints.Medium

    if (isLargeScreen) {
        MillenniumDialog(
            visible = true,
            onDismissRequest = onDismiss,
            modifier = modifier,
            paneTitle = dialogTitle ?: "") {
            MillenniumDialogHeader {
                if (dialogTitle != null) {
                    MillenniumDialogTitle(text = dialogTitle, textStyle = AppTheme.typography.titleMedium)
                } else {
                    MillenniumDialogTitle { title?.invoke() }
                }
                AppDialogClose(onDismiss)
            }
            MillenniumDialogBody { content() }
            actions?.let { MillenniumDialogFooter(content = { it() }) }
        }
    } else {
        val sheetState = rememberModalBottomSheetState(initialDetent = SheetDetent.FullyExpanded)
        LaunchedEffect(sheetState.currentDetent) {
            if (sheetState.currentDetent == SheetDetent.Hidden) {
                onDismiss()
            }
        }
        MillenniumModalBottomSheet(state = sheetState) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start) {
                title?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) { it() }
                        MillenniumIconButton(
                            onClick = onDismiss,
                            style = MillenniumIconButtonStyle.Quiet) {
                            MillenniumIcon(
                                Iconsax.FluentDismiss,
                                contentDescription = stringResource(Res.string.common_close_dialog))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp)
                        .verticalScroll(rememberScrollState())) {
                    content()
                }
                actions?.let {
                    Spacer(Modifier.height(8.dp))
                    MillenniumSeparator()
                    Spacer(Modifier.height(8.dp))
                    it()
                }
            }
        }
    }
}
