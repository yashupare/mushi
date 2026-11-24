package com.mushi.app.core.ui.component.cards

import com.mushi.app.core.ui.component.pulseRings
import com.mushi.app.core.ui.component.gentleFloat
import com.mushi.app.core.ui.staggeredEntrance
import com.mushi.app.core.ui.rememberStaggerState
import com.mushi.app.core.ui.theming.flatPanel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.ui.theming.AppTheme
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumBevelBrush
import org.spotube.millennium.theme.millenniumSurface

@Composable
internal fun MediaEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    // The tile floats with soft "sound wave" rings, and the text rises in after it.
    val stagger = rememberStaggerState()
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 360.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .staggeredEntrance(stagger, 0)
                    .gentleFloat()
                    .pulseRings(AppTheme.colors.primary.copy(alpha = 0.9f))
                    .size(64.dp)
                    .flatPanel(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                MillenniumIcon(
                    icon,
                    null,
                    modifier = Modifier.size(30.dp),
                    tint = AppTheme.colors.onSurfaceVariant
                )
            }
            Text(
                title,
                style = AppTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.staggeredEntrance(stagger, 1))
            if (description != null) Text(
                description,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.staggeredEntrance(stagger, 2)
            )
            if (action != null) Box(Modifier.staggeredEntrance(stagger, 3)) { action() }
        }
    }
}
