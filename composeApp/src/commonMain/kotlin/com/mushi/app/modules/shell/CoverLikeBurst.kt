package com.mushi.app.modules.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.theming.LocalMushiBrand
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxHeart2
import org.spotube.millennium.composables.MillenniumIcon

/** Triggers the big heart drawn over the player cover. */
internal class CoverLikeState {
    var plays by mutableIntStateOf(0)
        private set

    fun play() {
        plays++
    }
}

/** A large heart that pops and fades over the cover, with the heart button's burst around it. */
@Composable
internal fun CoverLikeBurst(state: CoverLikeState, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(1f) }
    val colors = LocalMushiBrand.current.gradient
    LaunchedEffect(state.plays) {
        if (state.plays == 0) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = LinearEasing))
    }
    if (state.plays == 0) return
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(160.dp).drawBehind { drawLikeBurst(progress.value, colors) },
            contentAlignment = Alignment.Center
        ) {
            MillenniumIcon(
                imageVector = Iconsax.IconsaxHeart2,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(96.dp).graphicsLayer {
                    val t = progress.value
                    // Pops in during the first quarter, holds, then fades.
                    val scale = if (t < 0.25f) 0.4f + 2.8f * t else (1.1f - 0.1f * ((t - 0.25f) * 4f)).coerceAtLeast(1f)
                    scaleX = scale
                    scaleY = scale
                    alpha = if (t < 0.7f) 1f else ((1f - t) / 0.3f).coerceIn(0f, 1f)
                })
        }
    }
}
