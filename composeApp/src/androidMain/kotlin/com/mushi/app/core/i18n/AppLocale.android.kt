package com.mushi.app.core.i18n

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
internal actual fun provideAppLocale(languageTag: String?): ProvidedValue<*> {
    // System resources are never touched by the override below, so they always report the
    // device language (also after the user changes it while the app is running).
    val locale = languageTag?.let(Locale::forLanguageTag)
        ?: Resources.getSystem().configuration.locales[0]
    // Compose resources read Locale.getDefault(); also used by non-UI code (notifications).
    Locale.setDefault(locale)

    val configuration = Configuration(LocalConfiguration.current).apply { setLocale(locale) }
    val resources = LocalContext.current.resources
    @Suppress("DEPRECATION")
    resources.updateConfiguration(configuration, resources.displayMetrics)
    return LocalConfiguration provides configuration
}
