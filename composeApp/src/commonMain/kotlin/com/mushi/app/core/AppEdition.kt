package com.mushi.app.core

/**
 * Switches for this customised, private build of the app.
 *
 * The music plugins ship inside the app (see `BUNDLED_PLUGIN_RESOURCES`) and are installed
 * automatically on launch, so the user never has to find, install or manage plugins.
 */
object AppEdition {
    /**
     * Whether the user can browse, install, update or remove plugins, and whether the
     * welcome/onboarding flow (which walks through plugin setup) is shown.
     *
     * `false` hides every plugin-management entry point in the UI. Set it back to `true`
     * to restore plugin management.
     */
    const val PLUGIN_MANAGEMENT_ENABLED: Boolean = false

    /**
     * Whether the app shows anything that points outside the app: the update checker, the
     * support and donation prompts, and the support section in Settings.
     *
     * `false` for the Mushi build.
     */
    const val UPSTREAM_LINKS_ENABLED: Boolean = false

    /**
     * Whether the app checks Mushi's own GitHub releases (`mushi.repositoryUrl` in
     * gradle.properties) and offers newer versions. A blank repository URL also turns it off.
     */
    const val APP_UPDATES_ENABLED: Boolean = true
}
