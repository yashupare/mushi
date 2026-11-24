package com.mushi.app.modules.settings.sections

import com.mushi.app.core.ui.component.MushiButton
import org.spotube.millennium.composables.MillenniumDialogHeader
import org.spotube.millennium.composables.MillenniumDialogTitle
import com.mushi.app.core.ui.component.AppDialogClose
import org.spotube.millennium.composables.MillenniumDialogBody
import org.spotube.millennium.composables.MillenniumDialogFooter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.core.ui.theming.isLightAppearance
import com.mushi.app.modules.settings.SettingsViewModel
import com.mushi.app.modules.settings.Theme
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.components.SelectionSettingCard
import com.mushi.app.modules.settings.components.SettingCardItem
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxColorSwatch
import com.mushi.app.resources.iconsax.IconsaxMagic
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumDialog
import org.spotube.millennium.composables.MillenniumText
import com.mushi.app.generated.resources.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import org.jetbrains.compose.resources.StringResource
import com.mushi.app.core.ui.pressScale
import com.mushi.app.core.ui.rememberHaptics
import com.mushi.app.core.ui.theming.MushiPalette
import com.mushi.app.core.ui.theming.spec

internal fun LazyListScope.appearanceSection(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel,
    includeHeader: Boolean = true) {
    if (includeHeader) {
        settingsSectionHeader(Res.string.settings_section_appearance)
    }
    settingsSectionCard(
        items = listOf(
            {
                SelectionSettingCard(
                    title = stringResource(Res.string.settings_theme_title),
                    subtitle = stringResource(
                        Res.string.settings_theme_subtitle_current,
                        settings.theme.displayLabel()
                    ),
                    icon = {
                        SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Palette, stringResource(Res.string.settings_theme_title))
                    },
                    selectedOption = settings.theme,
                    options = Theme.entries,
                    optionLabel = { it.displayLabel() },
                    onOptionSelected = { theme ->
                        settingsViewModel.updateSettings {
                            copy(theme = theme)
                        }
                    }
                )
            },
            {
                ColorPaletteSettingCard(
                    selected = settings.colorPalette,
                    onSelect = { palette ->
                        settingsViewModel.updateSettings { copy(colorPalette = palette) }
                    })
            }))
}

@Composable
private fun Theme.displayLabel(): String {
    return when (this) {
        Theme.LIGHT -> stringResource(Res.string.settings_theme_light)
        Theme.DARK -> stringResource(Res.string.settings_theme_dark)
        Theme.SYSTEM -> stringResource(Res.string.settings_theme_system)
    }
}

private val MushiPalette.nameRes: StringResource
    get() = when (this) {
        MushiPalette.RoseNight -> Res.string.palette_rose_night
        MushiPalette.Aurora -> Res.string.palette_aurora
        MushiPalette.Ember -> Res.string.palette_ember
        MushiPalette.Tide -> Res.string.palette_tide
    }

private val MushiPalette.descriptionRes: StringResource
    get() = when (this) {
        MushiPalette.RoseNight -> Res.string.palette_rose_night_description
        MushiPalette.Aurora -> Res.string.palette_aurora_description
        MushiPalette.Ember -> Res.string.palette_ember_description
        MushiPalette.Tide -> Res.string.palette_tide_description
    }

/**
 * The colour palette row. Opens a picker where tapping a palette applies it straight away, so the
 * whole app behind the dialog shows the result before it's closed.
 */
@Composable
private fun ColorPaletteSettingCard(
    selected: MushiPalette,
    onSelect: (MushiPalette) -> Unit) {
    var isDialogOpen by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()

    SettingCardItem(
        title = stringResource(Res.string.settings_color_palette_title),
        subtitle = stringResource(
            Res.string.settings_color_palette_subtitle_current,
            stringResource(selected.nameRes)),
        icon = {
            SettingsItemIcon(Iconsax.IconsaxColorSwatch, stringResource(Res.string.settings_color_palette_title))
        },
        trailingContent = { PaletteGradientChip(selected, Modifier.size(width = 44.dp, height = 24.dp)) },
        onClick = { isDialogOpen = true })

    if (isDialogOpen) {
        MillenniumDialog(visible = true, onDismissRequest = { isDialogOpen = false }) {
            MillenniumDialogHeader {
                MillenniumDialogTitle(stringResource(Res.string.settings_color_palette_dialog_title))
                AppDialogClose { isDialogOpen = false }
            }
            MillenniumDialogBody {
                MillenniumText(
                    stringResource(Res.string.settings_color_palette_dialog_description),
                    color = AppTheme.colors.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MushiPalette.entries.forEach { palette ->
                        PaletteOption(
                            palette = palette,
                            selected = palette == selected,
                            onClick = {
                                if (palette != selected) {
                                    haptics.tick()
                                    onSelect(palette)
                                }
                            })
                    }
                }
            }
            MillenniumDialogFooter {
                MushiButton(onClick = { isDialogOpen = false }) {
                    MillenniumText(stringResource(Res.string.common_close))
                }
            }
        }
    }
}

/** One palette: its gradient, its surface colours in the current light/dark mode, name and mood. */
@Composable
private fun PaletteOption(
    palette: MushiPalette,
    selected: Boolean,
    onClick: () -> Unit) {
    val dark = !isLightAppearance()
    val colors = remember(palette, dark) { palette.spec().let { if (dark) it.dark else it.light } }
    val name = stringResource(palette.nameRes)
    val shape = RoundedCornerShape(14.dp)
    val borderColor by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.primary else Color.Transparent,
        label = "palette_border")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(0.98f)
            .clip(shape)
            .background(AppTheme.colors.surfaceContainerHigh)
            .border(2.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
                contentDescription = name
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // A tiny "screen" in the palette's own colours: base, two surfaces and the accent.
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.background)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(6.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PaletteGradientChip(palette, Modifier.fillMaxWidth().height(10.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.surface))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.raised))
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colors.accent))
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            MillenniumText(name, style = AppTheme.typography.titleSmall, color = AppTheme.colors.onSurface)
            MillenniumText(
                stringResource(palette.descriptionRes),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant)
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) AppTheme.colors.primary else Color.Transparent)
                .border(
                    width = if (selected) 0.dp else 1.5.dp,
                    color = AppTheme.colors.outline,
                    shape = CircleShape),
            contentAlignment = Alignment.Center) {
            if (selected) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.onPrimary))
            }
        }
    }
}

/** The palette's signature gradient as a small pill. */
@Composable
private fun PaletteGradientChip(palette: MushiPalette, modifier: Modifier = Modifier) {
    val gradient = remember(palette) { Brush.linearGradient(palette.spec().gradient) }
    Box(modifier.clip(RoundedCornerShape(50)).background(gradient))
}
