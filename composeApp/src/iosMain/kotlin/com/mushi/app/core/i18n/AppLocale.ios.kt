package com.mushi.app.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSUserDefaults

// Compose (and Compose resources) read NSLocale.preferredLanguages, which honours the
// app-level AppleLanguages default.
private const val APPLE_LANGUAGES_KEY = "AppleLanguages"
private const val SYSTEM_LANGUAGE = "system"

private val LocalAppLocale = staticCompositionLocalOf { SYSTEM_LANGUAGE }

@Composable
internal actual fun provideAppLocale(languageTag: String?): ProvidedValue<*> {
    val defaults = NSUserDefaults.standardUserDefaults
    if (languageTag == null) {
        defaults.removeObjectForKey(APPLE_LANGUAGES_KEY)
    } else {
        defaults.setObject(listOf(languageTag), APPLE_LANGUAGES_KEY)
    }
    return LocalAppLocale provides (languageTag ?: SYSTEM_LANGUAGE)
}
