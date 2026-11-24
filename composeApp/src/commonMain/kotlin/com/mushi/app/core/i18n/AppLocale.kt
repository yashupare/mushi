package com.mushi.app.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.mushi.app.modules.settings.SupportedLanguages

/**
 * Points the platform locale (which Compose resources resolve strings from) at [languageTag],
 * or back to the system language when it is null, and returns the platform-specific
 * composition local that carries it.
 */
@Composable
internal expect fun provideAppLocale(languageTag: String?): ProvidedValue<*>

/**
 * Renders [content] in the app language picked in settings, overriding the system language
 * unless [language] is [SupportedLanguages.SYSTEM].
 *
 * Compose resources only re-resolve strings when the calling composable recomposes, so the
 * content is keyed on the language: switching languages recreates the subtree. Keep state that
 * must survive a language switch (e.g. navigation) above this provider.
 */
@Composable
fun AppLocaleProvider(language: SupportedLanguages, content: @Composable () -> Unit) {
    val languageTag = language.languageTag
    val layoutDirection = when {
        language == SupportedLanguages.SYSTEM -> null
        language.isRtl -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }
    val locales = listOfNotNull(
        provideAppLocale(languageTag),
        layoutDirection?.let { LocalLayoutDirection provides it })
    CompositionLocalProvider(*locales.toTypedArray()) {
        key(languageTag) {
            PublishResourceEnvironment()
            content()
        }
    }
}
