package com.mushi.app.modules.settings.components

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import org.spotube.millennium.composables.MillenniumListTile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Reusable setting card item component for consistent styling
 * across all list items in the settings screen
 */
@Composable
fun SettingCardItem(
    enabled: Boolean = true,
    title: String,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    MillenniumListTile(
        selected = false,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        leading = icon,
        title = {
            Text(
                title,
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colors.onSurface
            )
        },
        subtitle = subtitle?.let {
            {
                Text(
                    it,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }
        },
        trailing = trailingContent)
}
