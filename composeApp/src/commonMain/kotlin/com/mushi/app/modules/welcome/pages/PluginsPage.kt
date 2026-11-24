package com.mushi.app.modules.welcome.pages

import androidx.compose.runtime.Composable
import com.mushi.app.modules.plugin.PluginScreen

/** The same installation, discovery, permissions and sign-in flow as Settings, in-place. */
@Composable
fun PluginsPage() {
    PluginScreen(onboarding = true)
}
