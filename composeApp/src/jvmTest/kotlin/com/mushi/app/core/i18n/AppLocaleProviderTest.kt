package com.mushi.app.core.i18n

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.use
import com.mushi.app.modules.settings.SupportedLanguages
import kotlinx.coroutines.runBlocking
import com.mushi.app.core.i18n.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.common_cancel
import com.mushi.app.generated.resources.plugin_installed_plugins_count
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalComposeUiApi::class)
class AppLocaleProviderTest {
    @Test
    fun switchingLanguageUpdatesStringsWithoutRestart() {
        val systemLocale = Locale.getDefault()
        var language by mutableStateOf(SupportedLanguages.DE)
        var cancel = ""
        var plugins = ""
        var direction = LayoutDirection.Ltr

        try {
            ImageComposeScene(width = 100, height = 100) {
                AppLocaleProvider(language) {
                    val cancelText = stringResource(Res.string.common_cancel)
                    val pluginsText = pluralStringResource(Res.plurals.plugin_installed_plugins_count, 3, 3)
                    val layoutDirection = LocalLayoutDirection.current
                    SideEffect {
                        cancel = cancelText
                        plugins = pluginsText
                        direction = layoutDirection
                    }
                }
            }.use { scene ->
                scene.render()
                assertEquals("Abbrechen", cancel)
                assertEquals("3 Plugins installiert", plugins)
                // Non-composable lookups (ViewModels, services) follow the override too.
                assertEquals("Abbrechen", runBlocking { getString(Res.string.common_cancel) })

                language = SupportedLanguages.ZH_TW
                scene.render()
                assertEquals("取消", cancel)
                assertEquals("已安裝 3 個外掛程式", plugins)

                language = SupportedLanguages.RU
                scene.render()
                assertEquals("3 плагина установлено", plugins)

                language = SupportedLanguages.AR
                scene.render()
                assertEquals(LayoutDirection.Rtl, direction)

                language = SupportedLanguages.SYSTEM
                scene.render()
                assertEquals(systemLocale, Locale.getDefault())
            }
        } finally {
            Locale.setDefault(systemLocale)
        }
    }
}
