package com.mushi.app.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

// Captured before the first override so "System default" can restore it.
private val systemLocale: Locale = Locale.getDefault()

private val LocalAppLocale = staticCompositionLocalOf { systemLocale.toLanguageTag() }

@Composable
internal actual fun provideAppLocale(languageTag: String?): ProvidedValue<*> {
    val locale = languageTag?.let(Locale::forLanguageTag) ?: systemLocale
    // Compose resources read Locale.getDefault() both in composition and in getString().
    Locale.setDefault(locale)
    return LocalAppLocale provides locale.toLanguageTag()
}
