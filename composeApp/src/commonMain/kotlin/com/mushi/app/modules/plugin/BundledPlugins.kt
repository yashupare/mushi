package com.mushi.app.modules.plugin

/**
 * Plugin archives (`.smplug`) that ship inside the app and are installed automatically on
 * launch by [PluginManager], so users never install plugins themselves.
 *
 * Paths are relative to `composeApp/src/commonMain/composeResources`.
 *
 * - `spotify.smplug`: Spotify metadata plugin v1.1.0 from sonic-liberation/spotube-plugin-spotify
 *   (AGPL-3.0-or-later), SHA-256 5d83f273d425b18288c9d0bad37f1c4b357182a8f8625a71f161942cb211082e.
 *   It uses Spotify's unofficial API and needs the user to sign in to Spotify.
 *
 * Audio and lyrics come from the built-in NewPipe YouTube and LRCLib plugins.
 */
internal val BUNDLED_PLUGIN_RESOURCES: List<String> = listOf(
    "files/bundled/spotify.smplug")
