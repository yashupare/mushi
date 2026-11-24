package com.mushi.app.core.ui.component

import com.mushi.app.core.ui.component.MushiButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import compose.icons.FeatherIcons
import compose.icons.feathericons.Lock
import compose.icons.feathericons.LogIn
import com.mushi.app.core.ui.theming.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumIcon
import com.mushi.app.generated.resources.*

/** Friendly "sign in first" state for screens whose metadata plugin needs an account. */
@Composable
fun LoginRequiredDisplay(
    pluginName: String?,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingIn: Boolean = false) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center) {
            MillenniumIcon(
                imageVector = FeatherIcons.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AppTheme.colors.primary)
        }

        Text(
            text = stringResource(Res.string.login_required_title),
            style = AppTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center)

        Text(
            text = if (pluginName.isNullOrBlank()) {
                stringResource(Res.string.login_required_description)
            } else {
                stringResource(Res.string.login_required_description_plugin, pluginName)
            },
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center)

        MushiButton(
            onClick = onLogin,
            loading = isLoggingIn,
            modifier = Modifier.height(44.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MillenniumIcon(
                    imageVector = FeatherIcons.LogIn,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp).size(18.dp))
                Text(stringResource(Res.string.login_required_action))
            }
        }
    }
}
