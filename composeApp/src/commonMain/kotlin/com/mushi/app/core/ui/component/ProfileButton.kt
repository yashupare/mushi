package com.mushi.app.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.spotube.millennium.composables.MillenniumIcon
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.User
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.settings_screen_title

/**
 * The round account button at the top right of the main screens. It opens Settings, which no
 * longer has a tab of its own. The tap target is 44 dp; the visible circle is smaller.
 */
@Composable
fun ProfileButton(modifier: Modifier = Modifier) {
    val navigationCommands: NavigationCommands = koinInject()
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable { navigationCommands.navigateTo(Routes.Settings) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.onSurface.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            MillenniumIcon(
                imageVector = com.mushi.app.core.ui.theming.MushiIcons.User,
                contentDescription = stringResource(Res.string.settings_screen_title),
                tint = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
