package com.mushi.app.modules.plugin.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumActionPointer
import org.spotube.millennium.theme.millenniumSelectedGelBrush
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumSpokeSpinner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import com.mushi.app.core.ui.component.AdaptiveDropdownBottomSheet
import com.mushi.app.core.ui.component.AdaptiveMenuItem
import com.mushi.app.core.ui.component.HeaderDisplayMode
import com.mushi.app.resources.iconsax.Iconsax3DotsMore
import com.mushi.app.resources.iconsax.IconsaxUserRemove
import com.mushi.app.generated.resources.plugin_action_more
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.modules.plugin.PluginListItem
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxBox
import com.mushi.app.resources.iconsax.IconsaxTrash
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.plugin_action_details
import com.mushi.app.generated.resources.plugin_action_login
import com.mushi.app.generated.resources.plugin_action_logout
import com.mushi.app.generated.resources.plugin_action_remove

/**
 * One installed plugin: logo, name, and what it does. The whole row opens its details, which
 * hold everything else (support, account). User-installed plugins also get a remove button
 * on the row; built-in ones can't be removed.
 */
@Composable
internal fun PluginCard(
    item: PluginListItem,
    onInfo: () -> Unit,
    onRemove: () -> Unit) {
    val plugin = item.plugin
    val colors = MillenniumTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val wash by animateFloatAsState(
        targetValue = when {
            pressed -> 0.24f
            hovered -> 0.12f
            else -> 0f
        },
        animationSpec = tween(140))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (wash > 0f) Modifier.background(millenniumSelectedGelBrush(colors, wash)) else Modifier)
            .hoverable(interaction)
            .millenniumActionPointer()
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClickLabel = stringResource(Res.string.plugin_action_details),
                onClick = onInfo)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        PluginIconWell(size = 40.dp) {
            PluginLogo(
                plugin = plugin,
                logoPath = item.logoPath,
                contentDescription = plugin.name,
                modifier = Modifier.fillMaxSize(),
                fallback = { PluginLogoFallback(com.mushi.app.core.ui.theming.MushiIcons.Box) })
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                plugin.name,
                style = AppTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            PluginRowMeta(item)
        }
        if (item.isRemoving) {
            com.mushi.app.core.ui.component.MushiLoader(size = 16.dp)
        } else {
            if (!item.isBuiltIn) {
                MillenniumIconButton(
                    onClick = onRemove,
                    style = MillenniumIconButtonStyle.Quiet) {
                    MillenniumIcon(
                        com.mushi.app.core.ui.theming.MushiIcons.Trash,
                        contentDescription = stringResource(Res.string.plugin_action_remove),
                        modifier = Modifier.size(16.dp),
                        tint = AppTheme.colors.error)
                }
            }
            MillenniumIcon(
                FeatherIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.onSurfaceVariant)
        }
    }
}

/** What the plugin does, in its ability colors, then who made it: "Audio · Plugin Author". */
@Composable
private fun PluginRowMeta(item: PluginListItem) {
    val plugin = item.plugin
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        plugin.abilities.forEach { ability ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                MillenniumIcon(
                    ability.abilityIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = ability.accentColor())
                Text(
                    ability.displayLabel(),
                    style = AppTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.onSurface,
                    maxLines = 1)
            }
        }
        if (plugin.author.isNotBlank()) {
            Text(
                "· ${plugin.author}",
                modifier = Modifier.weight(1f, fill = false),
                style = AppTheme.typography.labelMedium,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

