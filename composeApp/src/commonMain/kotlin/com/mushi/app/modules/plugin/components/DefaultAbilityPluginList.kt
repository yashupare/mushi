package com.mushi.app.modules.plugin.components

import com.mushi.app.core.ui.component.MushiButton
import org.spotube.millennium.composables.MillenniumIcon
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import org.spotube.millennium.theme.millenniumBevelBrush
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import com.mushi.app.generated.resources.plugin_action_login
import com.mushi.app.generated.resources.plugin_action_logout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumActionPointer
import org.spotube.millennium.theme.millenniumSelectedGelBrush
import com.mushi.app.generated.resources.plugin_ability_audio_caption
import com.mushi.app.generated.resources.plugin_ability_lyrics_caption
import com.mushi.app.generated.resources.plugin_ability_metadata_caption
import com.mushi.app.generated.resources.plugin_ability_scrobble_caption
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme

import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.component.AdaptiveDropdownBottomSheet
import com.mushi.app.core.ui.component.AdaptiveMenuItem
import com.mushi.app.core.ui.component.HeaderDisplayMode
import com.mushi.app.modules.plugin.AbilitySelection
import com.mushi.app.modules.plugin.PluginAbility
import com.mushi.app.modules.plugin.PluginEntry
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxArrowDown4
import com.mushi.app.resources.iconsax.IconsaxDocumentText
import com.mushi.app.resources.iconsax.IconsaxMusic
import com.mushi.app.resources.iconsax.IconsaxSound
import com.mushi.app.resources.iconsax.IconsaxTextalignLeft
import okio.Path
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.plugin_version_label
import com.mushi.app.generated.resources.settings_plugins_ability_audio
import com.mushi.app.generated.resources.settings_plugins_ability_lyrics
import com.mushi.app.generated.resources.settings_plugins_ability_metadata
import com.mushi.app.generated.resources.settings_plugins_ability_scrobble
import com.mushi.app.generated.resources.settings_plugins_no_plugins
import com.mushi.app.generated.resources.plugin_selection_none

/** The accent color used for a plugin ability across the whole plugin UI. */
internal fun PluginAbility.accentColor(): Color = when (this) {
    PluginAbility.METADATA -> Color(0xFF34A853)
    PluginAbility.AUDIO -> Color(0xFF1E88E5)
    PluginAbility.LYRICS -> Color(0xFFF9A825)
    PluginAbility.SCROBBLE -> Color(0xFF8E24AA)
}

/** The icon used to represent a plugin ability. */
internal fun PluginAbility.abilityIcon(): ImageVector = when (this) {
    PluginAbility.METADATA -> com.mushi.app.core.ui.theming.MushiIcons.Document
    PluginAbility.AUDIO -> com.mushi.app.core.ui.theming.MushiIcons.MusicNote
    PluginAbility.LYRICS -> com.mushi.app.core.ui.theming.MushiIcons.AlignLeft
    PluginAbility.SCROBBLE -> Iconsax.IconsaxSound
}

/**
 * A vibrant, multi-hue gradient per ability, used by the Frutiger-Aero plugin tiles.
 */
internal fun PluginAbility.aeroGradient(): List<Color> = when (this) {
    PluginAbility.METADATA -> listOf(Color(0xFFA8E063), Color(0xFF56AB2F), Color(0xFF11998E))
    PluginAbility.AUDIO -> listOf(Color(0xFF56CCF2), Color(0xFF2F80ED), Color(0xFF6A11CB))
    PluginAbility.LYRICS -> listOf(Color(0xFFFFE259), Color(0xFFFFA751), Color(0xFFFF5E62))
    PluginAbility.SCROBBLE -> listOf(Color(0xFFFF6FD8), Color(0xFF9D50BB), Color(0xFF3F0D7A))
}

@Composable
internal fun PluginAbility.displayLabel(): String = when (this) {
    PluginAbility.METADATA -> stringResource(Res.string.settings_plugins_ability_metadata)
    PluginAbility.AUDIO -> stringResource(Res.string.settings_plugins_ability_audio)
    PluginAbility.LYRICS -> stringResource(Res.string.settings_plugins_ability_lyrics)
    PluginAbility.SCROBBLE -> stringResource(Res.string.settings_plugins_ability_scrobble)
}

/** A short "v1.2.3 · Author" summary shown under a plugin in the picker. */
@Composable
private fun PluginEntry.metadataLabel(): String = buildString {
    append(stringResource(Res.string.plugin_version_label, version))
    if (author.isNotBlank()) {
        append(" · ").append(author)
    }
}

/** One plain line saying what the ability does, so "Metadata" or "Scrobble" isn't jargon. */
@Composable
private fun PluginAbility.caption(): String = when (this) {
    PluginAbility.METADATA -> stringResource(Res.string.plugin_ability_metadata_caption)
    PluginAbility.AUDIO -> stringResource(Res.string.plugin_ability_audio_caption)
    PluginAbility.LYRICS -> stringResource(Res.string.plugin_ability_lyrics_caption)
    PluginAbility.SCROBBLE -> stringResource(Res.string.plugin_ability_scrobble_caption)
}

/**
 * The default-plugin pickers as one grouped list, one row per job. Left: the job, what it does,
 * and the plugin doing it. Right: the picker arrow, with the plugin's Login / Logout under it.
 * Tapping a row picks another plugin.
 */
@Composable
internal fun DefaultAbilityPluginList(
    selections: List<AbilitySelection>,
    onSelected: (PluginAbility, PluginEntry?) -> Unit,
    onLogin: (PluginEntry) -> Unit,
    onLogout: (PluginEntry) -> Unit,
    modifier: Modifier = Modifier) {
    PluginListPanel(modifier = modifier) {
        selections.forEachIndexed { index, selection ->
            if (index > 0) PluginRowSeparator()
            DefaultAbilityPluginRow(
                selection = selection,
                onSelected = { onSelected(selection.ability, it) },
                onLogin = onLogin,
                onLogout = onLogout)
        }
    }
}

@Composable
private fun DefaultAbilityPluginRow(
    selection: AbilitySelection,
    onSelected: (PluginEntry?) -> Unit,
    onLogin: (PluginEntry) -> Unit,
    onLogout: (PluginEntry) -> Unit) {
    val ability = selection.ability
    val selected = selection.selectedPlugin
    val noPluginsText = stringResource(Res.string.settings_plugins_no_plugins)

    val menuItems = buildList {
        if (selection.plugins.isNotEmpty()) {
            selection.plugins.forEach { plugin ->
                add(
                    AdaptiveMenuItem(
                        label = plugin.name,
                        subtitle = plugin.metadataLabel(),
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center) {
                                PluginLogo(
                                    plugin = plugin,
                                    logoPath = selection.pluginLogoPaths[plugin.id],
                                    contentDescription = plugin.name,
                                    modifier = Modifier.fillMaxSize(),
                                    fallback = {
                                        MillenniumIcon(
                                            imageVector = ability.abilityIcon(),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = ability.accentColor())
                                    })
                            }
                        },
                        onClick = { onSelected(plugin) },
                        selected = selected?.id == plugin.id)
                )
            }
        } else {
            add(AdaptiveMenuItem(label = noPluginsText, onClick = { }, enabled = false))
        }
    }

    AdaptiveDropdownBottomSheet(
        items = menuItems,
        modifier = Modifier.fillMaxWidth(),
        headerDisplayMode = HeaderDisplayMode.OnlyInBottomSheet,
        header = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AbilityIconTile(ability, size = 36.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ability.displayLabel(),
                        style = AppTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text(
                        ability.caption(),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant)
                }
            }
        },
        trigger = { onClick ->
            DefaultAbilityRowContent(
                selection = selection,
                onClick = onClick,
                onAuth = selected
                    ?.takeIf { selection.selectedAuthState.requiresAuth }
                    ?.let { plugin ->
                        { if (selection.selectedAuthState.isLoggedIn) onLogout(plugin) else onLogin(plugin) }
                    })
        })
}

@Composable
private fun DefaultAbilityRowContent(
    selection: AbilitySelection,
    onClick: () -> Unit,
    onAuth: (() -> Unit)?) {
    val ability = selection.ability
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
            .height(IntrinsicSize.Min)
            .then(if (wash > 0f) Modifier.background(millenniumSelectedGelBrush(colors, wash)) else Modifier)
            .hoverable(interaction)
            .millenniumActionPointer()
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AbilityIconTile(ability, size = 40.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                ability.displayLabel(),
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(
                ability.caption(),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            SelectedPluginChip(
                ability, selection.selectedPlugin, selection.pluginLogoPaths[selection.selectedPlugin?.id],
                modifier = Modifier.padding(top = 6.dp))
        }
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween) {
            MillenniumIcon(
                imageVector = Iconsax.IconsaxArrowDown4,
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp).size(16.dp),
                tint = AppTheme.colors.onSurfaceVariant)
            if (onAuth != null) {
                val loggedIn = selection.selectedAuthState.isLoggedIn
                MushiButton(
                    onClick = onAuth,
                    loading = selection.isAuthInProgress,
                    style = if (loggedIn) MillenniumButtonStyle.Secondary else MillenniumButtonStyle.Primary) {
                    Text(
                        stringResource(if (loggedIn) Res.string.plugin_action_logout else Res.string.plugin_action_login),
                        style = AppTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** The plugin currently doing the job: its logo and name in a sunken pill, or "None". */
@Composable
private fun SelectedPluginChip(
    ability: PluginAbility,
    plugin: PluginEntry?,
    logoPath: Path?,
    modifier: Modifier = Modifier) {
    val colors = MillenniumTheme.colors
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .heightIn(min = 28.dp)
            .clip(shape)
            .background(AppTheme.colors.onSurface.copy(alpha = 0.08f))
            .padding(start = if (plugin != null) 3.dp else 10.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (plugin != null) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape),
                contentAlignment = Alignment.Center) {
                PluginLogo(
                    plugin = plugin,
                    logoPath = logoPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    fallback = {
                        MillenniumIcon(
                            imageVector = ability.abilityIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = ability.accentColor())
                    })
            }
        }
        Text(
            plugin?.name ?: stringResource(Res.string.plugin_selection_none),
            style = AppTheme.typography.labelLarge,
            fontWeight = if (plugin != null) FontWeight.Medium else FontWeight.Normal,
            color = if (plugin != null) AppTheme.colors.onSurface else AppTheme.colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
    }
}

/** A flat rounded square in the ability's colour (no gloss, shadow or outline). */
@Composable
internal fun AbilityIconTile(ability: PluginAbility, size: Dp) {
    val shape = RoundedCornerShape(size / 3.6f)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(ability.aeroGradient().last()),
        contentAlignment = Alignment.Center) {
        MillenniumIcon(
            imageVector = ability.abilityIcon(),
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = Color.White)
    }
}
