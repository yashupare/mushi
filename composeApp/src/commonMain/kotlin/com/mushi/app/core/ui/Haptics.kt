package com.mushi.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * The few haptic cues the app uses, kept small and consistent: a light tick for changing a
 * selection (tabs) and a firmer tap for a confirmed action (play, pause). Platforms without
 * haptics ignore them. Use sparingly; the system's own vibration setting still applies.
 */
@Stable
class Haptics internal constructor(private val feedback: HapticFeedback) {
    /** A light tick for moving between options, such as switching tabs. */
    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    /** A firmer tap for a confirmed action, such as play or pause. */
    fun tap() = feedback.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberHaptics(): Haptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { Haptics(feedback) }
}
