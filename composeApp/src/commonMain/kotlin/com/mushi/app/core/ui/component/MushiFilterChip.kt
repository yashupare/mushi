package com.mushi.app.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.ui.rememberHaptics
import com.mushi.app.core.ui.theming.AppTheme
import org.spotube.millennium.theme.MillenniumTheme

/**
 * The pill used to filter a page (Home genres, Library sections, Search types): the selected one
 * is filled with the accent colour, the others sit quietly on a faint tint.
 */
@Composable
fun MushiFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHaptics()
    val background by animateColorAsState(
        if (selected) AppTheme.colors.primary else AppTheme.colors.onSurface.copy(alpha = 0.08f),
        label = "chip_background")
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(background)
            .selectable(selected = selected, role = Role.Tab) {
                if (!selected) haptics.tick()
                onClick()
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MillenniumTheme.colors.onAccent else AppTheme.colors.onSurface,
            maxLines = 1
        )
    }
}
