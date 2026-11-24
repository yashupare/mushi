package com.mushi.app.modules.plugin.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mushi.app.modules.plugin.PluginEntry
import com.mushi.app.modules.plugin.builtInPluginLogo
import okio.Path
import org.jetbrains.compose.resources.painterResource

/**
 * Renders a plugin's logo, resolving the best available source:
 *
 * 1. the plugin's own `logo.png` ([logoPath]) when it was installed from disk,
 * 2. the bundled placeholder logo for built-in plugins (see [builtInPluginLogo]),
 * 3. [fallback] (for example the ability icon) when no logo is available.
 */
@Composable
fun PluginLogo(
    plugin: PluginEntry?,
    logoPath: Path?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit) {
    val builtInLogo = plugin?.let { builtInPluginLogo(it) }

    when {
        logoPath != null -> {
            val platformContext = LocalPlatformContext.current
            AsyncImage(
                model = ImageRequest.Builder(platformContext)
                    .data(logoPath.toString())
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier)
        }

        builtInLogo != null -> {
            Image(
                painter = painterResource(builtInLogo),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier)
        }

        else -> fallback()
    }
}
