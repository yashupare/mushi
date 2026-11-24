package com.mushi.app.core

/**
 * Development-only switches. These are intentionally kept out of [com.mushi.app.modules.settings.UserSettings]
 * so they can never leak into user-facing configuration.
 */
object DebugConfig {
    /**
     * Flip to `true` while developing to always open the Welcome/Onboarding screen, even after
     * it has been completed (so you don't have to wipe app data to see it again). It still
     * dismisses for the session when you finish or skip it.
     *
     * Keep it `false` when committing.
     */
    const val FORCE_SHOW_ONBOARDING: Boolean = false

    /**
     * Flip to `true` while developing to show the support reminder dialog on the next eligible
     * app launch, ignoring both the 7-day interval and the "Support reminders" opt-out toggle.
     * It still waits until onboarding is dismissed, and only fires once per launch.
     *
     * Keep it `false` when committing.
     */
    const val FORCE_SUPPORT_REMINDER: Boolean = false

    const val FORCE_VLC_LOGS: Boolean = false
}
