package com.mushi.app.core.deeplink

/**
 * Cross-platform receiver for URIs handed to the app by the operating system
 * (deep links). Platform entry points (Android activity intents, desktop command
 * line / open-URI handler, iOS `onOpenURL`) call [onNewUri]; the main composable
 * installs a [listener] once composition starts.
 *
 * Follows the Compose Multiplatform deep linking docs pattern: URIs arriving
 * before a listener is installed are cached and delivered as soon as one is set.
 */
object ExternalUriHandler {
    private var cached: String? = null

    var listener: ((uri: String) -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                cached?.let(value::invoke)
                cached = null
            }
        }

    fun onNewUri(uri: String) {
        if (uri.isBlank()) return
        val currentListener = listener
        if (currentListener != null) {
            currentListener(uri)
        } else {
            cached = uri
        }
    }
}