package com.mushi.app.core.ui.component.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumActionPointer
import org.spotube.millennium.theme.millenniumSurface

/** Interaction state is presentation-only; all navigation/playback stays in event callbacks. */
@Composable
internal fun MediaCardSurface(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    interactive: Boolean = onClick != null,
    isBlacklisted: Boolean = false,
    content: @Composable (highlighted: Boolean) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    var groupFocused by remember { mutableStateOf(false) }
    val colors = MillenniumTheme.colors
    val highlight by animateFloatAsState(
        if (hovered || focused || groupFocused || pressed) 1f else 0f,
        tween(140)
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = com.mushi.app.core.ui.pressSpring(),
        label = "card_press")
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            .millenniumSurface(
                // Flat feedback: a soft tint while hovered or pressed. No border, glow or shadow
                // (the keyboard focus ring stays for accessibility).
                background = Brush.verticalGradient(
                    listOf(
                        (if (isBlacklisted) colors.danger else colors.text)
                            .copy(alpha = 0.06f * highlight + if (isBlacklisted) 0.12f else 0f),
                        (if (isBlacklisted) colors.danger else colors.text)
                            .copy(alpha = 0.06f * highlight + if (isBlacklisted) 0.12f else 0f)
                    )
                ),
                shape = shape,
                borderColor = if (focused) colors.accent else androidx.compose.ui.graphics.Color.Transparent,
                borderWidth = if (focused) 2.dp else 0.dp,
                innerGlowColor = androidx.compose.ui.graphics.Color.Transparent,
                shadowElevation = 0.dp
            )
            .clip(shape)
            .onFocusChanged { groupFocused = it.hasFocus }
            .focusGroup()
            .hoverable(interaction, enabled = interactive)
            .then(
                if (onClick != null) Modifier.millenniumActionPointer().clickable(
                    interactionSource = interaction,
                    indication = LocalIndication.current,
                    role = Role.Button,
                    onClick = onClick
                ) else Modifier
            )
    ) { content(hovered || focused || groupFocused) }
}
