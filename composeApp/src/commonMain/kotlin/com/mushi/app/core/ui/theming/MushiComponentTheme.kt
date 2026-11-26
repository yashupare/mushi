package com.mushi.app.core.ui.theming

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.spotube.millennium.composables.MillenniumButtonTheme
import org.spotube.millennium.composables.MillenniumButtonThemeProperties
import org.spotube.millennium.composables.MillenniumCardTheme
import org.spotube.millennium.composables.MillenniumCheckboxTheme
import org.spotube.millennium.composables.MillenniumDialogTheme
import org.spotube.millennium.composables.MillenniumDropdownMenuTheme
import org.spotube.millennium.composables.MillenniumIconButtonTheme
import org.spotube.millennium.composables.MillenniumIconButtonThemeProperties
import org.spotube.millennium.composables.MillenniumListTileTheme
import org.spotube.millennium.composables.MillenniumModalBottomSheetTheme
import org.spotube.millennium.composables.MillenniumProgressTheme
import org.spotube.millennium.composables.MillenniumSegmentedButtonTheme
import org.spotube.millennium.composables.MillenniumSeparatorTheme
import org.spotube.millennium.composables.MillenniumSliderTheme
import org.spotube.millennium.composables.MillenniumSnackbarTheme
import org.spotube.millennium.composables.MillenniumTabGroupTheme
import org.spotube.millennium.composables.MillenniumTextFieldTheme
import org.spotube.millennium.composables.MillenniumToggleSwitchTheme
import org.spotube.millennium.theme.MillenniumColors
import org.spotube.millennium.theme.MillenniumControlStateProperty
import org.spotube.millennium.theme.MillenniumThemeComponents

/*
 * One place that gives every Millennium component the flat Mushi look: no borders, shadows,
 * glows or bevels; flat fills; rounded shapes; Apple-style sizes. Every screen that uses these
 * components inherits it, so a change here changes the whole app. See MUSHI_REVAMP_PLAN.md,
 * section 2, for the spec each block below follows.
 */

private val Clear = Color.Transparent

/** The same properties for every state, with a dimmer pressed state and a faded disabled one. */
private fun <T> states(
    base: T,
    pressed: T = base,
    disabled: T = base,
    focused: T = base,
    error: T = base,
    hovered: T = base,
    selected: T = base
) = MillenniumControlStateProperty(
    default = base,
    hovered = hovered,
    focused = focused,
    pressed = pressed,
    disabled = disabled,
    selected = selected,
    error = error)

private fun tint(c: MillenniumColors, alpha: Float) = c.text.copy(alpha = alpha)

/** Flattens [base] into the Mushi look for the given colours. */
internal fun flatComponents(
    base: MillenniumThemeComponents,
    c: MillenniumColors,
    type: AppTypography = AppDefaultTypography
): MillenniumThemeComponents {
    val pill = RoundedCornerShape(50)
    val onDark = c.background.red + c.background.green + c.background.blue < 1.5f
    // The selected tab sits on a lighter plate: white on grey in light mode, raised in dark mode.
    val selectedTabPlate = if (onDark) c.raised else c.background

    // Buttons ---------------------------------------------------------------------------------
    fun button(
        proto: MillenniumButtonThemeProperties,
        fill: Color,
        label: Color
    ) = proto.copy(
        background = SolidColor(fill),
        foreground = label,
        textStyle = type.labelLarge,
        shape = RoundedCornerShape(12.dp),
        padding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        minSize = DpSize(proto.minSize.width, 48.dp),
        borderWidth = 0.dp,
        borderColor = Clear,
        shadows = emptyList(),
        shadowElevation = 0.dp,
        innerGlowColor = Clear,
        innerGlowWidth = 0.dp,
        innerGlowInset = 0.dp,
        innerGlowTop = 0.dp,
        bottomRimColor = Clear,
        alpha = 1f)

    fun buttonStates(
        s: MillenniumControlStateProperty<MillenniumButtonThemeProperties>,
        fill: Color,
        label: Color
    ): MillenniumControlStateProperty<MillenniumButtonThemeProperties> {
        val flat = button(s.default, fill, label)
        return states(
            base = flat,
            pressed = flat.copy(alpha = 0.85f),
            disabled = flat.copy(alpha = 0.4f))
    }

    val buttons = base.buttonTheme.let {
        MillenniumButtonTheme(
            primary = buttonStates(it.primary, c.accent, c.onAccent),
            secondary = buttonStates(it.secondary, tint(c, 0.08f), c.text),
            quiet = buttonStates(it.quiet, Clear, c.accent),
            danger = buttonStates(it.danger, c.danger, Color.White))
    }

    // Icon buttons ------------------------------------------------------------------------------
    fun icon(
        proto: MillenniumIconButtonThemeProperties,
        fill: Color,
        glyph: Color
    ) = proto.copy(
        background = SolidColor(fill),
        foreground = glyph,
        shape = CircleShape,
        size = DpSize(44.dp, 44.dp),
        iconSize = 22.dp,
        borderWidth = 0.dp,
        borderColor = Clear,
        shadows = emptyList(),
        shadowElevation = 0.dp,
        innerGlowColor = Clear,
        bottomRimColor = Clear,
        orb = false,
        orbColor = Clear,
        orbStrength = 0f,
        alpha = 1f)

    fun iconStates(
        s: MillenniumControlStateProperty<MillenniumIconButtonThemeProperties>,
        fill: Color,
        glyph: Color
    ): MillenniumControlStateProperty<MillenniumIconButtonThemeProperties> {
        val flat = icon(s.default, fill, glyph)
        return states(
            base = flat,
            pressed = flat.copy(alpha = 0.7f),
            disabled = flat.copy(alpha = 0.35f))
    }

    val iconButtons = base.iconButtonTheme.let {
        MillenniumIconButtonTheme(
            primary = iconStates(it.primary, c.accent, c.onAccent),
            secondary = iconStates(it.secondary, tint(c, 0.08f), c.text),
            quiet = iconStates(it.quiet, Clear, c.text),
            danger = iconStates(it.danger, c.danger, Color.White))
    }

    // Text field --------------------------------------------------------------------------------
    val textField = base.textFieldTheme.states.let { s ->
        val flat = s.default.copy(
            background = SolidColor(tint(c, 0.08f)),
            shape = RoundedCornerShape(12.dp),
            textStyle = type.bodyLarge,
            // The library's own defaults carry no font family, so set every style.
            placeholderStyle = type.bodyLarge,
            labelStyle = type.labelMedium,
            errorStyle = type.bodySmall,
            foreground = c.text,
            placeholderColor = c.muted,
            cursorColor = c.accent,
            selectionColor = c.accent.copy(alpha = 0.3f),
            errorColor = c.danger,
            borderColor = Clear,
            borderWidth = 0.dp,
            minimumHeight = 44.dp,
            insetShadeColor = Clear,
            innerGlowColor = Clear,
            bottomRimColor = Clear,
            alpha = 1f)
        MillenniumTextFieldTheme(
            states(
                base = flat,
                // Focus is a slightly deeper fill and the accent cursor, no ring or glow.
                focused = flat.copy(background = SolidColor(tint(c, 0.12f))),
                error = flat.copy(borderColor = c.danger, borderWidth = 1.5.dp),
                disabled = flat.copy(alpha = 0.5f)))
    }

    // Toggle ------------------------------------------------------------------------------------
    val toggle = base.toggleSwitchTheme.states.let { s ->
        val flat = s.default.copy(
            trackBackground = SolidColor(tint(c, 0.16f)),
            selectedTrackBackground = SolidColor(c.accent),
            trackShape = pill,
            trackSize = DpSize(51.dp, 31.dp),
            trackBorderColor = Clear,
            trackBorderWidth = 0.dp,
            trackGlowColor = Clear,
            thumbBackground = SolidColor(Color.White),
            thumbShape = CircleShape,
            thumbSize = 27.dp,
            thumbInset = 2.dp,
            thumbElevation = 3.dp,
            thumbBorderColor = Clear,
            thumbBorderWidth = 0.dp,
            thumbGlowColor = Clear,
            minimumTouchHeight = 44.dp,
            alpha = 1f)
        MillenniumToggleSwitchTheme(states(base = flat, disabled = flat.copy(alpha = 0.4f)))
    }

    // Slider ------------------------------------------------------------------------------------
    val slider = base.sliderTheme.states.let { s ->
        val flat = s.default.copy(
            trackBackground = SolidColor(tint(c, 0.2f)),
            activeTrackBackground = SolidColor(c.text),
            thumbBackground = SolidColor(c.text),
            trackShape = pill,
            thumbShape = CircleShape,
            trackHeight = 4.dp,
            thumbSize = 12.dp,
            activeThumbSize = 18.dp,
            touchTargetHeight = 44.dp,
            trackBorderColor = Clear,
            trackBorderWidth = 0.dp,
            thumbBorderColor = Clear,
            thumbBorderWidth = 0.dp,
            thumbElevation = 0.dp,
            thumbGlowColor = Clear,
            thumbBottomRimColor = Clear,
            trackInsetShadeColor = Clear,
            alpha = 1f)
        MillenniumSliderTheme(states(base = flat, disabled = flat.copy(alpha = 0.4f)))
    }

    // Checkbox ----------------------------------------------------------------------------------
    val checkbox = base.checkboxTheme.states.let { s ->
        val flat = s.default.copy(
            faceBackground = SolidColor(Clear),
            selectedFaceBackground = SolidColor(c.accent),
            selectedFaceForeground = c.onAccent,
            faceShape = CircleShape,
            faceSize = DpSize(22.dp, 22.dp),
            faceBorderColor = tint(c, 0.35f),
            faceBorderWidth = 1.5.dp,
            faceGlowColor = Clear,
            faceElevation = 0.dp,
            faceAlpha = 1f,
            minimumTouchHeight = 44.dp)
        MillenniumCheckboxTheme(
            states(
                base = flat,
                selected = flat.copy(faceBorderWidth = 0.dp),
                disabled = flat.copy(faceAlpha = 0.4f)))
    }

    // Tabs and segmented control ----------------------------------------------------------------
    val tabs = base.tabGroupTheme.states.let { s ->
        val flat = s.default.copy(
            listBackground = SolidColor(tint(c, 0.08f)),
            listShape = RoundedCornerShape(12.dp),
            listBorderColor = Clear,
            listBorderWidth = 0.dp,
            listPadding = 2.dp,
            tabShape = RoundedCornerShape(10.dp),
            // The chosen tab is a solid accent pill, like the Library tabs and Home chips.
            selectedTabBackground = SolidColor(c.accent),
            unselectedTabBackground = SolidColor(Clear),
            selectedTabForeground = c.onAccent,
            unselectedTabForeground = c.muted,
            tabTextStyle = type.labelLarge,
            minimumTabHeight = 36.dp,
            tabBorderWidth = 0.dp,
            tabElevation = 0.dp,
            tabGlowColor = Clear,
            alpha = 1f)
        MillenniumTabGroupTheme(states(base = flat, disabled = flat.copy(alpha = 0.4f)))
    }
    val segmented = base.segmentedButtonTheme.states.let { s ->
        val flat = s.default.copy(
            background = SolidColor(tint(c, 0.08f)),
            foreground = c.text,
            textStyle = type.labelLarge,
            minimumHeight = 36.dp,
            borderWidth = 0.dp,
            borderColor = Clear,
            innerGlowColor = Clear,
            alpha = 1f)
        // The theme never set a selected state, so the active segment looked like the others.
        val on = flat.copy(background = SolidColor(c.accent), foreground = c.onAccent)
        MillenniumSegmentedButtonTheme(
            states(
                base = flat,
                selected = on,
                pressed = flat.copy(alpha = 0.85f),
                disabled = flat.copy(alpha = 0.4f)))
    }

    // List rows ---------------------------------------------------------------------------------
    val listTile = base.listTileTheme.states.let { s ->
        val flat = s.default.copy(
            titleStyle = type.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            subtitleStyle = type.bodySmall,
            titleForeground = c.text,
            subtitleForeground = c.muted,
            selectedTitleForeground = c.accent,
            selectedSubtitleForeground = c.muted,
            background = SolidColor(Clear),
            selectedBackground = SolidColor(tint(c, 0.06f)),
            selectedGelStrength = 0f,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            minimumHeight = 56.dp,
            borderWidth = 0.dp,
            borderColor = Clear,
            innerGlowColor = Clear,
            alpha = 1f)
        MillenniumListTileTheme(
            states(
                base = flat,
                hovered = flat.copy(background = SolidColor(tint(c, 0.04f))),
                pressed = flat.copy(background = SolidColor(tint(c, 0.08f))),
                disabled = flat.copy(alpha = 0.4f)))
    }

    // Card --------------------------------------------------------------------------------------
    val card = base.cardTheme.states.let { s ->
        val flat = s.default.copy(
            background = SolidColor(c.surface),
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 0.dp,
            shadowColor = Clear,
            borderColor = Clear,
            borderWidth = 0.dp,
            innerGlowColor = Clear,
            bottomRimColor = Clear,
            alpha = 1f)
        MillenniumCardTheme(states(base = flat))
    }

    // Menu, dialog, sheet, toast ----------------------------------------------------------------
    val menu = base.dropdownMenuTheme.states.let { s ->
        val flat = s.default.copy(
            panelBackground = SolidColor(c.raised),
            panelShape = RoundedCornerShape(14.dp),
            panelBorderColor = Clear,
            panelBorderWidth = 0.dp,
            panelShadowElevation = 12.dp,
            panelGlowColor = Clear,
            panelPadding = PaddingValues(6.dp),
            itemBackground = SolidColor(Clear),
            itemShape = RoundedCornerShape(10.dp),
            itemForeground = c.text,
            itemTextStyle = type.bodyLarge,
            itemMinimumHeight = 44.dp,
            itemBorderColor = Clear,
            itemBorderWidth = 0.dp,
            itemGlowColor = Clear,
            alpha = 1f)
        MillenniumDropdownMenuTheme(
            states(
                base = flat,
                hovered = flat.copy(itemBackground = SolidColor(tint(c, 0.08f))),
                focused = flat.copy(itemBackground = SolidColor(tint(c, 0.08f))),
                pressed = flat.copy(itemBackground = SolidColor(tint(c, 0.12f))),
                selected = flat.copy(itemBackground = SolidColor(tint(c, 0.10f))),
                disabled = flat.copy(alpha = 0.4f)))
    }
    val dialog = base.dialogTheme.states.let { s ->
        val flat = s.default.copy(
            panelBackground = SolidColor(c.raised),
            shape = RoundedCornerShape(20.dp),
            foreground = c.text,
            titleStyle = type.titleMedium,
            panelBorderColor = Clear,
            panelBorderWidth = 0.dp,
            panelShadowElevation = 24.dp,
            panelGlowColor = Clear,
            scrimColor = Color.Black.copy(alpha = 0.4f),
            enterScale = 0.95f,
            titleBarBackground = SolidColor(Clear),
            titleBarForeground = c.text,
            titleBarBorderColor = Clear,
            footerBackground = SolidColor(Clear),
            footerBorderColor = Clear,
            alpha = 1f)
        MillenniumDialogTheme(states(base = flat))
    }
    val sheet = base.modalBottomSheetTheme.states.let { s ->
        val flat = s.default.copy(
            background = SolidColor(c.raised),
            foreground = c.text,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            handleSize = DpSize(36.dp, 5.dp),
            handleColor = tint(c, 0.25f),
            borderColor = Clear,
            borderWidth = 0.dp,
            shadowElevation = 0.dp,
            innerGlowColor = Clear,
            scrimColor = Color.Black.copy(alpha = 0.4f),
            alpha = 1f)
        MillenniumModalBottomSheetTheme(states(base = flat))
    }
    val toast = base.snackbarTheme.properties.copy(
        background = SolidColor(c.text),
        foreground = c.background,
        actionForeground = c.background,
        shape = RoundedCornerShape(14.dp),
        borderColor = Clear,
        borderWidth = 0.dp,
        shadowElevation = 8.dp,
        innerGlowColor = Clear)

    // Progress and separator --------------------------------------------------------------------
    val progress = base.progressTheme.states.let { s ->
        val flat = s.default.copy(
            trackBackground = SolidColor(tint(c, 0.12f)),
            progressBackground = SolidColor(c.text),
            shape = pill,
            height = 3.dp,
            borderColor = Clear,
            borderWidth = 0.dp,
            innerGlowColor = Clear,
            alpha = 1f)
        MillenniumProgressTheme(states(base = flat))
    }
    val separator = base.separatorTheme.states.let { s ->
        val flat = s.default.copy(
            color = c.border.copy(alpha = 0.6f),
            thickness = 0.5.dp,
            highlightColor = Clear,
            highlightWidth = 0.dp,
            alpha = 1f)
        MillenniumSeparatorTheme(states(base = flat))
    }

    return base.copy(
        textTheme = base.textTheme.withFontFamily(type.bodyMedium.fontFamily),
        buttonTheme = buttons,
        iconButtonTheme = iconButtons,
        textFieldTheme = textField,
        toggleSwitchTheme = toggle,
        sliderTheme = slider,
        checkboxTheme = checkbox,
        tabGroupTheme = tabs,
        segmentedButtonTheme = segmented,
        listTileTheme = listTile,
        cardTheme = card,
        dropdownMenuTheme = menu,
        dialogTheme = dialog,
        modalBottomSheetTheme = sheet,
        snackbarTheme = MillenniumSnackbarTheme(toast),
        progressTheme = progress,
        separatorTheme = separator)
}

/** Millennium's text styles (body, labels, buttons, titles…) in [family], in every state. */
private fun org.spotube.millennium.composables.MillenniumTextTheme.withFontFamily(
    family: androidx.compose.ui.text.font.FontFamily?
): org.spotube.millennium.composables.MillenniumTextTheme {
    if (family == null) return this
    fun org.spotube.millennium.composables.MillenniumTextThemeProperties.withFont() = copy(
        body = body.copy(fontFamily = family),
        label = label.copy(fontFamily = family),
        button = button.copy(fontFamily = family),
        title = title.copy(fontFamily = family),
        headline = headline.copy(fontFamily = family),
        caption = caption.copy(fontFamily = family),
        avatar = avatar.copy(fontFamily = family))
    val s = states
    return org.spotube.millennium.composables.MillenniumTextTheme(
        states = org.spotube.millennium.theme.MillenniumControlStateProperty(
            default = s.default.withFont(),
            hovered = s.hovered.withFont(),
            focused = s.focused.withFont(),
            pressed = s.pressed.withFont(),
            disabled = s.disabled.withFont(),
            selected = s.selected.withFont(),
            error = s.error.withFont()))
}
