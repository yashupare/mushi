package com.mushi.app.modules.shell.alternative_track



import androidx.compose.foundation.layout.Box
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mushi.app.modules.shell.player_queue.BottomSheetContent
import com.mushi.app.modules.shell.player_queue.EndDrawerSheet

private val SlidingSheetBreakpoint = 840.dp

@Composable
fun AlternativeTrackSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        if (currentWindowWidthBreakpoint() isAtLeast WindowBreakpoints.Expanded) {
            EndDrawerSheet(isVisible, onDismiss, content)
        } else {
            BottomSheetContent(isVisible, onDismiss, content)
        }
    }
}
